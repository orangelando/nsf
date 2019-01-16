package lando.nsf;

public final class NSF {
    public final NSFHeader header;
    public final byte[] data;
	
    public NSF(NSFHeader header, byte[] data) {
		this.header = header;
		this.data = data;
	}
	
    public boolean isBanked() {
		for(int bank: header.bankswitchInitValues) {
			if( bank != 0 ) {
				return true;
			}
		}
		
		return false;
	}
	
    public boolean isNTSC() {
		return (header.palNtscBits & 1) == 0;
	}
}