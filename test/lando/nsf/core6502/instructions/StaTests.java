package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;

public class StaTests {
    
    @Test
    public void sta_zero_page() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "STA $08",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0008, 0x10));
    }
    
    @Test
    public void sta_zero_page_x() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "LDX #$80",
                    "STA $ff,X",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x007f, 0x10));
    }
    
    @Test
    public void sta_absolute() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "STA $0345",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0345, 0x10));
    }
    
    @Test
    public void sta_absolute_x() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "LDX #$01",
                    "STA $3101,X",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x3102, 0x10));
    }
    
    @Test
    public void sta_absolute_y() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "LDY #$01",
                    "STA $3101,Y",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x3102, 0x10));
    }
    
    @Test
    public void sta_indexed_indirect_x() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    
                    "LDA #$02",
                    "STA $10",
                    "LDA #$31",
                    "STA $11",
                    
                    "LDA #$bc",
                    "LDX #$30",
                    "STA ($e0,X)",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0010, 0x02,
                        0x0011, 0x31,
                        0x3102, 0xbc));
    }
    
    @Test
    public void sta_indirect_indexed_y() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    
                    "LDA #$01",
                    "STA $10",
                    "LDA #$31",
                    "STA $11",
                    
                    "LDA #$bc",
                    "LDY #$01",
                    "STA ($10),Y",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0010, 0x01,
                        0x0011, 0x31,
                        
                        0x3102, 0xbc));
    }

}
