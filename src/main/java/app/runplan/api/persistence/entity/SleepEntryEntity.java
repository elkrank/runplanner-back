package app.runplan.api.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "sleep_entries")
public class SleepEntryEntity {
    @Id
    private LocalDate date;

    @Column
    private String bedtime;

    @Column
    private String wakeup;

    @Column
    private Double durationH;

    @Column
    private Integer quality;

    @Column
    private Integer wakeups;

    @Column(columnDefinition = "text")
    private String notes;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getBedtime() {
        return bedtime;
    }

    public void setBedtime(String bedtime) {
        this.bedtime = bedtime;
    }

    public String getWakeup() {
        return wakeup;
    }

    public void setWakeup(String wakeup) {
        this.wakeup = wakeup;
    }

    public Double getDurationH() {
        return durationH;
    }

    public void setDurationH(Double durationH) {
        this.durationH = durationH;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    public Integer getWakeups() {
        return wakeups;
    }

    public void setWakeups(Integer wakeups) {
        this.wakeups = wakeups;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
