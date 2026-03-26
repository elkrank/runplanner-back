package app.runplan.api.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "profiles")
public class ProfileEntity {
    @Id
    private UUID userId;

    @Column
    private Double weightCurrentKg;

    @Column
    private Integer heightCm;

    @Column
    private Double weightGoalKg;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Double getWeightCurrentKg() {
        return weightCurrentKg;
    }

    public void setWeightCurrentKg(Double weightCurrentKg) {
        this.weightCurrentKg = weightCurrentKg;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
    }

    public Double getWeightGoalKg() {
        return weightGoalKg;
    }

    public void setWeightGoalKg(Double weightGoalKg) {
        this.weightGoalKg = weightGoalKg;
    }
}
