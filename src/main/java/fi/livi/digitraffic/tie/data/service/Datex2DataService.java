package fi.livi.digitraffic.tie.data.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Exchange;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PayloadPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Situation;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;

@Service
public class Datex2DataService {

    private static final Logger log = LoggerFactory.getLogger(Datex2DataService.class);

//    @Autowired
//    public Datex2DataService() {
//    }

    @Transactional
    public void updateDatex2Data(List<Pair<D2LogicalModel, String>> data) {
        for (Pair<D2LogicalModel, String> pair : data) {
            D2LogicalModel datex = pair.getLeft();
            Exchange exchange = datex.getExchange();
            PayloadPublication payloadPublication = datex.getPayloadPublication();
            if (payloadPublication instanceof SituationPublication) {
                List<Situation> situations = ((SituationPublication) payloadPublication).getSituation();
                for (Situation situation : situations) {
                    log.info(situation.getId());
                }
            }


        }
        List<String> xmls = data.stream().map(o -> o.getRight()).collect(Collectors.toList());



    }
}
