package lando.nsf.core6502;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

public class LdaTests {

    @Test
    public void lda_immediate() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "BRK"),
                ExpectedState.accumAndMem(0x10));
    }
    
    @Test
    public void lda_zero_page() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "STA $08",
                    "LDA #$00",
                    "LDA $08",
                    "BRK"),
                ExpectedState.accumAndMem(0x10,
                        0x0008, 0x10));
    }
    
    @Test
    public void lda_zero_page_x() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "STA $7f",
                    "LDA #$00",
                    "LDX #$80",
                    "LDA $ff,X",
                    "BRK"),
                ExpectedState.accumAndMem(0x10,
                        0x007f, 0x10));
    }
    
    @Test
    public void lda_absolute() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$02",
                    "STA $0345",
                    "LDA #$00",
                    "LDA $0345",
                    "BRK"),
                ExpectedState.accumAndMem(0x02,
                        0x0345, 0x02));
    }
    
    @Test
    public void lda_absolute_x() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "STA $3102",
                    "LDX #$01",
                    "LDA #$00",
                    "LDA $3101,X",
                    "BRK"),
                ExpectedState.accumAndMem(0x10,
                        0x3102, 0x10));
    }
    
    @Test
    public void lda_absolute_y() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$10",
                    "STA $3102",
                    "LDY #$01",
                    "LDA #$00",
                    "LDA $3101,Y",
                    "BRK"),
                ExpectedState.accumAndMem(0x10,
                        0x3102, 0x10));
    }
    
    @Test
    public void lda_indexed_indirect_x() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$bc",
                    "STA $3102",
                    
                    "LDA #$02",
                    "STA $10",
                    "LDA #$31",
                    "STA $11",
                    
                    "LDA #$00",
                    "LDX #$30",
                    "LDA ($e0,X)",
                    "BRK"),
                ExpectedState.accumAndMem(0xbc,
                        0x0010, 0x02,
                        0x0011, 0x31,
                        
                        0x3102, 0xbc));
    }
    
    @Test
    public void lda_indirect_indexed_y() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$bc",
                    "STA $3102",
                    
                    "LDA #$01",
                    "STA $10",
                    "LDA #$31",
                    "STA $11",
                    
                    "LDA #$00",
                    "LDY #$01",
                    "LDA ($10),Y",
                    "BRK"),
                ExpectedState.accumAndMem(0xbc,
                        0x0010, 0x01,
                        0x0011, 0x31,
                        
                        0x3102, 0xbc));
    }

}
