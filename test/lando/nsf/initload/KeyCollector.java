package lando.nsf.initload;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

final class KeyCollector extends KeyAdapter {
    
    private static final int MAX_KEY_QUEUE_DEPTH = 10;

    private final List<Character> keyQueue = new ArrayList<>();
    
    KeyCollector() {
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        
        if( ! e.isShiftDown() ) {
        }
        
        keyQueue.add(c);
        
        while( keyQueue.size() > MAX_KEY_QUEUE_DEPTH ) {
            keyQueue.remove(0);
        }
    }
    
    public List<Character> drainKeyQueue() {
        List<Character> q = new ArrayList<>(keyQueue);
        
        keyQueue.clear();
        
        return q;
    }
}
