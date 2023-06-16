package co.za.imac.judge.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

import co.za.imac.judge.dto.FigureDTO;
import co.za.imac.judge.dto.SettingDTO;
import co.za.imac.judge.utils.SettingUtils;

@Service
public class SequenceService {

    private static final String SEQUENCES_DAT_PATH = SettingUtils.getApplicationConfigPath() + "/sequences.dat";
    private String SEQUENCES_DAT_URL = "http://SCORE_HOST:SCORE_HTTP_PORT/scorepad/sequences.dat";

    @Autowired
    private SettingService settingService;

    public void getSequenceFileFromScore() throws MalformedURLException, IOException {
        SettingDTO settingDTO = settingService.getSettings();
        SEQUENCES_DAT_URL = SEQUENCES_DAT_URL.replace("SCORE_HOST", settingDTO.getScore_host()).replace("SCORE_HTTP_PORT", String.valueOf(settingDTO.getScore_http_port()));
        FileUtils.copyURLToFile(new URL(SEQUENCES_DAT_URL), new File(SEQUENCES_DAT_PATH));
    }

    public boolean isSequence(){
        File targetFile = new File(SEQUENCES_DAT_PATH);
        return targetFile.exists();
    }
    public  Map<String,List<FigureDTO>> getAllSequences()
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
        System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
        System.out.println("------");

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
                        figures.add(new FigureDTO(k_factor, description, scoring));
                    }
                }
                String key = _class.toUpperCase().trim() + ":" + type.toUpperCase().trim();
                figuresMap.put(key, figures);
            }
        }
       return figuresMap;
    }
    public  List<FigureDTO> getAllSequenceForClass(String _class, String type) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException{
        Map<String,List<FigureDTO>> sequencesMap = getAllSequences();
        String key = _class.toUpperCase().trim() + ":" + type.toUpperCase().trim();
        return sequencesMap.get(key);
    }
}
