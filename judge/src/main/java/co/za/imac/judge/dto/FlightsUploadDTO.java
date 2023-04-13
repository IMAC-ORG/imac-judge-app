package co.za.imac.judge.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "flights")
public class FlightsUploadDTO {
    public int flightline = 1;
    @JacksonXmlElementWrapper(useWrapping = false)
	public FlightUploadDTO flight[];


    public FlightsUploadDTO() {
        this.flight = new FlightUploadDTO[1];
        this.flight[0] = new FlightUploadDTO();
    }

    
}
