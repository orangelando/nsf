package lando.nsf;

import java.util.Objects;

public final class NSF {
    public final NSFHeader header;
    public final byte[] data;
	
    public NSF(NSFHeader header, byte[] data) {
		this.header = Objects.requireNonNull(header);
		this.data = Objects.requireNonNull(data);
	}
	
    public boolean isBanked() {
		for(int bank: header.bankswitchInitValues) {
			if( bank != 0 ) {
				return true;
			}
		}
		
		return false;
	}
    
    public boolean isFDS() {
        return header.getSupportedExtraSoundChips().contains(SoundChip.FDS);
    }
	
    public boolean isNTSC() {
        return header.getNtscPalMode() == NtscPalMode.NTSC || 
                header.getNtscPalMode() == NtscPalMode.NTSC_AND_PAL;
	}
    
    public boolean isPAL() {
        return header.getNtscPalMode() == NtscPalMode.PAL || 
                header.getNtscPalMode() == NtscPalMode.NTSC_AND_PAL;
    }
    
    public long getPlayPeriodNanos() {
        
        long micros;
        
        if( header.ntscSpeed != 0 && isNTSC() ) {
            micros = header.ntscSpeed;
        }
        
        else if( header.palSpeed != 0 && isPAL() ) {
            micros = header.palSpeed;
        }
        
        else {
            throw new IllegalArgumentException(
                    "Can't figure out speed: " +
                    header.getNtscPalMode() + " " +
                    header.ntscSpeed + " " +
                    header.palSpeed);
        }
        
        return micros*1000;
    }
}