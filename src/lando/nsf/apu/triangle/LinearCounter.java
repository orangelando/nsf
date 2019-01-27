package lando.nsf.apu.triangle;

public final class LinearCounter {

    int reloadVal = 1;
    int counter = 0;
    boolean control = false;
    boolean halt = false;
    
    public void reload(int m) {
        counter = reloadVal;
    }
    
    public void setReload(int M) {
        reloadVal = M & 0x7F;
    }

    public void clock() {
        
        if( halt ) {
            counter = reloadVal;
        } else if( counter != 0 ) {
            --counter;
        }
        
        if( ! control ) {
            halt = false;
        }
    }
}
