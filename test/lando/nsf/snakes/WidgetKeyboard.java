package lando.nsf.snakes;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lando.nsf.core6502.ByteArrayMem;

final class WidgetKeyboard extends KeyAdapter {
    
    private static final int SYS_LAST_KEY_ADDR = 0xff;
    private static final int MAX_KEY_QUEUE_DEPTH = 10;

    private final ByteArrayMem mem;
    private final List<Character> keyQueue = new ArrayList<>();
    
    WidgetKeyboard(ByteArrayMem mem) {
        this.mem = Objects.requireNonNull(mem);
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        
        mem.bytes[SYS_LAST_KEY_ADDR] = (byte)c;
        
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
