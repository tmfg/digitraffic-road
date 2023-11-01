package fi.livi.digitraffic.tie.service.trafficmessage;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.AreaType;
import fi.livi.digitraffic.tie.helper.GeometryConstants;
import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;

@ConditionalOnNotWebApplication
@Component
public class RegionGeometryGitClient {
    private static final Logger log = LoggerFactory.getLogger(RegionGeometryGitClient.class);

    private static final File LOCAL_TGT_DIR = new File(System.getProperty("java.io.tmpdir") + "/tmfg_metadata");

    private final String gitUrl;
    private final String gitPath;
    private final ObjectReader genericJsonReader;
    private final GeoJsonReader geoJsonReader = new GeoJsonReader(GeometryConstants.JTS_GEOMETRY_FACTORY);

    public RegionGeometryGitClient(@Value("${dt.traffic-messages.git-repo.url}")
                                   final String gitUrl,
                                   @Value("${dt.traffic-messages.git-repo.path}")
                                   final String gitPath,
                                   final ObjectMapper objectMapper) {

        this.gitUrl = gitUrl;
        this.gitPath = gitPath;
        genericJsonReader = objectMapper.reader();
    }

    @Retryable
    public List<RegionGeometry> getChangesAfterCommit(final String afterCommitId) {
        log.info("method=getChangesAfterCommit {}", afterCommitId);

        try (final Git git = cloneOrPullRepository(gitUrl, LOCAL_TGT_DIR)) {

            final List<RevCommit> logAsc = getCommitsAfterCommitIdAsc(git, afterCommitId);

            final Repository repository = git.getRepository();

            final List<RegionGeometry> changesAfterCommit = new ArrayList<>();

            logAsc.forEach(commit -> {
                final List<DiffEntry> changes = getJsonChangesForCommit(commit, gitPath, repository);
                changes.forEach(diff -> appendChanges(changesAfterCommit, diff, commit, repository));
            });
            log.info("method=getChangesAfterCommit {} was {}", afterCommitId, changesAfterCommit.size());
            return changesAfterCommit;
        }
    }

    private void appendChanges(final List<RegionGeometry> changesAfterCommit,
                               final DiffEntry diff, final RevCommit commit, final Repository repository) {
        final DiffEntry.ChangeType changeType = diff.getChangeType();
        if (changeType.equals(DiffEntry.ChangeType.DELETE)) {
            log.info("Skip delete as latest version will be effective also after delete");
        } else {
            try {
                final ObjectLoader objectLoader = repository.open(diff.getNewId().toObjectId());
                final String content = new String(objectLoader.getBytes());
                changesAfterCommit.add(createAreaLocationRegionObject(commit, diff, content));
            } catch (final IOException e) {
                log.error(format("Failed to read contents of commit {0} for {1}} changeType {2}}", commit.getId().getName(), diff.getNewPath(), diff.getChangeType()), e);
                throw new RuntimeException(e);
            } catch (final ParseException e) {
                log.error(format("Failed to parse contents of commit {0} for {1} changeType {2}}", commit.getId().getName(), diff.getNewPath(), diff.getChangeType()), e);
                throw new RuntimeException(e);
            }
        }
    }

    private RegionGeometry createAreaLocationRegionObject(final RevCommit commit, final DiffEntry diff,
                                                          final String contentJson) throws JsonProcessingException, ParseException {
        try {
            final String gitPath = diff.getNewPath();
            final String gitId = diff.getNewId().toObjectId().getName();
            final Instant versionDate = Instant.ofEpochSecond(commit.getCommitTime());
            final JsonNode json = genericJsonReader.readTree(contentJson);
            final int locationCode = readLocationCode(json);
            final String name = readName(json);
            final AreaType type = readType(json);
            final Instant effectiveDate = readEffectiveDate(json);
            final Geometry geometry = geoJsonReader.read(json.get("geometry").toString());
            log.info("Create new RegionGeometry with name {}, locationCode {}, type {}, effectiveDate {}, gitPath {}, changeType {}", name, locationCode,type, effectiveDate, gitPath, diff.getChangeType());
            return new RegionGeometry(name, locationCode, type, effectiveDate, geometry, versionDate, gitId, gitPath, commit.getId().getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int readLocationCode(final JsonNode json) {
        return json.get("locationCode").asInt();
    }

    private String readName(final JsonNode json) {
        return json.get("name").asText();
    }

    private AreaType readType(final JsonNode json) {
        if (json.has("type")) {
            return AreaType.fromValue(json.get("type").asText());
        }
        return AreaType.UNKNOWN;
    }

    private Instant readEffectiveDate(final JsonNode json) {
        return ZonedDateTime.parse(json.get("effectiveDate").asText()).toInstant();
    }

    private List<RevCommit> getCommitsAfterCommitIdAsc(final Git git, final String afterCommitId) {
        try {
            final Iterable<RevCommit> logDesc = git.log().addPath(gitPath).call();

            // The output is given in reverse chronological order by default. (newest first) -> reverse to get from oldest first.
            final List<RevCommit> logAsc = new ArrayList<>();
            for (final RevCommit revCommit : logDesc) {
                // if we find the given afterCommitId then we have gone trough all newer commits and can break
                if (afterCommitId != null && afterCommitId.equals(revCommit.getId().getName())) {
                    break;
                }
                logAsc.add(0, revCommit);
            }
            return logAsc;
        } catch (GitAPIException e) {
            log.error(format("Failed to get Git log after commitId {0}", afterCommitId), e);
            throw new RuntimeException(e);
        }
    }

    private static List<DiffEntry> getJsonChangesForCommit(final RevCommit commit, final String path, final Repository repository) {
        final List<DiffEntry> diffsOut = new ArrayList<>();

        final DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);

        try {
            log.info("Get commit {} at {} message {}", commit.getId().getName(), Instant.ofEpochSecond(commit.getCommitTime()), commit.getShortMessage());
            final RevTree parentTree = commit.getParentCount() > 0 ? commit.getParent(0).getTree() : null;
            final List<DiffEntry> diffs = df.scan(parentTree, commit.getTree());
            for (final DiffEntry diff : diffs) {
                final String filePath = diff.getNewPath();
                // Return only changes under given directory to json files
                if (filePath.contains(path) && filePath.endsWith(".json")) {
                    diffsOut.add(diff);
                }
            }
        } catch (final IOException e) {
            log.error(format("Failed to get changes for commit {0}", commit.getId().getName()), e);
            throw new RuntimeException(e);
        }
        log.info("{} changes to JSON-files in commit {}", diffsOut.size(), commit.getId().getName());
        return diffsOut;
    }

    private static Git cloneOrPullRepository(final String repoUrl, final File targetPath) {

        if (targetPath.exists()) {
            log.info("Git repo {} already checked out to {}. Do pull.", repoUrl, targetPath);
            final Git git = gitOpen(targetPath);
            if (git != null) {
                try {
                    git.pull().call();
                    return git;
                } catch (final GitAPIException e) {
                    log.error(format("Failed to do git pull for local repository at {0}. Clean it and do clone.", targetPath), e);
                    // If we are here there is an error and we should clean up the local
                    cleanLocalRepo(targetPath);
                }
            } else {
                cleanLocalRepo(targetPath);
            }
        }

        return clone(repoUrl, targetPath);
    }

    private static Git clone(final String repoUrl, final File targetPath) {
        log.info("Cloning repo {} to {}", repoUrl, targetPath);
        final CloneCommand clone = Git
            .cloneRepository()
            .setCloneAllBranches(false)
            .setBranch("master")
            .setURI(repoUrl)
            .setDirectory(targetPath);

        try (final Git git = clone.call()) {
            log.info("Cloning repo {} to {} done", repoUrl, targetPath);
            return git;
        } catch (final Exception e) {
            log.error(format("Failed to clone repository {0} to {1}.", repoUrl, targetPath), e);
            throw new RuntimeException(e);
        }
    }

    private static void cleanLocalRepo(final File targetPath) {
        if (targetPath.exists()) {
            try {
                FileUtils.deleteDirectory(targetPath);
            } catch (IOException e) {
                log.error(format("Failed to delete local repository at {0}.", targetPath), e);
                throw new RuntimeException(e);
            }
        }
    }

    private static Git gitOpen(final File targetPath) {
        try (final Git git = Git.open(targetPath)) {
            return git;
        } catch (IOException e) {
            log.error(format("Failed to open local repository at {0}. Clean it and do clone.", targetPath), e);
        }
        return null;
    }

}
