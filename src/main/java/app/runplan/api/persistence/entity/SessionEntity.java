package app.runplan.api.persistence.entity;

import app.runplan.api.dto.ApiDtos.SessionCategory;
import app.runplan.api.dto.ApiDtos.SessionType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class SessionEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType type;

    @Column(nullable = false)
    private String kind;

    @Column
    private Double distKm;

    @Column
    private Integer durMin;

    @Column
    private String pace;

    @Column(columnDefinition = "text")
    private String notes;

    @ElementCollection
    @CollectionTable(name = "session_exercises", joinColumns = @JoinColumn(name = "session_id"))
    private List<ExerciseEmbeddable> exercises = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public SessionCategory getCategory() {
        return category;
    }

    public void setCategory(SessionCategory category) {
        this.category = category;
    }

    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Double getDistKm() {
        return distKm;
    }

    public void setDistKm(Double distKm) {
        this.distKm = distKm;
    }

    public Integer getDurMin() {
        return durMin;
    }

    public void setDurMin(Integer durMin) {
        this.durMin = durMin;
    }

    public String getPace() {
        return pace;
    }

    public void setPace(String pace) {
        this.pace = pace;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<ExerciseEmbeddable> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseEmbeddable> exercises) {
        this.exercises = exercises;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
