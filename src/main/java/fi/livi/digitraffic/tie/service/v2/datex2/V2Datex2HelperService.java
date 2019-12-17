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

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
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
        return isNewOrUpdatedSituation(DateHelper.toInstant(latestVersionTime), situation);
    }

    public static boolean isNewOrUpdatedSituation(final Instant latestVersionTime, final Situation situation) {
        // does any record have new version time?
        return latestVersionTime == null || situation.getSituationRecords().stream().anyMatch(r -> isUpdatedRecord(latestVersionTime, r));
    }

    public static boolean isUpdatedRecord(final Instant latestVersionTime, final SituationRecord record) {
        // different resolution, so remove fractions of second
        final Instant vTime = DateHelper.withoutMillis(record.getSituationRecordVersionTime());
        return vTime.isAfter(DateHelper.withoutMillis(latestVersionTime) );
    }

    public static SituationPublication getSituationPublication(final D2LogicalModel model) {
        if (model.getPayloadPublication() instanceof SituationPublication) {
            return (SituationPublication) model.getPayloadPublication();
        } else {
            final String err = "Not SituationPublication available for " + model.getPayloadPublication().getClass();
            log.error(err);
            throw new RuntimeException(err);
        }
    }

    public static void checkD2HasOnlyOneSituation(D2LogicalModel d2) {
        final int situations = getSituationPublication(d2).getSituations().size();
        if ( situations > 1 ) {
            log.error("method=checkOnyOneSituation D2LogicalModel had {) situations. Only 1 is allowed in this service.");
            throw new java.lang.IllegalArgumentException("D2LogicalModel passed to Datex2UpdateService can only have one situation per message, " +
                "there was " + situations);
        }
    }
}
