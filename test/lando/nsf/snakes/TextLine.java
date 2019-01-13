package lando.nsf.snakes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

final class TextLine {

    private final String line;
    private final List<LineSection> highlightedSections = new ArrayList<>();
    
    TextLine(String line) {
        this.line = Objects.requireNonNull(line);
    }
    
    String getLine() {
        return line;
    }
    
    List<LineSection> getHighlightedSections() {
        return Collections.unmodifiableList(highlightedSections);
    }
    
    void addHighlightedSection(LineSection sect) {
        Validate.notNull(sect);
        Validate.isTrue(sect.start + sect.length <= line.length(), 
                "section going beyond end of line: " + 
                        sect.start + " + " + sect.length + " > " + line.length() + "\n" + line);
        
        highlightedSections.add(sect);
    }
}
