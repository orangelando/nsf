package lando.nsf.apu;

import lando.nsf.gui.StringUtils;

final class APULog {

    static void logWrite(String l, int A, int M) {
        
        String s = StringUtils.toBin8(M);
        s = s.substring(0, 4) + " " + s.substring(4);
        
        String w = "";
        
        System.out.printf("%s: %s[%s]%s%n", StringUtils.toHex4(A), w, l, s);
    }
    
    static void logWrite(int regNum, int A, int M) {
       
        ++regNum; 
        
        String s = StringUtils.toBin8(M);
        s = s.substring(0, 4) + " " + s.substring(4);
        
        String w = "";
        
        for(int i = 1; i <= regNum; i++) {
            w += "               ";
        }
        
        System.out.printf("%s: %s[%d]%s%n", StringUtils.toHex4(A), w, regNum, s);
    }
}
