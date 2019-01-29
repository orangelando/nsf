package lando.nsf.initload;

interface APUSampleConsumer {
    
    void init() throws Exception ;
    
    void consume(float sample) throws Exception;
    
    void finish() throws Exception;
}
