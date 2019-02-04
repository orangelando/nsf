package lando.nsf.gui;

public final class StringUtils {

    public static String toBin8(int a) {
        String s = "00000000" + Integer.toBinaryString(a & 0xFF);
        
        return s.substring(s.length() - 8);
    }
    
    public static String toHex4(int a) {
        String s = "0000" + Integer.toHexString(a & 0xFFFF);
        
        return "$" + s.substring(s.length() - 4);
    }
    
    public static String toHex2(int a) {
        String s = "00" + Integer.toHexString(a & 0xFF);
        
        return "$" + s.substring(s.length() - 2);
    }
   
    private StringUtils() {
        
    }
}
