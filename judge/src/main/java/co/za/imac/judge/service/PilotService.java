package co.za.imac.judge.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

import co.za.imac.judge.dto.Pilot;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


@Service
public class PilotService {

    public List<Pilot> getPilots() throws ParserConfigurationException, SAXException, IOException {
        // Get Document Builder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        // parse XML file
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(  new File("/tmp/pilots.dat"));
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
                int primary_id  = Integer.parseInt(element.getElementsByTagName("primary_id").item(0).getTextContent());
                int secondary_id = 0;
                int comp_id = 0 ;
                if(element.getElementsByTagName("secondary_id").item(0).getTextContent().length() > 0){
                    secondary_id =  Integer.parseInt(element.getElementsByTagName("secondary_id").item(0).getTextContent());
                }
                if(element.getElementsByTagName("secondary_id").item(0).getTextContent().length() > 0){
                     comp_id =  Integer.parseInt(element.getElementsByTagName("comp_id").item(0).getTextContent());
                }
                String name = element.getElementsByTagName("name").item(0).getTextContent();
                String addr1 = element.getElementsByTagName("addr1").item(0).getTextContent();
                String addr2 = element.getElementsByTagName("addr2").item(0).getTextContent();
                String airplane = element.getElementsByTagName("airplane").item(0).getTextContent();
                Boolean missing_pilot_panel = Boolean.parseBoolean(element.getElementsByTagName("airplane").item(0).getTextContent());
                String comments = element.getElementsByTagName("comments").item(0).getTextContent();
                Boolean active = Boolean.parseBoolean(element.getElementsByTagName("active").item(0).getTextContent());
                Boolean freestyle = Boolean.parseBoolean(element.getElementsByTagName("freestyle").item(0).getTextContent());
                Boolean spread_spectrum = Boolean.parseBoolean(element.getElementsByTagName("spread_spectrum").item(0).getTextContent());
                int frequency =  Integer.parseInt(element.getElementsByTagName("frequency").item(0).getTextContent());
                
                NodeList classesList = doc.getElementsByTagName("classes");
                Element class_element = (Element) classesList.item(0);
                System.out.println();
                String _class = class_element.getElementsByTagName("class").item(0).getTextContent();

                Pilot pilot = new Pilot(freestyle, comments, addr2, addr1, _class, index, active, comp_id, frequency, spread_spectrum, secondary_id, airplane, name, missing_pilot_panel, primary_id);
                pilots.add(pilot);
                System.out.println(new Gson().toJson(pilot));

            }
        }
        return pilots;
    }

    public Pilot getPilot(int pilot_id) throws ParserConfigurationException, SAXException, IOException{
        List<Pilot> pilots = getPilots();
        return pilots.stream().filter(pilot -> pilot.getPrimary_id() == pilot_id).findFirst().orElse(null);
    }
}
