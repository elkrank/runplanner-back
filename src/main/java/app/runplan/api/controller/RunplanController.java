package app.runplan.api.controller;

import app.runplan.api.dto.ApiDtos.*;
import app.runplan.api.service.RunplanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@Validated
public class RunplanController {
    private final RunplanService service;

    public RunplanController(RunplanService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public Profile getProfile() {
        return service.getProfile();
    }

    @PatchMapping("/me")
    public Profile patchProfile(@Valid @RequestBody ProfilePatch input) {
        return service.updateProfile(input);
    }

    @GetMapping("/weeks/{isoWeek}")
    public WeekBundle getWeek(@PathVariable @Pattern(regexp = "^\\d{4}-W\\d{2}$") String isoWeek) {
        return service.getWeekBundle(isoWeek);
    }

    @DeleteMapping("/weeks/{isoWeek}/sessions")
    public ResponseEntity<Void> clearWeekSessions(@PathVariable @Pattern(regexp = "^\\d{4}-W\\d{2}$") String isoWeek) {
        service.clearWeekSessions(isoWeek);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    public ItemsResponse<Session> getSessions(@RequestParam("from") LocalDate from, @RequestParam("to") LocalDate to) {
        return new ItemsResponse<>(service.listSessions(from, to));
    }

    @PostMapping("/sessions")
    public ResponseEntity<Session> createSession(@Valid @RequestBody SessionCreate input) {
        var created = service.createSession(input);
        return ResponseEntity.created(URI.create("/sessions/" + created.id())).body(created);
    }

    @PatchMapping("/sessions/{sessionId}")
    public ResponseEntity<?> patchSession(@PathVariable UUID sessionId, @Valid @RequestBody SessionPatch input) {
        return service.patchSession(sessionId, input)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(new ErrorResponse("NOT_FOUND", "Session not found", Map.of("sessionId", sessionId))));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable UUID sessionId) {
        boolean deleted = service.deleteSession(sessionId);
        if (!deleted) {
            return ResponseEntity.status(404).body(new ErrorResponse("NOT_FOUND", "Session not found", Map.of("sessionId", sessionId)));
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sleep")
    public ItemsResponse<SleepEntry> getSleep(@RequestParam("from") LocalDate from, @RequestParam("to") LocalDate to) {
        return new ItemsResponse<>(service.listSleep(from, to));
    }

    @PutMapping("/sleep/{date}")
    public SleepEntry upsertSleep(@PathVariable LocalDate date, @Valid @RequestBody SleepEntryUpsert input) {
        return service.upsertSleep(date, input);
    }

    @DeleteMapping("/sleep/{date}")
    public ResponseEntity<Void> deleteSleep(@PathVariable LocalDate date) {
        service.deleteSleep(date);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/weight")
    public ItemsResponse<WeightEntry> getWeight(@RequestParam("from") LocalDate from, @RequestParam("to") LocalDate to) {
        return new ItemsResponse<>(service.listWeight(from, to));
    }

    @PutMapping("/weight/{date}")
    public WeightEntry upsertWeight(@PathVariable LocalDate date,
                                    @RequestBody Map<String, @DecimalMin("30") @DecimalMax("250") Double> payload) {
        return service.upsertWeight(date, payload.get("weightKg"));
    }

    @DeleteMapping("/weight/{date}")
    public ResponseEntity<Void> deleteWeight(@PathVariable LocalDate date) {
        service.deleteWeight(date);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/week/{isoWeek}")
    public WeekStats weekStats(@PathVariable @Pattern(regexp = "^\\d{4}-W\\d{2}$") String isoWeek) {
        return service.getWeekStats(isoWeek);
    }
}
