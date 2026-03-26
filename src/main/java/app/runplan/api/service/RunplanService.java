package app.runplan.api.service;

import app.runplan.api.dto.ApiDtos.*;
import app.runplan.api.persistence.entity.ExerciseEmbeddable;
import app.runplan.api.persistence.entity.ProfileEntity;
import app.runplan.api.persistence.entity.SessionEntity;
import app.runplan.api.persistence.entity.SleepEntryEntity;
import app.runplan.api.persistence.entity.WeightEntryEntity;
import app.runplan.api.persistence.repository.ProfileRepository;
import app.runplan.api.persistence.repository.SessionRepository;
import app.runplan.api.persistence.repository.SleepEntryRepository;
import app.runplan.api.persistence.repository.WeightEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class RunplanService {
    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final ProfileRepository profileRepository;
    private final SessionRepository sessionRepository;
    private final SleepEntryRepository sleepEntryRepository;
    private final WeightEntryRepository weightEntryRepository;

    public RunplanService(ProfileRepository profileRepository,
                          SessionRepository sessionRepository,
                          SleepEntryRepository sleepEntryRepository,
                          WeightEntryRepository weightEntryRepository) {
        this.profileRepository = profileRepository;
        this.sessionRepository = sessionRepository;
        this.sleepEntryRepository = sleepEntryRepository;
        this.weightEntryRepository = weightEntryRepository;
    }

    public Profile getProfile() {
        return toDto(getOrCreateProfile());
    }

    public Profile updateProfile(ProfilePatch patch) {
        var profile = getOrCreateProfile();
        profile.setWeightCurrentKg(patch.weightCurrentKg() != null ? patch.weightCurrentKg() : profile.getWeightCurrentKg());
        profile.setHeightCm(patch.heightCm() != null ? patch.heightCm() : profile.getHeightCm());
        profile.setWeightGoalKg(patch.weightGoalKg() != null ? patch.weightGoalKg() : profile.getWeightGoalKg());
        return toDto(profileRepository.save(profile));
    }

    public List<Session> listSessions(LocalDate from, LocalDate to) {
        return sessionRepository.findByDateBetweenOrderByDateAsc(from, to).stream().map(this::toDto).toList();
    }

    public Session createSession(SessionCreate input) {
        var now = Instant.now();
        var entity = new SessionEntity();
        entity.setId(UUID.randomUUID());
        entity.setDate(input.date());
        entity.setCategory(input.category());
        entity.setType(input.type());
        entity.setKind(input.kind());
        entity.setDistKm(input.distKm());
        entity.setDurMin(input.durMin());
        entity.setPace(input.pace());
        entity.setNotes(input.notes());
        entity.setExercises(toExerciseEntities(input.exercises()));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return toDto(sessionRepository.save(entity));
    }

    public Optional<Session> patchSession(UUID id, SessionPatch patch) {
        return sessionRepository.findById(id).map(existing -> {
            existing.setDate(patch.date() != null ? patch.date() : existing.getDate());
            existing.setCategory(patch.category() != null ? patch.category() : existing.getCategory());
            existing.setType(patch.type() != null ? patch.type() : existing.getType());
            existing.setKind(patch.kind() != null ? patch.kind() : existing.getKind());
            existing.setDistKm(patch.distKm() != null ? patch.distKm() : existing.getDistKm());
            existing.setDurMin(patch.durMin() != null ? patch.durMin() : existing.getDurMin());
            existing.setPace(patch.pace() != null ? patch.pace() : existing.getPace());
            existing.setNotes(patch.notes() != null ? patch.notes() : existing.getNotes());
            existing.setExercises(patch.exercises() != null ? toExerciseEntities(patch.exercises()) : existing.getExercises());
            existing.setUpdatedAt(Instant.now());
            return toDto(sessionRepository.save(existing));
        });
    }

    public boolean deleteSession(UUID id) {
        if (sessionRepository.existsById(id)) {
            sessionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<SleepEntry> listSleep(LocalDate from, LocalDate to) {
        return sleepEntryRepository.findByDateBetweenOrderByDateAsc(from, to).stream().map(this::toDto).toList();
    }

    public SleepEntry upsertSleep(LocalDate date, SleepEntryUpsert input) {
        var entity = sleepEntryRepository.findById(date).orElseGet(SleepEntryEntity::new);
        entity.setDate(date);
        entity.setBedtime(input.bedtime());
        entity.setWakeup(input.wakeup());
        entity.setDurationH(input.durationH());
        entity.setQuality(input.quality());
        entity.setWakeups(input.wakeups());
        entity.setNotes(input.notes());
        return toDto(sleepEntryRepository.save(entity));
    }

    public void deleteSleep(LocalDate date) {
        sleepEntryRepository.deleteById(date);
    }

    public List<WeightEntry> listWeight(LocalDate from, LocalDate to) {
        return weightEntryRepository.findByDateBetweenOrderByDateAsc(from, to).stream().map(this::toDto).toList();
    }

    public WeightEntry upsertWeight(LocalDate date, Double weightKg) {
        var entity = new WeightEntryEntity();
        entity.setDate(date);
        entity.setWeightKg(weightKg);
        return toDto(weightEntryRepository.save(entity));
    }

    public void deleteWeight(LocalDate date) {
        weightEntryRepository.deleteById(date);
    }

    public WeekStats getWeekStats(String isoWeek) {
        var weekDates = weekDates(isoWeek);
        var weekSessions = sessionRepository.findByDateIn(weekDates);
        var weekSleep = sleepEntryRepository.findByDateIn(weekDates);

        double distance = weekSessions.stream().mapToDouble(s -> s.getDistKm() == null ? 0 : s.getDistKm()).sum();
        int duration = weekSessions.stream().mapToInt(s -> s.getDurMin() == null ? 0 : s.getDurMin()).sum();
        int count = weekSessions.size();
        int calories = (int) Math.round(distance * 65);
        var dominantType = weekSessions.stream()
                .collect(Collectors.groupingBy(SessionEntity::getType, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

        int nights = weekSleep.size();
        double totalHours = weekSleep.stream().mapToDouble(s -> s.getDurationH() == null ? 0 : s.getDurationH()).sum();
        Double avgHours = nights == 0 ? null : totalHours / nights;
        Double best = nights == 0 ? null : weekSleep.stream().mapToDouble(s -> s.getDurationH() == null ? 0 : s.getDurationH()).max().orElse(0);
        Double avgQuality = nights == 0 ? null : weekSleep.stream().mapToInt(s -> s.getQuality() == null ? 0 : s.getQuality()).average().orElse(0);

        return new WeekStats(
                new TrainingStats(distance, duration, count, calories, dominantType),
                new SleepStats(nights, avgHours, nights == 0 ? null : totalHours, best, avgQuality)
        );
    }

    public WeekBundle getWeekBundle(String isoWeek) {
        var dates = weekDates(isoWeek);
        var sessionsByDate = sessionRepository.findByDateIn(dates).stream()
                .map(this::toDto)
                .collect(Collectors.groupingBy(Session::date));
        var sleepByDate = sleepEntryRepository.findByDateIn(dates).stream()
                .map(this::toDto)
                .collect(Collectors.toMap(SleepEntry::date, Function.identity()));

        var sessionsByDay = dates.stream().map(d -> sessionsByDate.getOrDefault(d, List.of())).toList();
        var sleepByDay = dates.stream().map(sleepByDate::get).toList();

        return new WeekBundle(isoWeek, dates, sessionsByDay, sleepByDay, getWeekStats(isoWeek));
    }

    public void clearWeekSessions(String isoWeek) {
        sessionRepository.deleteByDateIn(weekDates(isoWeek));
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

    private ProfileEntity getOrCreateProfile() {
        return profileRepository.findById(DEFAULT_USER_ID).orElseGet(() -> {
            var profile = new ProfileEntity();
            profile.setUserId(DEFAULT_USER_ID);
            profile.setWeightCurrentKg(75.0);
            return profileRepository.save(profile);
        });
    }

    private Profile toDto(ProfileEntity entity) {
        return new Profile(entity.getUserId(), entity.getWeightCurrentKg(), entity.getHeightCm(), entity.getWeightGoalKg());
    }

    private Session toDto(SessionEntity entity) {
        return new Session(
                entity.getId(),
                entity.getDate(),
                entity.getCategory(),
                entity.getType(),
                entity.getKind(),
                entity.getDistKm(),
                entity.getDurMin(),
                entity.getPace(),
                entity.getNotes(),
                toExerciseDtos(entity.getExercises()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private SleepEntry toDto(SleepEntryEntity entity) {
        return new SleepEntry(
                entity.getDate(),
                entity.getBedtime(),
                entity.getWakeup(),
                entity.getDurationH(),
                entity.getQuality(),
                entity.getWakeups(),
                entity.getNotes()
        );
    }

    private WeightEntry toDto(WeightEntryEntity entity) {
        return new WeightEntry(entity.getDate(), entity.getWeightKg());
    }

    private List<ExerciseEmbeddable> toExerciseEntities(List<Exercise> exercises) {
        if (exercises == null) {
            return List.of();
        }
        return exercises.stream().map(exercise -> {
            var embeddable = new ExerciseEmbeddable();
            embeddable.setName(exercise.name());
            embeddable.setSets(exercise.sets());
            embeddable.setReps(exercise.reps());
            embeddable.setWeightKg(exercise.weightKg());
            return embeddable;
        }).toList();
    }

    private List<Exercise> toExerciseDtos(List<ExerciseEmbeddable> exercises) {
        if (exercises == null) {
            return List.of();
        }
        return exercises.stream()
                .map(exercise -> new Exercise(exercise.getName(), exercise.getSets(), exercise.getReps(), exercise.getWeightKg()))
                .toList();
    }
}
