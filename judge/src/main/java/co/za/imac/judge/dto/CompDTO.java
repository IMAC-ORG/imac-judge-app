package co.za.imac.judge.dto;

public class CompDTO {
    
    public int rounds;
    public int sequences;

    public CompDTO(int rounds, int sequences) {
        this.rounds = rounds;
        this.sequences = sequences;
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
