package lando.nsf.initload;

interface SampleConsumer {
    
    void init() throws Exception ;
    
    void consume(float sample) throws Exception;
    
    void finish() throws Exception;
}
