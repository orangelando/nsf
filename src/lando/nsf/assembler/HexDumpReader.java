package lando.nsf.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lando.nsf.ExecutableImage;
import lando.nsf.MemorySegment;

public final class HexDumpReader {
    
    public static byte[] asBytes(List<Integer> ints) {
        byte[] bytes = new byte[ints.size()];
        
        for(int i = 0; i < ints.size(); i++) {
            bytes[i] = ints.get(i).byteValue();
        }
        
        return bytes;
    }
    
    public ExecutableImage read(List<String> lines) {
        
        List<Integer> byteVals = new ArrayList<>();
        LineReader reader = new LineReader();
        
        int[] startAddress = {-1};
        int[] expectedNextAddress = {-1};
        
        reader.read(lines, line -> {
            line = line.trim();
            
            if( line.isEmpty() ) {
                return;
            }
            
            String[] addrSplit = line.split(":");
            
            if( addrSplit.length != 2) {
                throw new RuntimeException("line expected to be 2 parts 'ADDR: DATA'");
            }
            
            int lineAddr = Integer.parseInt(addrSplit[0].trim(), 16);
            String[] byteStrs = addrSplit[1].trim().split("\\s+");
            
            List<Integer> lineBytes = Stream.of(byteStrs)
                .map(d -> Integer.parseInt(d, 16))
                .collect(Collectors.toList());
            
            if( startAddress[0] == -1 ) {
                startAddress[0] = lineAddr;
            } else {
                
                if( expectedNextAddress[0] != lineAddr ) {
                    throw new RuntimeException(String.format(
                            "Line address change does not match number of bytes last read [%x] vs [%x]",
                            expectedNextAddress[0], lineAddr));
                }
            }
            
            byteVals.addAll(lineBytes);
            expectedNextAddress[0] = lineAddr + lineBytes.size();
        });
        
        return toImg(startAddress[0], byteVals);
    }
    
    private ExecutableImage toImg(int startAddress, List<Integer> byteVals) {
        byte[] bytes = asBytes(byteVals);
        
        MemorySegment seg = new MemorySegment(startAddress, bytes);
        
        ExecutableImage img = new ExecutableImage();
        
        img.addMemorySegment(seg);
        
        return img;
    }
}
