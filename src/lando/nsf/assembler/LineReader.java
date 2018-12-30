package lando.nsf.assembler;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.Validate;

public final class LineReader {

    public void read(List<String> lines, Consumer<String> callback) {
        Validate.notNull(lines);
        Validate.notEmpty(lines);
        Validate.noNullElements(lines);
        
        int lineNum = 1;
        
        for(String line: lines) {
            try {
                callback.accept(line);
            } catch(Exception e) {
                throw new RuntimeException("error on line " + lineNum, e);
            } finally {
                lineNum++;
            }
        }
    }
}
