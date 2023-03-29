package co.za.imac.judge.dto;

public class FigureDTO {
    public int k_factor;
    public String description;
    public String scoring;
    public FigureDTO(int k_factor, String description, String scoring) {
        this.k_factor = k_factor;
        this.description = description.replaceAll("[^\\x00-\\x7F]", "");
        this.scoring = scoring;
    }
    public int getK_factor() {
        return k_factor;
    }
    public void setK_factor(int k_factor) {
        this.k_factor = k_factor;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getScoring() {
        return scoring;
    }
    public void setScoring(String scoring) {
        this.scoring = scoring;
    }

    
}
