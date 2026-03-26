package app.runplan.api.persistence.repository;

import app.runplan.api.persistence.entity.SleepEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SleepEntryRepository extends JpaRepository<SleepEntryEntity, LocalDate> {
    List<SleepEntryEntity> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);

    List<SleepEntryEntity> findByDateIn(List<LocalDate> dates);
}
