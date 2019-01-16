package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;

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
}
