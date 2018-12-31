package lando.nsf;

public final class HexUtils {

    public static String toHex16(int n) {
		return "" +
				HEX_CHARS[(n >>12) & 0xF] +
				HEX_CHARS[(n >> 8) & 0xF] +
				HEX_CHARS[(n >> 4) & 0xF] +
				HEX_CHARS[(n >> 0) & 0xF] ;
	}
	
    public static String toHex8(int n) {
		return "" +
				HEX_CHARS[(n >> 4) & 0xF] +
				HEX_CHARS[(n >> 0) & 0xF] ;
	}

    public static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();	
}
