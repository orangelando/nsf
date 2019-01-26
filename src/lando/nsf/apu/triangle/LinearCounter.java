package lando.nsf.apu.triangle;

public class LinearCounter {

    int reloadVal = 1;
    int counter = 0;
    boolean control = false;
    boolean halt = false;
    
    public void reload(int m) {
        
    }
    
    public void setReload(int m) {
        
    }

    public void clock() {
        
        if( halt ) {
            counter = reloadVal;
        } else if( counter > 0 ) {
            --counter;
        }
        
        if( ! control ) {
            halt = false;
        }
    }
}
