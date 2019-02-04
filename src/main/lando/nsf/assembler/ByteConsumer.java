package lando.nsf.assembler;

public interface ByteConsumer {

    void setAddress(int address); //called by .ORG directives
    void emitByte(int b); //b
}
