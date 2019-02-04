package lando.nsf.gui;

import java.awt.Font;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import javax.swing.JTextArea;

@SuppressWarnings("serial")
public final class TextLines extends JTextArea {
    
    private final Supplier<List<TextLine>> linesFn;
    private final TextLinesRenderer renderer;
    
    public TextLines(Supplier<List<TextLine>> linesFn) {
        super(80, 20);
        
        this.linesFn = Objects.requireNonNull(linesFn);
        this.renderer = new TextLinesRenderer(this);
        
        this.setEditable(false);
        this.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }
    
    public void updateTxt() throws Exception {
        
        List<TextLine> lines = linesFn.get();
        renderer.render(lines);
        
        this.repaint();
    }
}
