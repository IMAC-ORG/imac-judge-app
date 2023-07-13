package co.za.imac.judge.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleDTO {

    /********
     *          <sequence_id></sequence_id>
     *          <type>KNOWN</type>
     *          <class>INTERMEDIATE</class>
     *          <min_round>1</min_round>
     *          <max_round>20</max_round>
     *          <description></description>
     *          <short_desc></short_desc>   <----  Ignore this one, will be deprecated...
     *          <lang>en</lang>             <----  Ignore this one, it's wrong anyway...
     */

    private String sequence_id;              // Currently unused, will be a UUID.
    private String type;
    private String comp_class;
    private Integer min_round;
    private Integer max_round;
    private String description;
    private String short_desc;               // Eventually will remove this...
    private String lang;                     // Eventually will remove this...
    private Map<Integer, FigureDTO> figures;
    //private List<FigureDTO> figures;

    public ScheduleDTO() {
        figures = new HashMap<>();
    }

    public String getSequence_id() { return sequence_id; }
    public void setSequence_id(String sequence_id) { this.sequence_id = sequence_id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getComp_class() { return comp_class; }
    public void setComp_class(String comp_class) { this.comp_class = comp_class; }
    public Integer getMin_round() { return min_round; }
    public void setMin_round(Integer min_round) { this.min_round = min_round; }
    public Integer getMax_round() { return max_round; }
    public void setMax_round(Integer max_round) { this.max_round = max_round; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getShort_desc() { return short_desc; }
    public void setShort_desc(String short_desc) { this.short_desc = short_desc; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public Map<Integer, FigureDTO> getFigures() { return figures; }
    public void setFigures(Map<Integer, FigureDTO> figures) { this.figures = figures; }

}
