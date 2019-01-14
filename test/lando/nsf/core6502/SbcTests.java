package lando.nsf.core6502;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

public class SbcTests {

    @Test
    public void multi_byte_subtraction() {
        
        //try to compute 0x416 - 0x20 = 0x3f6 in place in memory
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "define srcL $10",
                    "define srcH $11",
                    "",
                    "    LDA #$16",
                    "    STA srcL",
                    "    LDA #$04",
                    "    STA srcH",
                    "    ",
                    "    LDA srcL",
                    "    SEC",
                    "    SBC #$20",
                    "    STA srcL",
                    "    BCC cont",
                    "    BRK",
                    "cont:",
                    "    DEC srcH",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x10, 0xf6,
                        0x11, 0x03
                        ));

    }
}
