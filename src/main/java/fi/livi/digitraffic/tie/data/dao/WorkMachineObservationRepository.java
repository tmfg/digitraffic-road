package fi.livi.digitraffic.tie.data.dao;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineObservation;

public interface WorkMachineObservationRepository extends JpaRepository<WorkMachineObservation, Long> {

    List<WorkMachineObservation> findByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaId(final long workMachineHarjaId, final long contractHarjaId);

    WorkMachineObservation findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByUpdatedDesc(final long workMachineHarjaId, final long contractHarjaId);

    @Modifying
    @Query(value =
               "INSERT INTO WORK_MACHINE_OBSERVATION_COORDINATE (work_machine_observation_id, order_number, longitude, latitude, observation_time)\n" +
               "SELECT :observationId , COALESCE(MAX(order_number) + 1, 0), :longitude, :latitude, coalesce(:observationTime, null)::timestamptz\n" +
               "FROM WORK_MACHINE_OBSERVATION_COORDINATE\n" +
               "WHERE work_machine_observation_id=:observationId",
           nativeQuery = true)
    int addCoordinates(final long observationId,
                       final BigDecimal longitude,
                       final BigDecimal latitude,
                       final Timestamp observationTime);

    @Query(value =
               "SELECT COALESCE(MAX(order_number), -1)\n" +
               "FROM WORK_MACHINE_OBSERVATION_COORDINATE\n" +
               "WHERE work_machine_observation_id = :observationId",
           nativeQuery = true)
    int getLastCoordinateOrder(final long observationId);

    @Modifying
    @Query(value =
               "insert into work_machine_task (work_machine_coordinate_observation_id, work_machine_coordinate_order_number, task)\n" +
               "select :observationId, MAX(order_number), :task\n" +
               "from work_machine_observation_coordinate\n" +
               "where work_machine_observation_id = :observationId",
           nativeQuery = true)
    void addCoordinateTaskToLastCoordinate(final Long observationId, String task);
}
