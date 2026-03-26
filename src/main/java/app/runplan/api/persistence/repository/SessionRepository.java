package app.runplan.api.persistence.repository;

import app.runplan.api.persistence.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
    List<SessionEntity> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);

    List<SessionEntity> findByDateIn(List<LocalDate> dates);

    void deleteByDateIn(List<LocalDate> dates);
}
