package co.za.imac.judge.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class FigureUploadDTO {
    public float raw_score;
	public boolean box_err;
	public boolean break_err;
	@JacksonXmlProperty(isAttribute = true, localName = "index")
	public int index;
	
	public FigureUploadDTO(float raw_score, boolean box_err, boolean break_err, int index) {
		this.raw_score = raw_score;
		this.box_err = box_err;
		this.break_err = break_err;
		this.index = index;
	}
	public float getRaw_score() {
		return raw_score;
	}
	public void setRaw_score(float raw_score) {
		this.raw_score = raw_score;
	}
	public boolean isBox_err() {
		return box_err;
	}
	public void setBox_err(boolean box_err) {
		this.box_err = box_err;
	}
	public boolean isBreak_err() {
		return break_err;
	}
	public void setBreak_err(boolean break_err) {
		this.break_err = break_err;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}

	
}
