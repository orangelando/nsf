package lando.nsf.cpu.instructions;

import static lando.nsf.cpu.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.cpu.ExpectedState;

public class BranchTests {

    @Test
    public void bcc() {
        runTest(
            0x0600,
            100,
            Arrays.asList(
                "    SEC",
                "    BCC is_clear",
                "    LDA #$ff",
                "    JMP cont",
                "is_clear:",
                "    LDA #$10",
                "cont:",
                "    STA $00",
                "    CLC",
                "    BCC also_clear",
                "    LDA #$ff",
                "    JMP also_cont",
                "also_clear:",
                "    LDA #$10",
                "also_cont:",
                "    STA $01",
                "    BRK"),
            ExpectedState.onlyMem(
                    0x00, 0xff,
                    0x01, 0x10));
    }
    
    @Test
    public void bcs() {
        runTest(
            0x0600,
            100,
            Arrays.asList(
                "    LDA #$10", //accum will only be set if wrong branch taken
                "    SEC",
                "    BCS pos",
                "    LDA #$ff",
                "    JMP cont",
                "pos:",
                "    STA $00",
                "cont:",
                "    CLC",
                "    BCS neg",
                "    STA $01",
                "    JMP done",
                "neg:",
                "    LDA #$ff",
                "    STA $01",
                "done:",
                "    BRK"),
            ExpectedState.onlyMem(
                    0x00, 0x10, 
                    0x01, 0x10));
    }
    
    @Test
    public void beq() {
        runTest(
            0x0600,
            100,
            Arrays.asList(
                "    LDA #$00",
                "    BEQ pos",
                
                "    LDA #$ff",
                "    JMP cont",
                "pos:",
                "    LDA #$10",
                "cont:",
                "    STA $00",
                
                "    LDA #$01",
                "    BEQ neg",
                
                "    LDA #$10",
                "    JMP done",
                "neg:",
                "    LDA #$ff",
                "done:",
                "    STA $01",
                "    BRK"),
            ExpectedState.onlyMem(
                    0x00, 0x10,
                    0x01, 0x10));
    }
    
    @Test
    public void bmi() {
        runTest(
            0x0600,
            100,
            Arrays.asList(
                "    LDA #$80",
                "    BMI pos",
                
                "    LDA #$ff",
                "    JMP cont",
                "pos:",
                "    LDA #$10",
                "cont:",
                "    STA $00",
                
                "    LDA #$01",
                "    BMI neg",
                
                "    LDA #$10",
                "    JMP done",
                "neg:",
                "    LDA #$ff",
                "done:",
                "    STA $01",
                "    BRK"),
            ExpectedState.onlyMem(
                    0x00, 0x10,
                    0x01, 0x10));
    }

    @Test
    public void bne() {
        runTest(
            0x0600,
            100,
            Arrays.asList(
                "    LDA #$01",
                "    BNE pos",
                
                "    LDA #$ff",
                "    JMP cont",
                "pos:",
                "    LDA #$10",
                "cont:",
                "    STA $00",
                
                "    LDA #$00",
                "    BNE neg",
                
                "    LDA #$10",
                "    JMP done",
                "neg:",
                "    LDA #$ff",
                "done:",
                "    STA $01",
                "    BRK"),
            ExpectedState.onlyMem(
                    0x00, 0x10,
                    0x01, 0x10));
    }

    @Test
    public void bpl() {
        runTest(
            0x0600,
            100,
            Arrays.asList(
                "    LDA #$00",
                "    BPL pos",
                
                "    LDA #$ff",
                "    JMP cont",
                "pos:",
                "    LDA #$10",
                "cont:",
                "    STA $00",
                
                "    LDA #$80",
                "    BPL neg",
                
                "    LDA #$10",
                "    JMP done",
                "neg:",
                "    LDA #$ff",
                "done:",
                "    STA $01",
                "    BRK"),
            ExpectedState.onlyMem(
                    0x00, 0x10,
                    0x01, 0x10));
    }
    
    @Test
    public void bvc() {
        runTest(
            0x0600,
            100,
            Arrays.asList(
                "    LDA #$01",
                "    ADC #$01",
                
                "    BVC pos",
                
                "    LDA #$ff",
                "    JMP cont",
                "pos:",
                "    LDA #$10",
                "cont:",
                "    STA $00",
                
                "    LDA #$40",
                "    ADC #$40",
                
                "    BVC neg",
                
                "    LDA #$10",
                "    JMP done",
                "neg:",
                "    LDA #$ff",
                "done:",
                "    STA $01",
                "    BRK"),
            ExpectedState.onlyMem(
                    0x00, 0x10,
                    0x01, 0x10));
    }
    
    @Test
    public void bvs() {
        runTest(
            0x0600,
            100,
            Arrays.asList(
                "    LDA #$40",
                "    ADC #$40",
                
                "    BVS pos",
                
                "    LDA #$ff",
                "    JMP cont",
                "pos:",
                "    LDA #$10",
                "cont:",
                "    STA $00",
                
                "    LDA #$01",
                "    ADC #$01",
                
                "    BVS neg",
                
                "    LDA #$10",
                "    JMP done",
                "neg:",
                "    LDA #$ff",
                "done:",
                "    STA $01",
                "    BRK"),
            ExpectedState.onlyMem(
                    0x00, 0x10,
                    0x01, 0x10));
    }
}
