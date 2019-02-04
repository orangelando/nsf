package lando.nsf.snakes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.JPanel;

import lando.nsf.cpu.ByteArrayMem;

@SuppressWarnings("serial")
final class WidgetPixels extends JPanel {
    
    private static final int START_ADDR = 0x0200;
    
    private static final Color[] COLORS = Arrays.asList(
       0x000000, 0xffffff, 0x880000, 0xaaffee,
       0xcc44cc, 0x00cc55, 0x0000aa, 0xeeee77,
       0xdd8855, 0x664400, 0xff7777, 0x333333,
       0x777777, 0xaaff66, 0x0088ff, 0xbbbbbb)
       .stream()
       .map(c -> new Color(c))
       .toArray(Color[]::new);
    
    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;
    private static final int PIXEL_SIZE = 14;
    
    private final ByteArrayMem mem;
    
    WidgetPixels(ByteArrayMem mem) {
        super();
        
        this.mem = Objects.requireNonNull(mem);
        
        this.setSize(500, 500);
    }
    
    @Override
    public void paint(Graphics g0) {
        Graphics2D g = (Graphics2D)g0;
        
        for(int y = 0; y < HEIGHT; y++) {
            for(int x = 0; x < WIDTH; x++) {
                Color c = COLORS[(int)mem.bytes[START_ADDR + y*WIDTH + x]&0xF];
                g.setPaint(c);
                g.fillRect(x*PIXEL_SIZE, y*PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
                
                g.setPaint(Color.DARK_GRAY);
                g.drawRect(x*PIXEL_SIZE, y*PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
            }
        }
    }
}
