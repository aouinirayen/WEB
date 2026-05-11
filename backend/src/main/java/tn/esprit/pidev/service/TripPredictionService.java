package tn.esprit.pidev.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.entity.Trip;
import tn.esprit.pidev.entity.Schedule;
import tn.esprit.pidev.repository.TripRepository;
import tn.esprit.pidev.repository.ScheduleRepository;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPredictionService {

    private final TripRepository tripRepository;
    private final ScheduleRepository scheduleRepository;

    public Map<String, Object> predictDemand() {
        List<Trip> allTrips = tripRepository.findAll();
        List<Schedule> allSchedules = scheduleRepository.findAll();

        // 1 — Analyze trips by day of week
        Map<String, Long> tripsByDay = analyzeTripsByDay(allTrips);

        // 2 — Analyze delay patterns
        Map<String, Double> avgDelayByDay =
                analyzeDelayByDay(allTrips);

        // 3 — Analyze completion rate
        Map<String, Double> completionRateByDay =
                analyzeCompletionRate(allTrips);

        // 4 — Find peak hours
        Map<String, Long> tripsByHour =
                analyzeTripsByHour(allTrips);

        // 5 — Generate recommendations
        List<Map<String, String>> recommendations =
                generateRecommendations(
                        tripsByDay,
                        avgDelayByDay,
                        completionRateByDay,
                        tripsByHour,
                        allSchedules
                );

        // 6 — Find busiest and quietest days
        String busiestDay = tripsByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String quietestDay = tripsByDay.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // 7 — Calculate overall stats
        long totalTrips = allTrips.size();
        long completedTrips = allTrips.stream()
                .filter(t -> Boolean.TRUE.equals(t.getCompleted()))
                .count();
        double overallCompletionRate = totalTrips > 0
                ? (double) completedTrips / totalTrips * 100 : 0;
        double avgDelay = allTrips.stream()
                .filter(t -> t.getDelayMinutes() != null)
                .mapToInt(Trip::getDelayMinutes)
                .average()
                .orElse(0);

        // Build response
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTripsAnalyzed", totalTrips);
        result.put("overallCompletionRate",
                Math.round(overallCompletionRate * 10.0) / 10.0);
        result.put("averageDelayMinutes",
                Math.round(avgDelay * 10.0) / 10.0);
        result.put("busiestDay", busiestDay);
        result.put("quietestDay", quietestDay);
        result.put("tripsByDay", tripsByDay);
        result.put("avgDelayByDay", avgDelayByDay);
        result.put("completionRateByDay", completionRateByDay);
        result.put("tripsByHour", tripsByHour);
        result.put("recommendations", recommendations);
        result.put("generatedAt",
                LocalDateTime.now().toString());

        return result;
    }

    private Map<String, Long> analyzeTripsByDay(
            List<Trip> trips) {
        Map<String, Long> result = new LinkedHashMap<>();
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY",
                "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        for (String day : days) result.put(day, 0L);

        trips.stream()
                .filter(t -> t.getDepartureTime() != null)
                .forEach(t -> {
                    String day = t.getDepartureTime()
                            .getDayOfWeek().name();
                    result.merge(day, 1L, Long::sum);
                });
        return result;
    }

    private Map<String, Double> analyzeDelayByDay(
            List<Trip> trips) {
        Map<String, Double> result = new LinkedHashMap<>();
        Map<String, List<Integer>> delaysByDay = new HashMap<>();

        trips.stream()
                .filter(t -> t.getDepartureTime() != null
                        && t.getDelayMinutes() != null)
                .forEach(t -> {
                    String day = t.getDepartureTime()
                            .getDayOfWeek().name();
                    delaysByDay.computeIfAbsent(
                                    day, k -> new ArrayList<>())
                            .add(t.getDelayMinutes());
                });

        delaysByDay.forEach((day, delays) -> {
            double avg = delays.stream()
                    .mapToInt(Integer::intValue)
                    .average().orElse(0);
            result.put(day,
                    Math.round(avg * 10.0) / 10.0);
        });
        return result;
    }

    private Map<String, Double> analyzeCompletionRate(
            List<Trip> trips) {
        Map<String, Double> result = new LinkedHashMap<>();
        Map<String, List<Trip>> tripsByDay = trips.stream()
                .filter(t -> t.getDepartureTime() != null)
                .collect(Collectors.groupingBy(t ->
                        t.getDepartureTime().getDayOfWeek().name()));

        tripsByDay.forEach((day, dayTrips) -> {
            long completed = dayTrips.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getCompleted()))
                    .count();
            double rate = (double) completed
                    / dayTrips.size() * 100;
            result.put(day,
                    Math.round(rate * 10.0) / 10.0);
        });
        return result;
    }

    private Map<String, Long> analyzeTripsByHour(
            List<Trip> trips) {
        Map<String, Long> result = new LinkedHashMap<>();
        // Initialize all hours
        for (int i = 0; i < 24; i++) {
            result.put(String.format("%02d:00", i), 0L);
        }
        trips.stream()
                .filter(t -> t.getDepartureTime() != null)
                .forEach(t -> {
                    String hour = String.format("%02d:00",
                            t.getDepartureTime().getHour());
                    result.merge(hour, 1L, Long::sum);
                });
        return result;
    }

    private List<Map<String, String>> generateRecommendations(
            Map<String, Long> tripsByDay,
            Map<String, Double> avgDelayByDay,
            Map<String, Double> completionRateByDay,
            Map<String, Long> tripsByHour,
            List<Schedule> schedules) {

        List<Map<String, String>> recommendations =
                new ArrayList<>();

        // Find busiest day — suggest more trips
        tripsByDay.forEach((day, count) -> {
            if (count > getAverage(tripsByDay) * 1.3) {
                Map<String, String> rec = new LinkedHashMap<>();
                rec.put("type", "INCREASE_TRIPS");
                rec.put("priority", "HIGH");
                rec.put("day", day);
                rec.put("message",
                        "Increase number of trips on " + day
                                + " — " + count
                                + " trips recorded (above average).");
                rec.put("icon", "📈");
                recommendations.add(rec);
            }
        });

        // Find quietest day — suggest reducing trips
        tripsByDay.forEach((day, count) -> {
            if (count < getAverage(tripsByDay) * 0.7
                    && count > 0) {
                Map<String, String> rec = new LinkedHashMap<>();
                rec.put("type", "REDUCE_TRIPS");
                rec.put("priority", "LOW");
                rec.put("day", day);
                rec.put("message",
                        "Consider reducing trips on " + day
                                + " — only " + count
                                + " trips recorded (below average).");
                rec.put("icon", "📉");
                recommendations.add(rec);
            }
        });

        // High delay days — suggest more vehicles
        avgDelayByDay.forEach((day, avgDelay) -> {
            if (avgDelay > 10) {
                Map<String, String> rec = new LinkedHashMap<>();
                rec.put("type", "HIGH_DELAY");
                rec.put("priority", "HIGH");
                rec.put("day", day);
                rec.put("message",
                        "Average delay of " + avgDelay
                                + " min on " + day
                                + " — consider adding more vehicles "
                                + "or adjusting schedules.");
                rec.put("icon", "⚠️");
                recommendations.add(rec);
            }
        });

        // Low completion rate
        completionRateByDay.forEach((day, rate) -> {
            if (rate < 70) {
                Map<String, String> rec = new LinkedHashMap<>();
                rec.put("type", "LOW_COMPLETION");
                rec.put("priority", "HIGH");
                rec.put("day", day);
                rec.put("message",
                        "Only " + rate + "% of trips completed "
                                + "on " + day
                                + " — investigate causes and "
                                + "improve reliability.");
                rec.put("icon", "🚨");
                recommendations.add(rec);
            }
        });

        // Peak hour analysis
        String peakHour = tripsByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        long peakCount = tripsByHour.getOrDefault(peakHour, 0L);
        if (peakCount > 0) {
            Map<String, String> rec = new LinkedHashMap<>();
            rec.put("type", "PEAK_HOUR");
            rec.put("priority", "MEDIUM");
            rec.put("day", "ALL");
            rec.put("message",
                    "Peak hour is " + peakHour
                            + " with " + peakCount
                            + " trips — ensure maximum vehicle "
                            + "availability during this time.");
            rec.put("icon", "🕐");
            recommendations.add(rec);
        }

        // Weekend vs weekday
        long weekdayTrips = tripsByDay.entrySet().stream()
                .filter(e -> !e.getKey().equals("SATURDAY")
                        && !e.getKey().equals("SUNDAY"))
                .mapToLong(Map.Entry::getValue).sum();
        long weekendTrips =
                tripsByDay.getOrDefault("SATURDAY", 0L)
                        + tripsByDay.getOrDefault("SUNDAY", 0L);

        if (weekendTrips < weekdayTrips * 0.3
                && weekendTrips > 0) {
            Map<String, String> rec = new LinkedHashMap<>();
            rec.put("type", "WEEKEND_LOW");
            rec.put("priority", "MEDIUM");
            rec.put("day", "WEEKEND");
            rec.put("message",
                    "Weekend trips are significantly lower than "
                            + "weekdays — consider adjusting weekend "
                            + "schedules to match actual demand.");
            rec.put("icon", "📅");
            recommendations.add(rec);
        }

        if (recommendations.isEmpty()) {
            Map<String, String> rec = new LinkedHashMap<>();
            rec.put("type", "ALL_GOOD");
            rec.put("priority", "LOW");
            rec.put("day", "ALL");
            rec.put("message",
                    "Trip patterns look balanced. "
                            + "No immediate action required.");
            rec.put("icon", "✅");
            recommendations.add(rec);
        }

        return recommendations;
    }

    private double getAverage(Map<String, Long> map) {
        return map.values().stream()
                .mapToLong(Long::longValue)
                .average().orElse(0);
    }
}