package lando.nsf.apu;

import java.util.Objects;

public final class APURegisters {

    private final APU apu;
    
    public APURegisters(APU apu) {
        this.apu = Objects.requireNonNull(apu);
    }
}
