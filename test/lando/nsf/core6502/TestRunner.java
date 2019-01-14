package lando.nsf.core6502;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

import lando.nsf.ExecutableImage;
import lando.nsf.assembler.SimpleAssembler;

final class TestRunner {
    
    private static final PrintStream out = System.err;

    static void runTest(
            int startAddr,
            int maxSteps,
            List<String> lines,
            ExpectedState endState) {
        
        SimpleAssembler assembler = new SimpleAssembler();
        ExecutableImage exec = assembler.build(startAddr, lines).getExecImg();
        ByteArrayMem mem = new ByteArrayMem();
        
        mem.load(exec);
        
        //add sentinal address in interrupt handler 
        mem.write(0xFFFE, 0xFF);
        mem.write(0xFFFF, 0xFF);
        
        CPU cpu = new CPU(mem);
        
        cpu.A  = 0;
        cpu.X  = 0;
        cpu.Y  = 0;
        cpu.P  = 0b00110000;
        cpu.PC = startAddr;
        cpu.S  = 0xFF;

        int numSteps = 0;
        long numCycles = 0;
        long startNanos = System.nanoTime();
        
        boolean verbose = "true".equals(System.getProperty("lando.nsf.core6502.verbose"));
        
        while(true) {
            
            numCycles += cpu.step();
            numSteps++;
            
            if( verbose ) {
                StatusPrinter.printRegisters(cpu, mem);
            }
            
            if( numSteps > maxSteps ) {
                Assert.fail("max-steps exceeded");
            }
            
            if( cpu.PC == 0xFFFF ) {
                break;
            }
        }
        
        long endNanos = System.nanoTime();
        long numNanos = endNanos - startNanos;
        
        if( verbose ) {
            double mhz = (numCycles/1e6)/(numNanos/1e9);
            out.printf("mhz: %.2f%n", mhz);
        }
        
        if( ! endState.registersMatch(cpu) ) {
            Assert.fail("registers do not match: " + 
                    endState.registersString() + " vs " + 
                    ExpectedState.copyRegisters(cpu).registersString());
        }
        
        List<DataDiff> memDiffs = endState.dataDiffs(mem);
        
        if( ! memDiffs.isEmpty() ) {
            Assert.fail("memory diffs:\n" + 
                    memDiffs.stream()
                        .map(d -> d.toString())
                        .collect(Collectors.joining("\n")));
        }
    }
}
