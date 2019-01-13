package lando.nsf.core6502;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

public class AdcAndAddressingModeTests {

    
    @Test
    public void adc_immediate() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$01",
                    "ADC #$10",
                    "STA $0200",
                    "CLC",
                    "LDA #$01",
                    "ADC #$ff",
                    "STA $0201",
                    "ADC #$00",
                    "STA $0202",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0200, 0x11,
                        0x0201, 0x00,
                        0x0202, 0x01));
    }
    
    @Test
    public void adc_zero_page() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$04",
                    "STA $08",
                    "LDA #$10",
                    "ADC $08",
                    "STA $0200",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0008, 0x04,
                        0x0200, 0x14));
    }
    
    @Test
    public void adc_zero_page_x() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$02",
                    "STA $ff",
                    "LDA #$fe",
                    "TAX",
                    "LDA #$01",
                    "ADC $01,X",
                    "STA $0200",
                    "STA $03,X",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0001, 0x03,
                        0x00ff, 0x02,
                        0x0200, 0x03));
    }

    @Test
    public void adc_absolute() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$02",
                    "STA $0345",
                    "LDA #$10",
                    "ADC $0345",
                    "STA $0200",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0345, 0x02,
                        0x0200, 0x12));
    }
    
    @Test
    public void adc_absolute_x() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$bc",
                    "STA $3102",
                    "LDA #$03",
                    "TAX",
                    "LDA #$01",
                    "ADC $30ff,X",
                    "STA $0200",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x3102, 0xbc,
                        0x0200, 0xbd));
    }
    
    @Test
    public void adc_absolute_y() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$bc",
                    "STA $3102",
                    "LDA #$03",
                    "TAY",
                    "LDA #$01",
                    "ADC $30ff,Y",
                    "STA $0200",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x3102, 0xbc,
                        0x0200, 0xbd));
    }
    
    @Test
    public void adc_indexed_indirect_x() {
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
                    
                    "LDA #$01",
                    "TAX",
                    "ADC ($0f,X)",
                    "STA $0200",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x0010, 0x02,
                        0x0011, 0x31,
                        
                        0x3102, 0xbc,
                        
                        0x0200, 0xbd));
    }
    
    @Test
    public void adc_indirect_indexed_y() {
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
                    
                    "LDA #$01",
                    "TAY",
                    "ADC ($10),Y",
                    "STA $0200",

                    "BRK"),
                ExpectedState.onlyMem(
                        0x0010, 0x01,
                        0x0011, 0x31,

                        0x3102, 0xbc,
                        0x0200, 0xbd));
    }





}
