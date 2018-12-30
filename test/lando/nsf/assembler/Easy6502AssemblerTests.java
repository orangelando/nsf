package lando.nsf.assembler;

import static org.junit.Assert.assertArrayEquals;

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

    private final Easy6502TestName name;
    
    public Easy6502AssemblerTests(Easy6502TestName name) {
        this.name = name;
    }
    
    @Test
    public void runTest() throws Exception {
        
        List<String> asmLines = name.assemblyLines();
        List<String> hexLines = name.hexDumpLines();
        
        ExecutableImage asmImg = new SimpleAssembler().build(asmLines, 0x0600);
        ExecutableImage hexImg = new HexDumpReader().read(hexLines);
        
        assertArrayEquals(
                hexImg.joinAllSegments(), 
                asmImg.joinAllSegments());
    }
}
