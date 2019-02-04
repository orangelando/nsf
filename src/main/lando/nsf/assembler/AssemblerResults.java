package lando.nsf.assembler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import lando.nsf.ExecutableImage;

public final class AssemblerResults {

    private final ExecutableImage execImg;
    private final TreeMap<Integer, Integer> addrsToLineNums;
    private final Map<String, Integer> addressLabels;
    
    public AssemblerResults(
            ExecutableImage execImg, 
            TreeMap<Integer, Integer> addrsToLineNums, 
            Map<String, Integer> addressLabels) {
        
        this.execImg         = Objects.requireNonNull(execImg);
        this.addrsToLineNums = new TreeMap<>(Objects.requireNonNull(addrsToLineNums));
        this.addressLabels   = new HashMap<>(Objects.requireNonNull(addressLabels));
    }
    
    public ExecutableImage getExecImg() {
        return execImg;
    }
    
    public TreeMap<Integer, Integer> getAddrsToLineNums() {
        return addrsToLineNums;
    }
    
    public Map<String, Integer> getAddressLabels() {
        return addressLabels;
    }
}
