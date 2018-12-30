package lando.nsf.assembler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Easy6502TestName {
    
    our_first_program,
    instructions,
    branching,
    relative,
    indirect,
    indexed_indirect,
    indirect_indexed,
    stack,
    jmp,
    jsr_rts,
    defines,
    snakes;
    
    public static Collection<Object[]> asParameters() {
        return Stream.of(values())
                .map(name -> new Object[] {name})
                .collect(Collectors.toList());
    }
    
    List<String> assemblyLines() throws Exception {
        return readLines(resourcePath("s"));
    }
    
    List<String> hexDumpLines() throws Exception {
        return readLines(resourcePath("hexdump"));
    }

    private String resourcePath(String extension) {
        return "/" + getClass().getPackage().getName().replace('.', '/') + "/easy6502/" + name() + "." + extension;
    }

    private List<String> readLines(String resourcePath) throws Exception {
        try(InputStream is = getClass().getResourceAsStream(resourcePath);
            InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(r)) {
            
            List<String> lines = new ArrayList<>();
            
            for(String line = br.readLine(); line != null; line = br.readLine()) {
                lines.add(line);
            }
            
            return lines;
        }
    }
    

}
