package co.za.imac.judge.dto;

import java.util.ArrayList;
import java.util.List;

public class PilotScores {
    private String name;


    private int primary_id;

    private String _class;

    private Boolean isActive = true;
    private int activeRound = 1;
    private int activeSequence = 1;

    private List<PScore> scores = new ArrayList<>();

    public PilotScores(String name, int primary_id, String _class) {
        this.name = name;
        this.primary_id = primary_id;
        this._class = _class;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrimary_id() {
        return primary_id;
    }

    public void setPrimary_id(int primary_id) {
        this.primary_id = primary_id;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public int getActiveRound() {
        return activeRound;
    }

    public void setActiveRound(int activeRound) {
        this.activeRound = activeRound;
    }

    public int getActiveSequence() {
        return activeSequence;
    }

    public void setActiveSequence(int activeSequence) {
        this.activeSequence = activeSequence;
    }

    public List<PScore> getScores() {
        return scores;
    }

    public void setScores(List<PScore> scores) {
        this.scores = scores;
    }

    

}
