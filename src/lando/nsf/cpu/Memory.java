package lando.nsf.cpu;

public interface Memory {

    int read(int addr);
    
    void write(int addr, int data);
}
