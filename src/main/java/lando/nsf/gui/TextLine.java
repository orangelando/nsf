package lando.nsf.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class TextLine {

    private final String line;
    private final List<LineSection> highlightedSections = new ArrayList<>();
    
    public TextLine(String line) {
        this.line = Objects.requireNonNull(line);
    }
    
    public String getLine() {
        return line;
    }
    
    public List<LineSection> getHighlightedSections() {
        return Collections.unmodifiableList(highlightedSections);
    }
    
    public void addHighlightedSection(LineSection sect) {
        Validate.notNull(sect);
        Validate.isTrue(sect.start + sect.length <= line.length(), 
                "section going beyond end of line: " + 
                        sect.start + " + " + sect.length + " > " + line.length() + "\n" + line);
        
        highlightedSections.add(sect);
    }
}
