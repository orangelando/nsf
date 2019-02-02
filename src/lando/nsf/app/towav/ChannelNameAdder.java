package lando.nsf.app.towav;

import java.nio.file.Path;

public final class ChannelNameAdder {

    public Path addChannelName(Path path, String name) {
        String fileName = path.getFileName().toString();
        
        int lastDot = fileName.lastIndexOf('.');
        
        if( lastDot == -1 || lastDot == 0) {
            fileName = fileName + "-" + name;
        } else {
            fileName = fileName.substring(0, lastDot) + "-" + name + fileName.substring(lastDot);
        }
        
        return path.getParent().resolve(fileName);

    }
}
