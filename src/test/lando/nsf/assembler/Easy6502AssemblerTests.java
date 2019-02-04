package lando.nsf.assembler;

import static org.junit.Assert.assertArrayEquals;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import lando.nsf.ExecutableImage;

@RunWith(Parameterized.class)
public class Easy6502AssemblerTests {
        
    @Parameters
    public static Collection<Object[]> names() {
        return Easy6502TestName.asParameters();
    }

    private final PrintStream out = System.err;
    private final Easy6502TestName name;
    
    public Easy6502AssemblerTests(Easy6502TestName name) {
        this.name = name;
    }
    
    @Test
    public void runTest() throws Exception {
        
        out.println("name: " + name);
        
        List<String> asmLines = name.assemblyLines();
        List<String> hexLines = name.hexDumpLines();
        
        ExecutableImage asmImg = new SimpleAssembler().build(0x0600, asmLines).getExecImg();
        ExecutableImage hexImg = new HexDumpReader().read(hexLines);
        
        byte[] hexBytes = hexImg.joinAllSegments();
        byte[] asmBytes = asmImg.joinAllSegments();
        
        out.println("expected:");
        printSnippet(hexBytes);
        
        out.println("computed:");
        printSnippet(asmBytes);
        
        assertArrayEquals(hexBytes, asmBytes);
    }
    
    private void printSnippet(byte[] bytes) {
        int len = Math.min(bytes.length, 50);
        
        for(int i = 0; i < len; i++) {
            out.printf(" %2x", bytes[i]&255);
        }
        
        out.println();
    }
}
