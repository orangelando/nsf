package lando.nsf;

import static lando.nsf.HexUtils.toHex16;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

public final class NSFReader {

    public static final byte[] NESM = {'N', 'E', 'S', 'M', 0x1A};
	
	/**
	 * http://kevtris.org/nes/nsfspec.txt
	 */
    public static NSF readNSF(File file) throws Exception {
        byte[] bytes = FileUtils.readFileToByteArray(file);
        return readNSF(bytes);
    }
    
    public static NSF readNSF(byte[] bytes) {
        
		NSFHeader header = new NSFHeader();
		DataReader reader = new DataReader(bytes);
	
		reader.copy(0, header.nesm, 0, 5);
		header.versionNumber = reader.readByte(0x5);
		
		Validate.isTrue( equals(header.nesm, 0, NESM, 0, 5), "First five file bytes are not 'NESM\\n'" );
		//Validate.isTrue( header.versionNumber == 1 );
		
		header.totalSongs      = reader.readByte(0x6);
		header.startingSong    = reader.readByte(0x7);
		header.loadDataAddr    = reader.readWord(0x8);
		header.initDataAddr    = reader.readWord(0xA);
		header.playDataAddr    = reader.readWord(0xC);
		header.songName        = reader.readString(0x0E, 32);
		header.artistName      = reader.readString(0x2E, 32);
		header.copyrightHolder = reader.readString(0x4E, 32);
		header.ntscSpeed       = reader.readWord(0x6E);
		
		for(int i = 0; i < 8; i++) {
			header.bankswitchInitValues[i] = 
					reader.readByte(0x70 + i);
		}
		
		header.palSpeed        = reader.readWord(0x78);
		header.palNtscBits     = reader.readByte(0x7A);
		header.extraSoundChipSupport = reader.readByte(0x7B);
		
		return new NSF(header, bytes);
	}
	
    public static void load(NSF nsf, NESMem mem) {
		Validate.notNull(nsf);
		Validate.notNull(mem);
		
		if( nsf.isBanked() ) {
			throw new IllegalArgumentException("Banked NSFs not handled yet.");
		} else {
			System.err.println("Straight loading... " + 0x80 );
			
			if( nsf.header.loadDataAddr < 0x8000 ) {
				System.err.println("Warning: load data before $8000 vs " + toHex16(nsf.header.loadDataAddr));
			}
			
			for(int i = 0x80; i < nsf.data.length; i++) {
				mem.bytes[nsf.header.loadDataAddr + i - 0x80] = nsf.data[i];
			}
		}
	}
	
	private static boolean equals(byte [] a, int aIndex, byte [] b, int bIndex, int len) {
		
		Validate.notNull(a);
		Validate.isTrue(aIndex >= 0 && aIndex + len - 1 < a.length);
		
		Validate.notNull(b);
		Validate.isTrue(bIndex >= 0 && bIndex + len - 1 < b.length);
		
		for(int i = 0;i < len; i++) {
			if( a[aIndex + i] != b[bIndex + i]) {
				return false;
			}
		}
		
		return true;
	}
}
