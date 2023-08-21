package co.za.imac.judge.service;

import co.za.imac.judge.dto.CompDTO;
import co.za.imac.judge.dto.FigureDTO;
import co.za.imac.judge.dto.ScheduleDTO;
import co.za.imac.judge.dto.SettingDTO;
import co.za.imac.judge.utils.SettingUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleService {

    /***********
     * This is the schedule service.   It provides an interface to the schedule data.
     * Schedules are sometimes called Sequences, but rounds have sequences as well,
     * so it's better for the flown schedule to have it's own name.
     *
     */

    private static final Logger logger =
            LoggerFactory.getLogger(ScheduleService.class);

    private Map<Integer, ScheduleDTO> schedules = null;  // The schedules for this instance.

    // Score still calls them sequences...  :-)
    private static final String SEQUENCES_DAT_PATH = SettingUtils.getApplicationConfigPath() + "/sequences.dat";
    private String SEQUENCES_DAT_URL = "http://SCORE_HOST:SCORE_HTTP_PORT/scorepad/sequences.dat";

    @Autowired
    private SettingService settingService;

    public ScheduleService() {
    }

    public Map<Integer, ScheduleDTO> getSchedules() {
        if (schedules == null) {
            this.populateSequences();
        }
        return schedules;
    }

    public ScheduleService setSchedules(Map<Integer, ScheduleDTO> schedules) {
        this.schedules = schedules;
        return this;
    }

    public void populateSequences() {
        try {
            this.loadSequenceFileIntoSchedules();
        } catch( Exception e ) {
            try {
                logger.error("There was an error loading the schedules data from the sequence.dat file..");
                e.printStackTrace();
            } catch (Exception logger_e) {
                logger_e.printStackTrace();
            }
        }
    }

    public void getSequenceFileFromScore() throws MalformedURLException, IOException {
        logger.info("Loading sequences from Score.");
        SettingDTO settingDTO = settingService.getSettings();
        SEQUENCES_DAT_URL = SEQUENCES_DAT_URL.replace("SCORE_HOST", settingDTO.getScore_host()).replace("SCORE_HTTP_PORT", String.valueOf(settingDTO.getScore_http_port()));
        FileUtils.copyURLToFile(new URL(SEQUENCES_DAT_URL), new File(SEQUENCES_DAT_PATH),1000,1000);
    }

    /************
     *
     *  Not sure we need this now..
     *
    public boolean addFigureToSchedule(Integer schedId, Integer figNum, FigureDTO fig) {

        if (fig == null || schedId == null || fig == null) {
            return false;
        }

        if (this.schedules == null) {
            this.schedules = new HashMap<>();
        }

        if(schedules.get(schedId) == null) {
            // No such schedule
            logger.info("Schedule not found: " + schedId);
            return false;
        }

        schedules.get(schedId).getFigures().put(figNum, fig);
        return true;
    }
    /*******/

    public FigureDTO getScheduleFigureByFigNum(Integer schedId, Integer figNum) {
        try {
            return schedules.get(schedId).getFigures().get(figNum);
        } catch (NullPointerException ne) {
            try {
                logger.error("Could not get figure " + figNum + " for schedule " + schedId);
                ne.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isSequence(){
        File targetFile = new File(SEQUENCES_DAT_PATH);
        return targetFile.exists();
    }

    public boolean loadSequenceFileIntoSchedules()
            throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {

        logger.info ("(Re)Loading sequences.");
        this.schedules = new HashMap<>();
        if(!isSequence()){
            getSequenceFileFromScore();
        }

        // Get Document Builder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        // parse XML file
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse( new File(SEQUENCES_DAT_PATH));
        doc.getDocumentElement().normalize();
        logger.info("Sequence file root Element: " + doc.getDocumentElement().getNodeName());

        NodeList sequenceList = doc.getElementsByTagName("sequence");
        //NodeList newList = (NodeList) list.item(0);
        logger.debug("Sequences length: " + sequenceList.getLength());
        //Map<String,List<FigureDTO>> figuresMap = new HashMap<>();
        for (int temp = 0; temp < sequenceList.getLength(); temp++) {
            Node node = sequenceList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String index = element.getAttribute("index");
                String type = element.getElementsByTagName("type").item(0).getTextContent();
                String _class =  "";
                try {
                    _class = element.getElementsByTagName("class").item(0).getTextContent();
                } catch (Exception e) {
                    _class = "FREESTYLE";
                }
                logger.debug ("Sequence number: " + index + " Class: " + _class + " Type: " + type);
                ScheduleDTO newSched = new ScheduleDTO();
                newSched.setComp_class(_class);
                newSched.setType(type);
                newSched.setSequence_id(element.getElementsByTagName("sequence_id").item(0).getTextContent());
                newSched.setMax_round(Integer.parseInt(element.getElementsByTagName("max_round").item(0).getTextContent()));
                newSched.setMin_round(Integer.parseInt(element.getElementsByTagName("min_round").item(0).getTextContent()));
                newSched.setDescription(element.getElementsByTagName("description").item(0).getTextContent());
                newSched.setShort_desc(element.getElementsByTagName("short_desc").item(0).getTextContent());
                newSched.setLang(element.getElementsByTagName("lang").item(0).getTextContent());
                Map<Integer, FigureDTO>figures = new HashMap<>();
                Element figuresList = (Element) element.getElementsByTagName("figures").item(0);
                logger.debug("Figure count for seq: " + figuresList.getElementsByTagName("figure").getLength());
                for (int temp2 = 0; temp2 < figuresList.getElementsByTagName("figure").getLength(); temp2++) {
                    Node node2 = figuresList.getElementsByTagName("figure").item(temp2);
                    if (node2.getNodeType() == Node.ELEMENT_NODE) {
                        Element figureElement = (Element) node2;
                        String description = figureElement.getElementsByTagName("description").item(0).getTextContent();
                        String spokenDescription = figureElement.getElementsByTagName("spoken_desc").item(0).getTextContent();
                        int k_factor = Integer
                                .parseInt(figureElement.getElementsByTagName("k_factor").item(0).getTextContent());
                        String scoring = figureElement.getElementsByTagName("scoring").item(0).getTextContent();
                        figures.put((temp2+1), new FigureDTO((temp2+1), k_factor, description, scoring, spokenDescription));
                    }
                }
                newSched.setFigures(figures);
                logger.debug("Adding Schedule " + temp + " to the list.");
                schedules.put(temp, newSched);
            }
        }
        return true;
    }

    public  Map<String,List<FigureDTO>> getAllSequences_old()
            throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {

        if(!isSequence()){
            getSequenceFileFromScore();
        }
        // Get Document Builder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        // parse XML file
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse( new File(SEQUENCES_DAT_PATH));
        doc.getDocumentElement().normalize();
        System.out.println("Sequence file root Element :" + doc.getDocumentElement().getNodeName());

        NodeList list = doc.getElementsByTagName("sequences");
        NodeList newList = (NodeList) list.item(0);
        System.out.println(newList.getLength());
        Map<String,List<FigureDTO>> figuresMap = new HashMap<>();
        for (int temp = 0; temp < newList.getLength(); temp++) {
            Node node = newList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String index = element.getAttribute("index");
                String type = element.getElementsByTagName("type").item(0).getTextContent();
                String _class =  "";
                try {
                     _class = element.getElementsByTagName("class").item(0).getTextContent();
                } catch (Exception e) {
                     _class = "FREESTYLE";
                }
                System.out.println(index);
                System.out.println(_class);
                // System.out.println(_class);
                List<FigureDTO> figures  = new ArrayList<>();
                Element figuresList = (Element) element.getElementsByTagName("figures").item(0);
                System.out.println(figuresList.getElementsByTagName("figure").getLength());
                for (int temp2 = 0; temp2 < figuresList.getElementsByTagName("figure").getLength(); temp2++) {
                    Node node2 = figuresList.getElementsByTagName("figure").item(temp2);
                    if (node2.getNodeType() == Node.ELEMENT_NODE) {
                        Element figureElement = (Element) node2;
                        String description = figureElement.getElementsByTagName("description").item(0).getTextContent();
                        int k_factor = Integer
                                .parseInt(figureElement.getElementsByTagName("k_factor").item(0).getTextContent());
                        String scoring = figureElement.getElementsByTagName("scoring").item(0).getTextContent();
                        figures.add(new FigureDTO((temp2+1), k_factor, description, scoring));
                    }
                }
                String key = _class.toUpperCase().trim() + ":" + type.toUpperCase().trim();
                figuresMap.put(key, figures);
            }
        }
       return figuresMap;
    }
    public  List<FigureDTO> getAllSequenceForClass(String _class, String type) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException{
        Map<String,List<FigureDTO>> sequencesMap = this.getAllSequences_old();
        String key = _class.toUpperCase().trim() + ":" + type.toUpperCase().trim();
        return sequencesMap.get(key);
    }
}
