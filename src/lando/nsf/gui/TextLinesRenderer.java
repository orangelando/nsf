package lando.nsf.gui;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JTextArea;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

final class TextLinesRenderer {

    private static final HighlightPainter painter(int rgb) {
        return new DefaultHighlighter.DefaultHighlightPainter(new Color(rgb));
    }
    
    private static final HighlightPainter STACK_HIGHLIGHT = painter(0xFF_FF_00);
    
    private static final HighlightPainter[] HIGHLIGHT_PAINTERS = {
            painter(0xFF_FF_00), //  0 - generic highlight
            painter(0x88_88_88), //  1 -       |      | diff |
            painter(0x88_88_FF), //  2 -       | read |      |
            painter(0x00_00_FF), //  3 -       | read | diff |
            painter(0xFF_88_88), //  4 - write |      |      |
            painter(0xFF_00_00), //  5 - write |      | diff |
            painter(0xFF_00_FF), //  6 - write | read |      |
            painter(0xFF_88_FF), //  7 - write | read | diff |
        };    
    

    private final JTextArea txt;
    
    TextLinesRenderer(JTextArea txt) {
        this.txt = Objects.requireNonNull(txt);
    }
    
    void render(List<TextLine> lines) throws Exception {
        Highlighter hl = txt.getHighlighter();
        hl.removeAllHighlights();
        
        txt.setText(lines.stream()
                .map(t -> t.getLine())
                .collect(Collectors.joining("\n")));
     
        int offset = 0;
        
        for(TextLine line: lines) {
        
            for(LineSection sect: line.getHighlightedSections()) {
                
                HighlightPainter p;
                
                if( (sect.status & MemoryMonitor.STATUS_STACK) != 0 ) {
                    p = STACK_HIGHLIGHT;
                } else {
                    p = HIGHLIGHT_PAINTERS[ (sect.status>>1) & 7 ];
                }
                
                hl.addHighlight(
                        offset + sect.start, 
                        offset + sect.start + sect.length, //inclusive end  
                        p);        
            }
            
            offset += line.getLine().length() + 1; //1 for the \n char
        }
    }
}
