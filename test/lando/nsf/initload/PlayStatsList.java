package lando.nsf.initload;

import org.apache.commons.lang3.Validate;

public final class PlayStatsList {

    private int currIndex = 0;
    private final PlayStats[] stats;
    
    public PlayStatsList(int capacity) {
        Validate.isTrue(capacity > 0);
        
        this.stats = new PlayStats[capacity];
        
        for(int i = 0; i < stats.length; i++) {
            stats[i] = new PlayStats();
        }
    }
    
    public PlayStats get(int i) {
        Validate.isTrue(i >= 0 && i < currIndex);
        
        return stats[i];
    }
    
    public int size() {
        return currIndex;
    }
    
    public int capacity() {
        return stats.length;
    }
    
    public boolean isFull() {
        return currIndex >= stats.length;
    }
    
    public PlayStats next() {
        return stats[currIndex++];
    }
}
