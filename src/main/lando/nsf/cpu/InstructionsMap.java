package lando.nsf.cpu;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

final class InstructionsMap {
    
    Instruction[] buildOpCodeArray(List<Instruction> instructions) {
        Instruction[] array = new Instruction[256];
        
        for(Instruction instr: instructions) {
            Validate.isTrue( array[instr.opCode] == null );
            
            array[instr.opCode] = instr;
        }
        
        return array;
    }

    Map<OpCodeName, Map<AddrMode, Instruction>> buildNameMap(List<Instruction> instructions) {
        Validate.notNull(instructions);
        
        Map<OpCodeName, Map<AddrMode, Instruction>> nameMap = 
                new EnumMap<>(OpCodeName.class); 

        for(Instruction instr: instructions) {
            
            Map<AddrMode, Instruction> addrMap = nameMap.get(instr.name);
            
            if( addrMap == null ) {
                addrMap = new EnumMap<>(AddrMode.class);
                nameMap.put(instr.name, addrMap);
            }
            
            Validate.isTrue( ! addrMap.containsKey(instr.addrMode), 
                    "Dupe entry for " + instr.name + ", " + instr.addrMode);
            
            addrMap.put(instr.addrMode, instr);
        }
        
        //freeze all inbetween maps
        for(OpCodeName key: nameMap.keySet()) {
            nameMap.put(key, 
                    Collections.unmodifiableMap(nameMap.get(key)));
        }

        //freeze top level map
        return Collections.unmodifiableMap(nameMap);
    }
}
