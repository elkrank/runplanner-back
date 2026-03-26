package app.runplan.api.service;

import app.runplan.api.dto.ApiDtos.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RunplanService {
    private final UUID userId = UUID.randomUUID();
    private Profile profile = new Profile(userId, 75.0, null, null);

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Map<LocalDate, SleepEntry> sleepEntries = new ConcurrentHashMap<>();
    private final Map<LocalDate, WeightEntry> weightEntries = new ConcurrentHashMap<>();

    public Profile getProfile() {
        return profile;
    }

    public Profile updateProfile(ProfilePatch patch) {
        profile = new Profile(
                userId,
                patch.weightCurrentKg() != null ? patch.weightCurrentKg() : profile.weightCurrentKg(),
                patch.heightCm() != null ? patch.heightCm() : profile.heightCm(),
                patch.weightGoalKg() != null ? patch.weightGoalKg() : profile.weightGoalKg()
        );
        return profile;
    }

    public List<Session> listSessions(LocalDate from, LocalDate to) {
        return sessions.values().stream().filter(s -> !s.date().isBefore(from) && !s.date().isAfter(to))
                .sorted(Comparator.comparing(Session::date)).toList();
    }

    public Session createSession(SessionCreate input) {
        var now = Instant.now();
        var session = new Session(UUID.randomUUID(), input.date(), input.category(), input.type(), input.kind(), input.distKm(),
                input.durMin(), input.pace(), input.notes(), input.exercises() == null ? List.of() : input.exercises(), now, now);
        sessions.put(session.id(), session);
        return session;
    }

    public Optional<Session> patchSession(UUID id, SessionPatch patch) {
        var existing = sessions.get(id);
        if (existing == null) return Optional.empty();
        var updated = new Session(id,
                patch.date() != null ? patch.date() : existing.date(),
                patch.category() != null ? patch.category() : existing.category(),
                patch.type() != null ? patch.type() : existing.type(),
                patch.kind() != null ? patch.kind() : existing.kind(),
                patch.distKm() != null ? patch.distKm() : existing.distKm(),
                patch.durMin() != null ? patch.durMin() : existing.durMin(),
                patch.pace() != null ? patch.pace() : existing.pace(),
                patch.notes() != null ? patch.notes() : existing.notes(),
                patch.exercises() != null ? patch.exercises() : existing.exercises(),
                existing.createdAt(), Instant.now());
        sessions.put(id, updated);
        return Optional.of(updated);
    }

    public boolean deleteSession(UUID id) {
        return sessions.remove(id) != null;
    }

    public List<SleepEntry> listSleep(LocalDate from, LocalDate to) {
        return sleepEntries.values().stream().filter(s -> !s.date().isBefore(from) && !s.date().isAfter(to))
                .sorted(Comparator.comparing(SleepEntry::date)).toList();
    }

    public SleepEntry upsertSleep(LocalDate date, SleepEntryUpsert input) {
        var entry = new SleepEntry(date, input.bedtime(), input.wakeup(), input.durationH(), input.quality(), input.wakeups(), input.notes());
        sleepEntries.put(date, entry);
        return entry;
    }

    public void deleteSleep(LocalDate date) {
        sleepEntries.remove(date);
    }

    public List<WeightEntry> listWeight(LocalDate from, LocalDate to) {
        return weightEntries.values().stream().filter(w -> !w.date().isBefore(from) && !w.date().isAfter(to))
                .sorted(Comparator.comparing(WeightEntry::date)).toList();
    }

    public WeightEntry upsertWeight(LocalDate date, Double weightKg) {
        var entry = new WeightEntry(date, weightKg);
        weightEntries.put(date, entry);
        return entry;
    }

    public void deleteWeight(LocalDate date) {
        weightEntries.remove(date);
    }

    public WeekStats getWeekStats(String isoWeek) {
        var weekDates = weekDates(isoWeek);
        var weekSessions = sessions.values().stream().filter(s -> weekDates.contains(s.date())).toList();
        var weekSleep = sleepEntries.values().stream().filter(s -> weekDates.contains(s.date())).toList();

        double distance = weekSessions.stream().mapToDouble(s -> s.distKm() == null ? 0 : s.distKm()).sum();
        int duration = weekSessions.stream().mapToInt(s -> s.durMin() == null ? 0 : s.durMin()).sum();
        int count = weekSessions.size();
        int calories = (int) Math.round(distance * 65);
        var dominantType = weekSessions.stream().collect(java.util.stream.Collectors.groupingBy(Session::type, java.util.stream.Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

        int nights = weekSleep.size();
        double totalHours = weekSleep.stream().mapToDouble(SleepEntry::durationH).sum();
        Double avgHours = nights == 0 ? null : totalHours / nights;
        Double best = nights == 0 ? null : weekSleep.stream().mapToDouble(SleepEntry::durationH).max().orElse(0);
        Double avgQuality = nights == 0 ? null : weekSleep.stream().mapToInt(SleepEntry::quality).average().orElse(0);

        return new WeekStats(
                new TrainingStats(distance, duration, count, calories, dominantType),
                new SleepStats(nights, avgHours, nights == 0 ? null : totalHours, best, avgQuality)
        );
    }

    public WeekBundle getWeekBundle(String isoWeek) {
        var dates = weekDates(isoWeek);
        var sessionsByDay = dates.stream().map(d -> sessions.values().stream().filter(s -> s.date().equals(d)).toList()).toList();
        var sleepByDay = dates.stream().map(sleepEntries::get).toList();
        return new WeekBundle(isoWeek, dates, sessionsByDay, sleepByDay, getWeekStats(isoWeek));
    }

    public void clearWeekSessions(String isoWeek) {
        var dates = new HashSet<>(weekDates(isoWeek));
        sessions.entrySet().removeIf(e -> dates.contains(e.getValue().date()));
    }

    private List<LocalDate> weekDates(String isoWeek) {
        var parts = isoWeek.split("-W");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);
        WeekFields wf = WeekFields.ISO;
        LocalDate first = LocalDate.of(year, 1, 4)
                .with(wf.weekOfWeekBasedYear(), week)
                .with(wf.dayOfWeek(), 1);
        return java.util.stream.IntStream.range(0, 7).mapToObj(first::plusDays).toList();
    }
}
