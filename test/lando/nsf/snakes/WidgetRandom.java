package lando.nsf.snakes;

import java.util.Objects;
import java.util.Random;

import lando.nsf.core6502.ByteArrayMem;

final class WidgetRandom {
    
    private static final int SYS_RANDOM_ADDR = 0xfe;

    private final Random random = new Random();
    private final byte[] oneByte = new byte[1];
    
    private final ByteArrayMem mem;

    WidgetRandom(ByteArrayMem mem) {
        this.mem = Objects.requireNonNull(mem);
    }
    
    void setRandByte() {
        random.nextBytes(oneByte);
        mem.bytes[SYS_RANDOM_ADDR] = oneByte[0];
    }
}
