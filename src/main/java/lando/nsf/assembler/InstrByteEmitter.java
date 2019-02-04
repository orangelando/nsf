package lando.nsf.assembler;

import org.apache.commons.lang3.Validate;

import lando.nsf.cpu.Instruction;

final class InstrByteEmitter {
    
    private final ByteConsumer consumer;
    
    InstrByteEmitter(ByteConsumer consumer) {
        this.consumer = consumer;
    }

    void emit(Instruction instr) {
        Validate.isTrue(instr.addrMode.instrLen == 1);
        consumer.emitByte(instr.opCode);
    }
    
    void emit(Instruction instr, int low) {
        Validate.isTrue(instr.addrMode.instrLen == 2);
        consumer.emitByte(instr.opCode);
        consumer.emitByte(low & 255);
    }
    
    void emit(Instruction instr, int low, int high) {
        Validate.isTrue(instr.addrMode.instrLen == 3);
        consumer.emitByte(instr.opCode);
        consumer.emitByte(low & 255);
        consumer.emitByte(high & 255);
    }
}
