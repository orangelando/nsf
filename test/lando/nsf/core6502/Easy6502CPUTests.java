package lando.nsf.core6502;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import lando.nsf.ExecutableImage;
import lando.nsf.assembler.SimpleAssembler;

public class Easy6502CPUTests {
    
    private final PrintStream out = System.err;

    @Test
    public void our_first_program() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$01",
                    "STA $0200",
                    "LDA #$05",
                    "STA $0201",
                    "LDA #$08",
                    "STA $0202",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0200, 0x01,
                        0x0201, 0x05,
                        0x0202, 0x08));
    }
    
    @Test
    public void registers_and_flags() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$c0  ;Load the hex value $c0 into the A register",
                    "TAX       ;Transfer the value in the A register to X",
                    "INX       ;Increment the value in the X register",
                    "ADC #$c4  ;Add the hex value $c4 to the A register",
                    "BRK       ;Break - we're done"    
                    ),
                ExpectedState.onlyRegisters(
                        Optional.ofNullable(0x84),
                        Optional.ofNullable(0xc1),
                        Optional.ofNullable(0x00),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                        ));
    }
    
    @Test
    public void branching() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                        "  LDX #$08",
                        "decrement:",
                        "  DEX",
                        "  STX $0200",
                        "  CPX #$03",
                        "  BNE decrement",
                        "  STX $0201",
                        "  BRK"),
                ExpectedState.of(
                        Optional.empty(),
                        Optional.ofNullable(0x03),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        0x200, 0x03,
                        0x201, 0x03
                        ));
    }
    
    @Test
    public void indirect_jmp() {
        runTest(
                0x0600,
                6,
                Arrays.asList(
                        "LDA #$ff",
                        "STA $f0",
                        "LDA #$ff",
                        "STA $f1",
                        "JMP ($00f0)"
                        ),
                ExpectedState.empty());
    }
    
    @Test
    public void indexed_indirect() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                        "LDX #$01",
                        "LDA #$05",
                        "STA $01",
                        "LDA #$07",
                        "STA $02",
                        "LDY #$0a",
                        "STY $0705",
                        "LDA ($00,X)",
                        "BRK"
                        ),
                ExpectedState.onlyRegisters(
                        Optional.of(0x0a), 
                        Optional.empty(), 
                        Optional.empty(), 
                        Optional.empty(), 
                        Optional.empty(), 
                        Optional.empty()));
    }
    
    @Test
    public void indirect_indexed() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                        "LDY #$01",
                        "LDA #$03",
                        "STA $01",
                        "LDA #$07",
                        "STA $02",
                        "LDX #$0a",
                        "STX $0704",
                        "LDA ($01),Y",
                        "BRK"
                        ),
                ExpectedState.onlyRegisters(
                        Optional.of(0x0a), 
                        Optional.empty(), 
                        Optional.empty(), 
                        Optional.empty(), 
                        Optional.empty(), 
                        Optional.empty()));
    }
    
    @Test
    public void the_stack() {
        runTest(
                0x0600,
                500,
                Arrays.asList(
                        "  LDX #$00",
                        "  LDY #$00",
                        "firstloop:",
                        "  TXA",
                        "  STA $0200,Y",
                        "  PHA",
                        "  INX",
                        "  INY",
                        "  CPY #$10",
                        "  BNE firstloop ;loop until Y is $10",
                        "secondloop:",
                        "  PLA",
                        "  STA $0200,Y",
                        "  INY",
                        "  CPY #$20      ;loop until Y is $20",
                        "  BNE secondloop",
                        "  BRK"
                        ),
                ExpectedState.of(
                        Optional.empty(), //a 
                        Optional.empty(), //x 
                        Optional.empty(), //y 
                        Optional.empty(), //s 
                        Optional.empty(), 
                        Optional.empty(),
                        
                        //The final BRK pushes the PC and status register on the stack
                        //0x01FF, 0x00, 
                        //0x01FE, 0x01, 
                        //0x01FD, 0x02, 
                        0x01FC, 0x03,
                        
                        0x01FB, 0x04,
                        0x01FA, 0x05, 
                        0x01F9, 0x06, 
                        0x01F8, 0x07,
                        
                        0x01F7, 0x08, 
                        0x01F6, 0x09,
                        0x01F5, 0x0a, 
                        0x01F4, 0x0b,
                        
                        0x01F3, 0x0c, 
                        0x01F2, 0x0d,
                        0x01F1, 0x0e, 
                        0x01F0, 0x0f
                        ));
    }

    @Test
    public void jmp() {
        runTest(
                0x0600,
                500,
                Arrays.asList(
                      "  LDA #$03",
                      "  JMP there",
                      "  BRK",
                      "  BRK",
                      "  BRK",
                      "there:",
                      "  STA $0200",
                      "  BRK"),
                ExpectedState.onlyMem(
                        0x200, 0x03
                        ));

    }
    
    @Test
    public void jsr_rts() {
        runTest(
                0x0600,
                500,
                Arrays.asList(
                      "  JSR init",
                      "  JSR loop",
                      "  JSR end",
                      "  ",
                      "init:",
                      "  LDX #$00",
                      "  RTS",
                      "  ",
                      "loop:",
                      "  INX",
                      "  CPX #$05",
                      "  BNE loop",
                      "  RTS",
                      "  ",
                      "end:",
                      "  BRK"),
                ExpectedState.onlyRegisters(
                        Optional.empty(),  //a 
                        Optional.of(0x05), //x 
                        Optional.empty(),  //y 
                        Optional.empty(),  //s 
                        Optional.empty(),  //pc
                        Optional.empty()   //p
                        ));

    }
    
    private void runTest(
            int startAddr,
            int maxSteps,
            List<String> lines,
            ExpectedState endState) {
        
        SimpleAssembler assembler = new SimpleAssembler();
        ExecutableImage exec = assembler.build(startAddr, lines);
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
        
        while(true) {
            
            numCycles += cpu.step();
            numSteps++;
            
            StatusPrinter.printRegisters(cpu, mem);
            
            if( numSteps > maxSteps ) {
                Assert.fail("max-steps exceeded");
            }
            
            if( cpu.PC == 0xFFFF ) {
                break;
            }
        }
        
        long endNanos = System.nanoTime();
        long numNanos = endNanos - startNanos;
        double mhz = (numCycles/1e6)/(numNanos/1e9);
        
        out.printf("mhz: %.2f%n", mhz);
        
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
