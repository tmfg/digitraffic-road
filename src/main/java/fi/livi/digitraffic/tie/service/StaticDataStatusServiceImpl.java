package fi.livi.digitraffic.tie.service;

import fi.livi.digitraffic.tie.dao.StaticDataStatusDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaticDataStatusServiceImpl implements StaticDataStatusService {

    private StaticDataStatusDAO staticDataStatusDAO;



    @Autowired
    public StaticDataStatusServiceImpl(StaticDataStatusDAO staticDataStatusDAO) {
        this.staticDataStatusDAO = staticDataStatusDAO;
    }

    @Transactional
    @Override
    public void updateStaticDataStatus(StaticStatusType type, boolean updateStaticDataStatus) {
        staticDataStatusDAO.updateStaticDataStatus(type, updateStaticDataStatus);
    }
}
