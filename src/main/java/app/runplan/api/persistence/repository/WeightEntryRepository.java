package app.runplan.api.persistence.repository;

import app.runplan.api.persistence.entity.WeightEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeightEntryRepository extends JpaRepository<WeightEntryEntity, LocalDate> {
    List<WeightEntryEntity> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}
