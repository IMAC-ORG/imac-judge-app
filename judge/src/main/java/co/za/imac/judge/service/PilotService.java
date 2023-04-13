package co.za.imac.judge.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

import co.za.imac.judge.dto.CompDTO;
import co.za.imac.judge.dto.FigureUploadDTO;
import co.za.imac.judge.dto.FiguresUploadDTO;
import co.za.imac.judge.dto.FlightUploadDTO;
import co.za.imac.judge.dto.FlightsUploadDTO;
import co.za.imac.judge.dto.PScore;
import co.za.imac.judge.dto.Pilot;
import co.za.imac.judge.dto.PilotScoreDTO;
import co.za.imac.judge.dto.PilotScores;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Service
public class PilotService {

    private static final String PILOT_SCORE_DIR = "/tmp/pilots/scores/";
    @Autowired
    private CompService compService;

    public List<Pilot> getPilots() throws ParserConfigurationException, SAXException, IOException {
        // Get Document Builder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        // parse XML file
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(new File("/tmp/pilots.dat"));
        // optional, but recommended
        // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();
        System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
        System.out.println("------");
        // get <staff>
        NodeList list = doc.getElementsByTagName("pilots");
        NodeList newList = (NodeList) list.item(0);
        System.out.println(newList.getLength());
        List<Pilot> pilots = new ArrayList<>();
        for (int temp = 0; temp < newList.getLength(); temp++) {
            Node node = newList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String index = element.getAttribute("index");
                int primary_id = Integer.parseInt(element.getElementsByTagName("primary_id").item(0).getTextContent());
                int secondary_id = 0;
                int comp_id = 0;
                if (element.getElementsByTagName("secondary_id").item(0).getTextContent().length() > 0) {
                    secondary_id = Integer
                            .parseInt(element.getElementsByTagName("secondary_id").item(0).getTextContent());
                }
                if (element.getElementsByTagName("secondary_id").item(0).getTextContent().length() > 0) {
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
                Element class_element = (Element) classesList.item(0);
                System.out.println();
                String _class = class_element.getElementsByTagName("class").item(0).getTextContent();

                Pilot pilot = new Pilot(freestyle, comments, addr2, addr1, _class, index, active, comp_id, frequency,
                        spread_spectrum, secondary_id, airplane, name, missing_pilot_panel, primary_id);
                pilots.add(pilot);
                System.out.println(new Gson().toJson(pilot));

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
            pilotScoreFile.createNewFile();

            FileWriter fw = new FileWriter(pilotScoreFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            PilotScores pilotScores = new PilotScores(pilot.getName(), pilot.getPrimary_id(), pilot.getClassString());
            bw.write(new Gson().toJson(pilotScores));
            bw.close();
        } catch (Exception e) {
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
        PScore newScore = new PScore(pilotScoreDTO.getRound(), pilotScoreDTO.getSequence(), pilotScoreDTO.getScores());
        if (isNewScore(pilotScores, newScore)) {
            List<PScore> currentScores = pilotScores.getScores();
            currentScores.add(newScore);
            pilotScores.setScores(currentScores);
            // update next seq / round
            pilotScores = setNextEvent(pilotScores);
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

    public PilotScores setNextEvent(PilotScores pilotScore) throws IOException {

        // fetch comp details
        CompDTO compDTO = compService.getComp();

        // check if theres a next sequence
        if (pilotScore.getActiveSequence() < compDTO.getSequences()) {
            // there is a next sequence for round increment and return
            pilotScore.setActiveSequence((pilotScore.getActiveSequence() + 1));
            return pilotScore;
        }
        if (pilotScore.getActiveSequence() == compDTO.getSequences()) {
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

        }
        return pilotScore;
    }

    public void syncPilotsToScoreWebService(PilotScores pilotScore) throws JsonProcessingException{
        List<FlightUploadDTO> flight = new ArrayList<>();
        int index = 0 ; 
        for(PScore score : pilotScore.getScores()){
            List<FigureUploadDTO> figureScores = new ArrayList<>();
            for(int i = 0; i< score.getScores().length; i++){
                float fscore = score.getScores()[i];
                boolean break_err = false;
                boolean box_err = false;
                if(fscore == -1){
                    fscore = 0; 
                    break_err = true;
                }
                if(fscore == -2){
                    fscore = 0; 
                }
                FigureUploadDTO figureScore = new FigureUploadDTO(fscore, break_err, box_err, i);
                figureScores.add(figureScore);
            }
            FiguresUploadDTO figures = new FiguresUploadDTO(figureScores);
            //create FlightUploadDTO
            FlightUploadDTO flightUploadDTO = new FlightUploadDTO(pilotScore.getPrimary_id(), "KNOWN", score.getRound(), score.getSequence(), 1, false, figures, index);
            flight.add(flightUploadDTO);
            index ++;
        }
         //create final stupid wrapper
        FlightsUploadDTO flightsUploadDTO = new FlightsUploadDTO(flight);
        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(flightsUploadDTO);
        System.out.println(xml);
    }
}
