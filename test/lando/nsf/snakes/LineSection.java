package lando.nsf.snakes;

import org.apache.commons.lang3.Validate;

final class LineSection {

    final int start;
    final int length;
    final int status;
    
    LineSection(int start, int length, int status) {
        Validate.isTrue(start >= 0);
        Validate.isTrue(length > 0);
        Validate.isTrue(status >= 0);
        
        this.start = start;
        this.length = length;
        this.status = status;
    }
}
