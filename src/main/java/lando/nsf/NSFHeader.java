package lando.nsf;

import java.util.EnumSet;
import java.util.Set;

public final class NSFHeader {
    public byte[] nesm = new byte[5];
    public int versionNumber;
    public int totalSongs;
    public int startingSong;
    public int loadDataAddr;
    public int initDataAddr;
    public int playDataAddr;
    public String songName;
    public String artistName;
    public String copyrightHolder;
    public int ntscSpeed;
    public int[] bankswitchInitValues = new int[8];
    public int palSpeed;
    public int palNtscBits;
    public int extraSoundChipSupport;
    
    public Set<SoundChip> getSupportedExtraSoundChips() {
        Set<SoundChip> chips = EnumSet.noneOf(SoundChip.class);

        for(SoundChip chip: SoundChip.values()) {
            if( (extraSoundChipSupport & (1<<chip.bitPos)) != 0 ) {
                chips.add(chip);
            }
        }
        
        return chips;
    }
    
    public NtscPalMode getNtscPalMode() {
        
        switch(palNtscBits) {
        case 0b00: return NtscPalMode.NTSC;
        case 0b01: return NtscPalMode.PAL;
        case 0b10: return NtscPalMode.NTSC_AND_PAL;
        }
        return NtscPalMode.UNKNOWN;
    }
}

