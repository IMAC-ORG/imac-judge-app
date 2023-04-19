package co.za.imac.judge.dto;

public class SettingDTO {
    
    private int judge_id = 1;
    private String score_host = "192.168.1.4";
    private int score_http_port = 8181;
    private int line_number = 1;
    
    public int getLine_number() {
        return line_number;
    }

    public void setLine_number(int line_number) {
        this.line_number = line_number;
    }

    public SettingDTO() {
    }
    
    public int getJudge_id() {
        return judge_id;
    }
    public void setJudge_id(int judge_id) {
        this.judge_id = judge_id;
    }
    public String getScore_host() {
        return score_host;
    }
    public void setScore_host(String score_host) {
        this.score_host = score_host;
    }
    public int getScore_http_port() {
        return score_http_port;
    }
    public void setScore_http_port(int score_http_port) {
        this.score_http_port = score_http_port;
    }
    
}
