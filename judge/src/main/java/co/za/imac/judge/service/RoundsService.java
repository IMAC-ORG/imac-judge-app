package co.za.imac.judge.service;

import java.io.*;
import java.util.*;

import co.za.imac.judge.dto.RoundDTO;
import co.za.imac.judge.dto.RoundsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import co.za.imac.judge.utils.SettingUtils;

@Service
public class RoundsService {

    private static final Logger logger =
            LoggerFactory.getLogger(RoundsService.class);

    private final String ROUNDS_FILE_NAME = SettingUtils.getApplicationConfigPath() + "/rounds.json";
    private RoundsDTO roundsDTO;

    public RoundsService() {
        try {
            setupRounds(0); // It's probably null at the start!
        } catch (Exception e) {
            try {
                logger.error("There was an error loading the rounds data..");
                e.printStackTrace();
            } catch (Exception logger_e) {
                logger_e.printStackTrace();
            }
        }
    }

    public Integer getScoringRoundNum() {
        return this.roundsDTO.getScoringRoundNum();
    }

    public RoundDTO getScoringRound() {
        if (this.roundsDTO.getScoringRoundNum() == null) {
            return null;
        }
        try {
            return this.getRoundByID(this.roundsDTO.getScoringRoundNum());
        } catch (Exception e) {
            try {
                logger.error("Could not get the scoring round: " + this.roundsDTO.getScoringRoundNum());
                e.printStackTrace();
            } catch (Exception logger_e) {
                logger_e.printStackTrace();
            }
        }
        return null;
    }

    public RoundDTO getRoundByID(Integer round_id) {
        if (round_id == null) {
            logger.warn("Trying to get a round with a null id.  Bailing on that chief.");
            return null;
        }

        try {
            for (RoundDTO rnd : roundsDTO.getRounds()) {
                if (round_id.intValue() == rnd.getRound_id().intValue() ) {
                    return rnd;
                }
            }
            return null;  // Not found.
        } catch (NullPointerException e) {
            try {
                logger.error("Could not cpmpare " + round_id + " to  a round with a null ID.  This isn't good.");
                e.printStackTrace();
            } catch (Exception logger_e) {
                logger_e.printStackTrace();
            }
        }
        return null;
    }


    public Map<String, Object> activateRoundForScoring(Integer i) {
        // Choose the round to score.   Reset the others..
        Map<String, Object> res = new HashMap<String, Object>();
        if (i == null) {
            res.put("message", "You must supply a round ID.");
            res.put("success", Boolean.FALSE);
            logger.error((String) res.get("message"));
            return res;
        }
        RoundDTO r = getRoundByID(i);
        if (r == null) {
            res.put("message", "Could not find round to score");
            res.put("success", Boolean.FALSE);
            logger.error((String) res.get("message"));
            return res;
        }

        this.roundsDTO.setScoringRoundNum(i);

        for (RoundDTO rnd : roundsDTO.getRounds()) {
            if ("F".equalsIgnoreCase(rnd.getPhase())) {
                // Pause the other round that is flying...
                // We could check if they have zero scores.  Then phase could be "U".
                rnd.setPhase("P");
            }
        }
        r.setPhase("F");
        res.put("message", "");
        res.put("success", Boolean.TRUE);

        return res;
    }

    public Map<String, Object> closeRound(Integer i) {
        // Close the round...
        Map<String, Object> res = new HashMap<String, Object>();
        if (i == null) {
            res.put("message", "You must supply a round ID.");
            res.put("success", Boolean.FALSE);
            logger.error((String) res.get("message"));
            return res;
        }
        RoundDTO r = this.getRoundByID(i);
        if (r == null) {
            res.put("message", "Could not find round to score");
            res.put("success", Boolean.FALSE);
            logger.error((String) res.get("message"));
            return res;
        }

        if (!"F".equalsIgnoreCase(r.getPhase()) || r.getRound_id() != this.getScoringRound().getRound_id()) {
            res.put("message", "This is not the active round.");
            res.put("success", Boolean.FALSE);
            logger.error((String) res.get("message"));
            return res;
        }

        r.setPhase("D");
        this.roundsDTO.setScoringRoundNum(null);
        res.put("message", "Closed round " + i);
        res.put("success", Boolean.TRUE);
        logger.info((String) res.get("message"));
        return res;
    }


    public boolean isScoringRound() throws IOException {
        // There are rounds set to 'F'.
        /**********
         *  Too complicated this way...   Just see if we have an active scoring round.

        if (roundsDTO == null && ! this.setupRoundsDTO(compService.getComp().getComp_id())) {
            logger.error("Could not set up the rounds file!!!  This is bad!");
            return false;
        }

        for (Round r : roundsDTO.getRounds()) {
            if (new String("F").equalsIgnoreCase(r.getPhase()) ) {
                return true;
            }
        }
        return false;

         */
        logger.info("Is there an active round? : " + (this.getScoringRound() != null));
        return (this.getScoringRound() != null);
    }

    public boolean setupRounds(int comp_id) {
        if (roundsDTO == null ) {
            // We haven't loaded the data yet...   Lets go.

            roundsDTO = new RoundsDTO();

            File targetFile = new File(ROUNDS_FILE_NAME);
            if (targetFile.exists()) {
                try {
                    this.loadRoundsFromFile();
                } catch (IOException e) {
                    logger.error("Could not load round data.  " + e.getMessage());
                    return false;
                }
            } else {
                // Lets create one.
                this.saveRoundsToFile();
            }
        }
        roundsDTO.setComp_id(comp_id);
        return true;
    }


    public boolean loadRoundsFromFile() throws IOException{

        FileInputStream inputStream = new FileInputStream(ROUNDS_FILE_NAME);
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        if (roundsDTO != null)
            logger.warn("Overwriting existing round data with data from file!");
        roundsDTO = new Gson().fromJson(resultStringBuilder.toString(),RoundsDTO.class);

        if (roundsDTO.getComp_id() == null) {
            roundsDTO.setComp_id(0);
        }
        return true;
    }

    public boolean saveRoundsToFile() {
        File roundsFile = new File(ROUNDS_FILE_NAME);
        try {
            if (!roundsFile.exists()) {
                roundsFile.getParentFile().mkdirs();
                roundsFile.createNewFile();
            }
            FileWriter fw = new FileWriter(roundsFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(new Gson().toJson(roundsDTO));
            bw.close();

        } catch (Exception e) {
            try {
                logger.error("Could not save round data.  " + e.getMessage());
                e.printStackTrace();
            } catch (Exception logger_e) {
                logger_e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public List<RoundDTO> getRounds() {
        return roundsDTO.getRounds();
    }

    public List<RoundDTO>  getFlyingRounds() {
        // Iterate over the rounds list and add the ones which are 'Flying' to the list
        List<RoundDTO> activeRounds = new ArrayList<>();
        for (RoundDTO rnd : roundsDTO.getRounds()) {
            if ("F".equalsIgnoreCase(rnd.getPhase())) {
                activeRounds.add(rnd);
            }
        }
        return activeRounds;
    }


    public RoundDTO addRound(RoundDTO round) {
        return addRound(round, null, null);
    }

    public RoundDTO addRound(RoundDTO round, Integer round_id, Integer round_num) {
        /******************************************************
         * Take a round and add it to the list.
         *
         *  Arguments:
         *   roundObj   - The main argument is the round object to be created.    It should be valid...
         *               round.type must be one of: KNOWN, UNKNOWN, FREESTYLE (but has potential to be
         *               extended in the future).
         *
         *               round.comp_class currently should be one of:  BASIC, PORTSMAN, INTERMEDIATE,
         *               ADVANCED, UNLIMITED.  However there's nothing wrong with INVITATIONAL
         *               (for Tuscan) or F3A-SPORTSMAN (etc).  In the case of multi class types (freestyle) this arg
         *               can be null.
         *
         *   round_id   - If known is an integer denoting the unique round identifier for this device.   If omitted
         *                (null) then the next available will be chosen.
         *
         *   round_num  - If known is an integer denoting the unique round number within the class/type.  If omitted
         *                (null) then the next available will be chosen.

         */

        if (round.getType() == null) {
            logger.error("The round is invalid - it must supply a round type.");
            return null;
        }

        if (!round.getType().equalsIgnoreCase("FREESTYLE") && round.getComp_class() == null) {
            logger.error("Round types apart from Freestyle must define a class");
            return null;
        }

        if (round.getRound_id() == null)
            round.setRound_id(getNextFreeRoundID());
        if (round.getRound_num() == null)
            round.setRound_num(getNextFreeRoundNum(round.getComp_class(), round.getType()));

        roundsDTO.getRounds().add(round);
        return round;
    }

    public int getNextFreeRoundID() {
        // Iterate over the rounds and get the highest ID.

        int currentTopRoundId = -1; // Round ID might potentially start at 0.
        int thisRoundID;

            for (RoundDTO rnd : roundsDTO.getRounds()) {
                try {
                    thisRoundID = rnd.getRound_id().intValue();
                    if (thisRoundID > currentTopRoundId)
                        currentTopRoundId = thisRoundID;  // New max!
                } catch (NullPointerException e) {
                    try {
                        logger.error("Came accross a round with a null ID.  This isn't good.");
                        e.printStackTrace();
                    } catch (Exception logger_e) {
                        logger_e.printStackTrace();
                    }
                }
            }
        return(currentTopRoundId + 1);
    }

    public int getNextFreeRoundNum(String comp_class, String type) {
        // Round numbers are unique within the class and type (Freestyle is cross class so it is special)
        int currentTopRound = 0;  // Start at 0!   But round numbers start at 1

        for (RoundDTO rnd : roundsDTO.getRounds()) {

            boolean compClassMatch = false;
            boolean typeMatch = type.equalsIgnoreCase(rnd.getType());
            if (type.equalsIgnoreCase("FREESTYLE")) {
                // freestyle does not have class so now just check
                compClassMatch = true;
            } else {
                compClassMatch = comp_class.equals(rnd.getComp_class());
            }

            if (compClassMatch && typeMatch) {
                // We have the round!    Increment nextFreeRound if it's higher.
                if (rnd.getRound_num().intValue() > currentTopRound)
                    currentTopRound = rnd.getRound_num();
            }
        }
        return (currentTopRound + 1);
    }
}
