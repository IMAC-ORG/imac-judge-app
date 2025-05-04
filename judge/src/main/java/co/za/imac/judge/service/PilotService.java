package co.za.imac.judge.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import co.za.imac.judge.controller.RootController;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import co.za.imac.judge.dto.CompDTO;
import co.za.imac.judge.dto.FigureUploadDTO;
import co.za.imac.judge.dto.FiguresUploadDTO;
import co.za.imac.judge.dto.FlightUploadDTO;
import co.za.imac.judge.dto.FlightsUploadDTO;
import co.za.imac.judge.dto.PScore;
import co.za.imac.judge.dto.Pilot;
import co.za.imac.judge.dto.PilotScoreDTO;
import co.za.imac.judge.dto.PilotScores;
import co.za.imac.judge.dto.SettingDTO;
import co.za.imac.judge.utils.SettingUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Service
public class PilotService {
    private static final Logger logger =
            LoggerFactory.getLogger(PilotService.class);

    private static final String PILOT_SCORE_DIR = SettingUtils.getApplicationConfigPath() + "/pilots/scores/";
    private static final String PILOT_DAT_PATH = SettingUtils.getApplicationConfigPath() + "/pilots.dat";
    private String PILOT_DAT_URL = "http://SCORE_HOST:SCORE_HTTP_PORT/scorepad/pilots.dat";
    private String SCORE_UPLOAD_URL = "http://SCORE_HOST:SCORE_HTTP_PORT/scorepadupload/";
    @Autowired
    private CompService compService;
    @Autowired
    private SettingService settingService;

    public void getPilotsFileFromScore() throws MalformedURLException, IOException {
        SettingDTO settingDTO = settingService.getSettings();
        PILOT_DAT_URL = PILOT_DAT_URL.replace("SCORE_HOST", settingDTO.getScore_host()).replace("SCORE_HTTP_PORT", String.valueOf(settingDTO.getScore_http_port()));
        FileUtils.copyURLToFile(new URL(PILOT_DAT_URL), new File(PILOT_DAT_PATH),1000,1000);
    }

    public boolean isPilots(){
        File targetFile = new File(PILOT_DAT_PATH);
        return targetFile.exists();
    }

    public List<Pilot> getPilots() throws ParserConfigurationException, SAXException, IOException {
        if(!isPilots()){
            getPilotsFileFromScore();
        }
        // Get Document Builder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        // parse XML file
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(new File(PILOT_DAT_PATH));
        // optional, but recommended
        // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();
        logger.debug ("Pilot file root Element :" + doc.getDocumentElement().getNodeName());
        // get <staff>
        NodeList list = doc.getElementsByTagName("pilots");
        NodeList newList = (NodeList) list.item(0);

        logger.debug ("Pilot count: " + newList.getLength());
        List<Pilot> pilots = new ArrayList<>();
        for (int temp = 0; temp < newList.getLength(); temp++) {
            Node node = newList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String index = element.getAttribute("index");
                int primary_id = Integer.parseInt(element.getElementsByTagName("primary_id").item(0).getTextContent());
                int secondary_id = 0;
                int comp_id = 0;
                if (element.getElementsByTagName("secondary_id").item(0).hasChildNodes() == true && 
                    element.getElementsByTagName("secondary_id").item(0).getTextContent().length() > 0) {
                    secondary_id = Integer
                            .parseInt(element.getElementsByTagName("secondary_id").item(0).getTextContent());
                }
                if (element.getElementsByTagName("comp_id").item(0).hasChildNodes() == true && 
                    element.getElementsByTagName("comp_id").item(0).getTextContent().length() > 0) {
                    comp_id = Integer.parseInt(element.getElementsByTagName("comp_id").item(0).getTextContent());
                }
                String name = element.getElementsByTagName("name").item(0).getTextContent();
                String addr1 = element.getElementsByTagName("addr1").item(0).getTextContent();
                String addr2 = element.getElementsByTagName("addr2").item(0).getTextContent();
                String airplane = element.getElementsByTagName("airplane").item(0).getTextContent();
                Boolean missing_pilot_panel = Boolean
                        .parseBoolean(element.getElementsByTagName("airplane").item(0).getTextContent());
                String comments = element.getElementsByTagName("comments").item(0).getTextContent();
                Boolean active = Boolean.parseBoolean(element.getElementsByTagName("active").item(0).getTextContent());
                Boolean freestyle = Boolean
                        .parseBoolean(element.getElementsByTagName("freestyle").item(0).getTextContent());
                Boolean spread_spectrum = Boolean
                        .parseBoolean(element.getElementsByTagName("spread_spectrum").item(0).getTextContent());
                int frequency = Integer.parseInt(element.getElementsByTagName("frequency").item(0).getTextContent());

                NodeList classesList = doc.getElementsByTagName("classes");
                Element class_element = (Element) classesList.item(Integer.parseInt(index));
                String _class = class_element.getElementsByTagName("class").item(0).getTextContent();
                Pilot pilot = new Pilot(freestyle, comments, addr2, addr1, _class, index, active, comp_id, frequency,
                        spread_spectrum, secondary_id, airplane, name, missing_pilot_panel, primary_id);
                pilots.add(pilot);
                logger.debug(new Gson().toJson(pilot));
            }
        }
        return pilots;
    }

    public Pilot getPilot(int pilot_id) throws ParserConfigurationException, SAXException, IOException {
        List<Pilot> pilots = getPilots();
        return pilots.stream().filter(pilot -> pilot.getPrimary_id() == pilot_id).findFirst().orElse(null);
    }

    public void setupPilotScores() throws ParserConfigurationException, SAXException, IOException {
        List<Pilot> pilots = getPilots();
        pilots.parallelStream().forEach(pilot -> {
            createPilotScoreFile(pilot);
        });
    }

    public void createPilotScoreFile(Pilot pilot) {
        try {
            String filepath = PILOT_SCORE_DIR + pilot.getPrimary_id() + ".json";
            File pilotScoreFile = new File(filepath);
            pilotScoreFile.getParentFile().mkdirs();
            boolean createResult = pilotScoreFile.createNewFile();
            if (createResult)
                logger.debug("File created: " + filepath);
            else
                logger.warn("File exists: " + filepath);

            FileWriter fw = new FileWriter(pilotScoreFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            PilotScores pilotScores = new PilotScores(pilot.getName(), pilot.getPrimary_id(), pilot.getClassString(), settingService.getSettings().getJudge_id());
            bw.write(new Gson().toJson(pilotScores));
            bw.close();
        } catch (Exception e) {
            logger.error("Could not create pilot file for pilot " + pilot.getName() + " (" + pilot.getPrimary_id() + ").");
            logger.error("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public PilotScores getPilotScores(Pilot pilot) throws IOException {
        String filepath = PILOT_SCORE_DIR + pilot.getPrimary_id() + ".json";
        File pilotScoreFile = new File(filepath);
        if (!pilotScoreFile.exists()) {
            createPilotScoreFile(pilot);
        }
        Path path = Paths.get(filepath);
        String pilotScoresJson = Files.readAllLines(path).get(0);
        return new Gson().fromJson(pilotScoresJson, PilotScores.class);
    }

    public PilotScores savePilotScoresToFile(PilotScores scores) throws IOException {
        String filepath = PILOT_SCORE_DIR + scores.getPrimary_id() + ".json";
        File pilotScoreFile = new File(filepath);
        if (!pilotScoreFile.exists()) {
            try {
                createPilotScoreFile(getPilot(scores.getPrimary_id()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //
        FileWriter fw = new FileWriter(pilotScoreFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(new Gson().toJson(scores));
        bw.close();
        return scores;
    }

    public PilotScores submitScore(PilotScoreDTO pilotScoreDTO)
            throws ParserConfigurationException, SAXException, IOException {
        Pilot pilot = getPilot(pilotScoreDTO.getPrimary_id());
        PilotScores pilotScores = getPilotScores(pilot);
        PScore newScore = new PScore(pilotScoreDTO.getRound(), pilotScoreDTO.getSequence(), pilotScoreDTO.getScores(),pilotScoreDTO.getType().toUpperCase());
        if (isNewScore(pilotScores, newScore)) {
            List<PScore> currentScores = pilotScores.getScores();
            currentScores.add(newScore);
            pilotScores.setScores(currentScores);
            // update next seq / round
            pilotScores = setNextEvent(pilotScores,pilotScoreDTO.getType().toUpperCase());
            pilotScores.setActiveRoundType(pilotScoreDTO.getType().toUpperCase());
            // save to file
            pilotScores = savePilotScoresToFile(pilotScores);
        } else {
            // TODO update existing score
        }
        return pilotScores;
    }

    public boolean isNewScore(PilotScores pilotScores, PScore score) {
        for (PScore savedScore : pilotScores.getScores()) {
            if (savedScore.getRound() == score.getRound() && savedScore.getSequence() == score.getSequence()) {
                return false;
            }
        }
        return true;
    }

    public PilotScores setNextEvent(PilotScores pilotScore , String roundType) throws IOException {

        // fetch comp details
        CompDTO compDTO = compService.getComp();
        int compSequences = 0;
        if(roundType.equalsIgnoreCase("KNOWN")){
            compSequences = compDTO.getSequences();
        }
        if(roundType.equalsIgnoreCase("UNKNOWN")){
            compSequences = compDTO.getUnknown_sequences();
        }
        // check if theres a next sequence
        if (pilotScore.getActiveSequence() < compSequences) {
            // there is a next sequence for round increment and return
            pilotScore.setActiveSequence((pilotScore.getActiveSequence() + 1));
            return pilotScore;
        }
        if (pilotScore.getActiveSequence() == compSequences) {

            // Just reset the seq back to 1 and increment the round counter.
            pilotScore.setActiveRound((pilotScore.getActiveRound() + 1));
            pilotScore.setActiveSequence(1);
            return pilotScore;


            /************************************
             * The logic below is a little off...
             * It works but There's really no reason to keep a max rounds option.
             * Just let the pilots keep flying until you are done...
             *
            // sequences for round is complete advanced round
            if (pilotScore.getActiveRound() < compDTO.getRounds()) {
                // theres a next round advance round
                pilotScore.setActiveRound((pilotScore.getActiveRound() + 1));
                pilotScore.setActiveSequence(1);
                return pilotScore;
            }
            // its the last round of last sequence the pilot is done
            if (pilotScore.getActiveRound() == compDTO.getRounds()) {
                pilotScore.setIsActive(false);
            }
            return pilotScore;
             */


        }
        return pilotScore;
    }

    public void syncPilotsToScoreWebService() throws IOException, ParserConfigurationException, SAXException, UnirestException {
        SettingDTO settingDTO = settingService.getSettings();
        List<FlightUploadDTO> flight = new ArrayList<>();
        int index = 0;
        List<PilotScores> pilotScores = new ArrayList<>();
        List<Pilot> pilots = getPilots();
        for (Pilot pilot : pilots) {
            pilotScores.add(getPilotScores(pilot));
        }
        for (PilotScores pilotScore : pilotScores) {
            if (pilotScore.getScores().size() > 0) {
                for (PScore score : pilotScore.getScores()) {
                    List<FigureUploadDTO> figureScores = new ArrayList<>();
                    for (int i = 0; i < score.getScores().length; i++) {
                        float fscore = score.getScores()[i];
                        boolean break_err = false;
                        boolean box_err = false;
                        boolean not_observed = false;
                        if (fscore == -1) {
                            fscore = 0;
                            break_err = true;
                        }
                        if (fscore == -2) {
                            fscore = 0;
                            not_observed = true;
                        }
                        FigureUploadDTO figureScore = new FigureUploadDTO(fscore, box_err, break_err,not_observed, i);
                        figureScores.add(figureScore);
                    }
                    FiguresUploadDTO figures = new FiguresUploadDTO(figureScores);
                    // create FlightUploadDTO
                    FlightUploadDTO flightUploadDTO = new FlightUploadDTO(pilotScore.getPrimary_id(), score.getType(), 
                            score.getRound(),
                            score.getSequence(), pilotScore.getJudge_id(), false, figures, index, settingDTO.getLine_number());
                    flight.add(flightUploadDTO);
                    index++;
                }
            }
            // create final stupid wrapper
        }

        FlightsUploadDTO flightsUploadDTO = new FlightsUploadDTO(flight, settingDTO.getLine_number());

        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(flightsUploadDTO);

        //SettingDTO settingDTO = settingService.getSettings();
        SCORE_UPLOAD_URL = SCORE_UPLOAD_URL.replace("SCORE_HOST", settingDTO.getScore_host()).replace("SCORE_HTTP_PORT", String.valueOf(settingDTO.getScore_http_port()));

        String flights_dat_file_name = "LINE" + settingDTO.getLine_number() + "_JUDGE" + settingDTO.getJudge_id() + "_flights.dat";
        String flights_dat_file_path = PILOT_SCORE_DIR + flights_dat_file_name;
        File flights_dat_file = new File(flights_dat_file_path);
        FileWriter fw = new FileWriter(flights_dat_file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(xml);
        bw.close();
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post(SCORE_UPLOAD_URL)
                .header("Accept-Language", "en-au")
                .field("uploadtype", "flightdata")
                .field("keepbackup", "false")
                .field("submit", "submit")
                .field("file", flights_dat_file)
                .field("filename", flights_dat_file_name)
                .asString();
        if(response.getStatus() != 200){
            throw new IOException("Unable to sync to Score");
        }
        System.out.println(response.getStatus());
        System.out.println(xml);
    }
}
