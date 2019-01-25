package lando.nsf.apu;

public final class LengthCounter {

    private static final int[][] LENGTH_LOOKUP_TABLE = {
            {0x0A, 0xFE},   // 0
            {0x14, 0x02},   // 1
            {0x28, 0x04},   // 2
            {0x50, 0x06},   // 3
            {0xA0, 0x08},   // 4
            {0x3C, 0x0A},   // 5
            {0x0E, 0x0C},   // 6
            {0x1A, 0x0E},   // 7
            {0x0C, 0x10},   // 8
            {0x18, 0x12},   // 9
            {0x30, 0x14},   // A
            {0x60, 0x16},   // B
            {0xC0, 0x18},   // C
            {0x48, 0x1A},   // D
            {0x10, 0x1C},   // E
            {0x20, 0x1E}};  // F
    
    private int count = 0;
    private boolean disabled = true;
    
    /**
     * The bottom 3 bits are ignored.
     * 
     */
    public void reload(int registerVal) {
        if( disabled ) {
            return;
        }
        
        registerVal >>= 3;
        
        count = LENGTH_LOOKUP_TABLE[(registerVal>>1)&0xF][registerVal&1];
    }
    
    public void setDisabled(boolean flag) {
        if( flag ) {
            setDisabled();
        } else {
            clearDisabled();
        }
    }
    
    public void setDisabled() {
        disabled = true;
        count = 0;
    }
    
    public void clearDisabled() {
        disabled = false;
    }
    
    public void clock() {
        if( disabled ) {
            return;
        }
        
        if( count > 0 ) {
            --count;
        }
    }
    
    public int getCount() {
        return count;
    }
}
