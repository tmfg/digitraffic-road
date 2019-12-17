package fi.livi.digitraffic.tie.service.v2.datex2;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.JsonMessage;
import fi.livi.digitraffic.tie.helper.DateHelper;

@Service
public class V2Datex2HelperService {
    private static final Logger log = LoggerFactory.getLogger(V2Datex2HelperService.class);

    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;

    @Autowired
    public V2Datex2HelperService(final ObjectMapper objectMapper) {
        jsonWriter = objectMapper.writerFor(JsonMessage.class);
        jsonReader = objectMapper.readerFor(JsonMessage.class);
    }

    /**
     *
     * @param json
     * @return Json object or null in case of error
     */
    public JsonMessage convertToJsonObject(final String json) {
        try {
            return jsonReader.readValue(json);
        } catch (JsonProcessingException e) {
            log.error("method=convertToJsonObject error while converting JSON to SimppeliSituationV02Schema jsonValue=\n" + json, e);
            throw new RuntimeException(e);
        }
    }

    public String convertToJsonString(final JsonMessage situation) {
        try {
            return jsonWriter.writeValueAsString(situation);
        } catch (JsonProcessingException e) {
            log.error("method=writeValueAsString Error while converting jsonSituation-object to string with guid " + situation.getSituationId());
            throw new RuntimeException(e);
        }
    }

    public static boolean isNewOrUpdatedSituation(final ZonedDateTime latestVersionTime, final Situation situation) {
        // does any record have new version time?
        return situation.getSituationRecords().stream().anyMatch(r -> isNewOrUpdatedRecord(latestVersionTime, r));
    }

    public static boolean isNewOrUpdatedRecord(final ZonedDateTime latestVersionTime, final SituationRecord record) {
        // different resolution, so remove fractions of second
        final Instant vTime = DateHelper.withoutMillis(record.getSituationRecordVersionTime());
        return latestVersionTime == null || vTime.isAfter(DateHelper.withoutMillis(latestVersionTime.toInstant()) );
    }
}
