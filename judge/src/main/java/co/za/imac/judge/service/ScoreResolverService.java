package co.za.imac.judge.service;

import co.za.imac.judge.dto.PScore;
import co.za.imac.judge.dto.Pilot;
import co.za.imac.judge.dto.PilotScores;
import co.za.imac.judge.utils.ContestClasses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure-logic resolver helpers. Reads pilot data via PilotService; emits the
 * detection payload for /api/scores/mismatches and the per-pilot eligibility
 * checks consumed by the resolver backends.
 *
 * <p>Mirrors the SequenceValidationService pattern: no own state, autowires
 * the data services it needs.
 */
@Service
public class ScoreResolverService {

    @Autowired
    private PilotService pilotService;

    /**
     * Builds the detection payload for /api/scores/mismatches: a map with
     * "mismatches" (anomalous groups only) and "allGroups" (every (class,
     * roundType) group with at least one pilot). Both arrays sorted by
     * competition class order, then KNOWN, UNKNOWN, FREESTYLE within a class.
     */
    public Map<String, Object> getMismatches() throws IOException, ParserConfigurationException, SAXException {
        List<Pilot> pilots = pilotService.getPilots();

        List<Map<String, Object>> mismatches = new ArrayList<>();
        List<Map<String, Object>> allGroups = new ArrayList<>();

        Map<String, List<Pilot>> pilotsByClass = new HashMap<>();
        for (Pilot pilot : pilots) {
            pilotsByClass.computeIfAbsent(pilot.getClassString().toUpperCase(), k -> new ArrayList<>()).add(pilot);
        }

        String[] classRoundTypes = {"KNOWN", "UNKNOWN"};
        for (Map.Entry<String, List<Pilot>> entry : pilotsByClass.entrySet()) {
            for (String roundType : classRoundTypes) {
                checkAndAddMismatches(entry.getValue(), roundType, entry.getKey(), mismatches, allGroups);
            }
        }

        // FREESTYLE: cross-class virtual group
        List<Pilot> freestylePilots = pilots.stream()
                .filter(p -> Boolean.TRUE.equals(p.getFreestyle()))
                .toList();
        if (!freestylePilots.isEmpty()) {
            checkAndAddMismatches(freestylePilots, "FREESTYLE", "FREESTYLE", mismatches, allGroups);
        }

        // Sort by class order, then KNOWN -> UNKNOWN -> FREESTYLE within a class.
        Comparator<Map<String, Object>> byClassOrder = (g1, g2) -> {
            int classCmp = Integer.compare(
                    ContestClasses.orderIndex((String) g1.get("className")),
                    ContestClasses.orderIndex((String) g2.get("className")));
            if (classCmp != 0) return classCmp;
            String t1 = (String) g1.get("roundType");
            String t2 = (String) g2.get("roundType");
            int o1 = "KNOWN".equals(t1) ? 0 : "UNKNOWN".equals(t1) ? 1 : 2;
            int o2 = "KNOWN".equals(t2) ? 0 : "UNKNOWN".equals(t2) ? 1 : 2;
            return Integer.compare(o1, o2);
        };
        mismatches.sort(byClassOrder);
        allGroups.sort(byClassOrder);

        Map<String, Object> result = new HashMap<>();
        result.put("mismatches", mismatches);
        result.put("allGroups", allGroups);
        return result;
    }

    /**
     * Counts the number of distinct rounds the pilot has scored for the
     * given round type.
     */
    public int countRoundsForType(PilotScores scores, String roundType) {
        if (scores == null || scores.getScores() == null) return 0;

        Set<Integer> rounds = new HashSet<>();
        for (PScore score : scores.getScores()) {
            if (roundType.equalsIgnoreCase(score.getType())) {
                rounds.add(score.getRound());
            }
        }
        return rounds.size();
    }

    /**
     * Returns the round number of the pilot's unresolved missing seq 2, or null.
     * A round R qualifies iff the pilot has scored seq 1 of (R, KNOWN) but not
     * seq 2, AND some other pilot in the same group has scored seq 2 of
     * (R, KNOWN).
     */
    public Integer findUnresolvedMissingSeq2Round(Pilot pilot, List<Pilot> groupPilots) throws IOException {
        PilotScores scores = pilotService.getPilotScores(pilot);
        if (scores == null || scores.getScores() == null) return null;

        Set<Integer> roundsWithSeq1 = new HashSet<>();
        Set<Integer> roundsWithSeq2 = new HashSet<>();
        for (PScore s : scores.getScores()) {
            if (!"KNOWN".equalsIgnoreCase(s.getType())) continue;
            if (s.getSequence() == 1) roundsWithSeq1.add(s.getRound());
            else if (s.getSequence() == 2) roundsWithSeq2.add(s.getRound());
        }

        for (Integer round : roundsWithSeq1) {
            if (roundsWithSeq2.contains(round)) continue;
            for (Pilot peer : groupPilots) {
                if (peer.getPrimary_id().equals(pilot.getPrimary_id())) continue;
                PilotScores peerScores = pilotService.getPilotScores(peer);
                if (peerScores == null || peerScores.getScores() == null) continue;
                for (PScore ps : peerScores.getScores()) {
                    if ("KNOWN".equalsIgnoreCase(ps.getType())
                            && ps.getRound() == round
                            && ps.getSequence() == 2) {
                        return round;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Builds the (class, roundType) detection group and adds it to allGroups.
     * Adds to mismatches when the group has a count gap (max - min &gt;= 2) or
     * any pilot has an incomplete round. A spread of 0 or 1 with no incomplete
     * rounds is a valid state and yields no mismatch entry.
     */
    private void checkAndAddMismatches(List<Pilot> pilots, String roundType, String className,
                                       List<Map<String, Object>> mismatches,
                                       List<Map<String, Object>> allGroups) throws IOException {
        if (pilots.isEmpty()) return;

        List<Map<String, Object>> pilotEntries = new ArrayList<>();
        int minCount = Integer.MAX_VALUE;
        int maxCount = Integer.MIN_VALUE;

        for (Pilot pilot : pilots) {
            PilotScores scores = pilotService.getPilotScores(pilot);
            int count = countRoundsForType(scores, roundType);
            Integer incompleteRound = "KNOWN".equalsIgnoreCase(roundType)
                    ? findUnresolvedMissingSeq2Round(pilot, pilots)
                    : null;

            Map<String, Object> entry = new HashMap<>();
            entry.put("pilotId", pilot.getPrimary_id());
            entry.put("name", pilot.getName());
            entry.put("roundCount", count);
            entry.put("incompleteRound", incompleteRound);
            pilotEntries.add(entry);

            if (count < minCount) minCount = count;
            if (count > maxCount) maxCount = count;
        }

        pilotEntries.sort((a, b) ->
                ((String) a.get("name")).compareToIgnoreCase((String) b.get("name")));

        int spread = maxCount - minCount;
        boolean hasIncomplete = pilotEntries.stream().anyMatch(e -> e.get("incompleteRound") != null);

        List<String> anomalies = new ArrayList<>();
        if (spread >= 2) anomalies.add("count_gap");
        if (hasIncomplete) anomalies.add("incomplete_round");

        Map<String, Object> group = new HashMap<>();
        group.put("className", className);
        group.put("roundType", roundType);
        group.put("minCount", minCount);
        group.put("maxCount", maxCount);
        group.put("spread", spread);
        group.put("anomalies", anomalies);
        group.put("pilots", pilotEntries);

        allGroups.add(group);
        if (!anomalies.isEmpty()) {
            mismatches.add(group);
        }
    }
}
