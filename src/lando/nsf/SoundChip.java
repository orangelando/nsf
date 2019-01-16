package lando.nsf;

public enum SoundChip {
    VRC_6(0),
    VRC_7(1),
    FDS(2),
    MMC5(3),
    NAMCO_163(4),
    SUNSOFT_5B(5);
    
    final int bitPos;
    
    SoundChip(int bitPos) {
        this.bitPos = bitPos;
    }
}
