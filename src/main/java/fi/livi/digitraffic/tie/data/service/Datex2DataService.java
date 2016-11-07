package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@Service
public class Datex2DataService {

    private static final Logger log = LoggerFactory.getLogger(Datex2DataService.class);
    private final Datex2Repository datex2Repository;

    @Autowired
    public Datex2DataService(Datex2Repository datex2Repository) {
        this.datex2Repository = datex2Repository;
    }

    @Transactional
    public void updateDatex2Data(List<Pair<D2LogicalModel, String>> data) {

        for (Pair<D2LogicalModel, String> pair : data) {

            Datex2 datex2 = new Datex2();
            datex2.setImportDate(LocalDateTime.now());
            datex2.setMessage(pair.getRight());
            datex2Repository.save(datex2);

//            D2LogicalModel datex = pair.getLeft();
//            PayloadPublication payloadPublication = datex.getPayloadPublication();
//            if (payloadPublication instanceof SituationPublication) {
//                SituationPublication situationPublication = (SituationPublication) payloadPublication;
//
//                List<Situation> situations = situationPublication.getSituation();
//                for (Situation situation : situations) {
//                    situation.getId();
//                    log.info(situation.getId());
//                }
//            }
        }
    }
}
