package fi.livi.digitraffic.tie.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface SqlRepository extends Repository<SqlRepository.DummyEntity, Long> {
    @Entity
    class DummyEntity {
        @Id
        public long id;
    }
}
