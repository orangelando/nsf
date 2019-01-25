package lando.nsf.apu;

public final class PulseSequencer {
    
    private static final int[][] SEQUENCES = {
            {0, 1, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 1, 1},
    };
    
    private int step = 0;
    private int duty = 0;
    
    void reset() {
        step = 0;
    }
    
    void setDuty(int d) {
        this.duty = d & 3;
    }
    
    void clock() {
        step = (step + 1)&7;
    }
    
    int getOutput() {
        return SEQUENCES[duty][step];
    }
}
