package co.za.imac.judge.dto;

public class CompDTO {
    
    public int rounds;
    public int sequences;
    public int unknown_sequences = 0;
    public int judge_id = 1;

    public int getJudge_id() {
        return judge_id;
    }
    public void setJudge_id(int judge_id) {
        this.judge_id = judge_id;
    }
    public int getUnknown_sequences() {
        return unknown_sequences;
    }
    public void setUnknown_sequences(int unknown_sequences) {
        this.unknown_sequences = unknown_sequences;
    }
    public CompDTO(int rounds, int sequences, int unknown_sequences) {
        this.rounds = rounds;
        this.sequences = sequences;
        this.unknown_sequences = unknown_sequences;
    }
    public int getRounds() {
        return rounds;
    }
    public void setRounds(int rounds) {
        this.rounds = rounds;
    }
    public int getSequences() {
        return sequences;
    }
    public void setSequences(int sequences) {
        this.sequences = sequences;
    }
}
