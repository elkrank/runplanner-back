package app.runplan.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApiDtos {

    public enum SessionCategory {run, strength}

    public enum SessionType {easy, moderate, hard, rest}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(String code, String message, Map<String, Object> details) {
    }

    public record Profile(UUID userId,
                          @DecimalMin("30") @DecimalMax("200") Double weightCurrentKg,
                          @Min(100) @Max(230) Integer heightCm,
                          @DecimalMin("30") @DecimalMax("200") Double weightGoalKg) {
    }

    public record ProfilePatch(@DecimalMin("30") @DecimalMax("200") Double weightCurrentKg,
                               @Min(100) @Max(230) Integer heightCm,
                               @DecimalMin("30") @DecimalMax("200") Double weightGoalKg) {
    }

    public record Exercise(@NotBlank String name,
                           @Min(1) Integer sets,
                           String reps,
                           @DecimalMin("0") Double weightKg) {
    }

    public record SessionCreate(@NotNull LocalDate date,
                                @NotNull SessionCategory category,
                                @NotNull SessionType type,
                                @NotBlank String kind,
                                @DecimalMin("0") Double distKm,
                                @Min(0) Integer durMin,
                                String pace,
                                String notes,
                                @Valid List<Exercise> exercises) {
    }

    public record SessionPatch(LocalDate date,
                               SessionCategory category,
                               SessionType type,
                               String kind,
                               @DecimalMin("0") Double distKm,
                               @Min(0) Integer durMin,
                               String pace,
                               String notes,
                               @Valid List<Exercise> exercises) {
    }

    public record Session(UUID id,
                          LocalDate date,
                          SessionCategory category,
                          SessionType type,
                          String kind,
                          Double distKm,
                          Integer durMin,
                          String pace,
                          String notes,
                          List<Exercise> exercises,
                          Instant createdAt,
                          Instant updatedAt) {
    }

    public record SleepEntryUpsert(@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$") String bedtime,
                                   @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$") String wakeup,
                                   @DecimalMin("0") @DecimalMax("14") Double durationH,
                                   @Min(1) @Max(4) Integer quality,
                                   @Min(0) @Max(20) Integer wakeups,
                                   String notes) {
    }

    public record SleepEntry(LocalDate date,
                             String bedtime,
                             String wakeup,
                             Double durationH,
                             Integer quality,
                             Integer wakeups,
                             String notes) {
    }

    public record WeightEntry(LocalDate date,
                              @DecimalMin("30") @DecimalMax("250") Double weightKg) {
    }

    public record WeekStats(TrainingStats training, SleepStats sleep) {
    }

    public record TrainingStats(Double distanceKm,
                                Integer durationMinutes,
                                Integer sessionCount,
                                Integer calories,
                                SessionType dominantType) {
    }

    public record SleepStats(Integer nightsFilled,
                             Double averageHours,
                             Double totalHours,
                             Double bestNightHours,
                             Double averageQuality) {
    }

    public record WeekBundle(String isoWeek,
                             List<LocalDate> dates,
                             List<List<Session>> sessionsByDay,
                             List<SleepEntry> sleepByDay,
                             WeekStats stats) {
    }

    public record ItemsResponse<T>(List<T> items) {
    }
}
