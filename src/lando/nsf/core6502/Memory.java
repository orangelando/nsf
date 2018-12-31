package lando.nsf.core6502;

public interface Memory {

    int read(int addr);
    
    void write(int addr, int data);
}
