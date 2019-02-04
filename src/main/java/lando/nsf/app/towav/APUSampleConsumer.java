package lando.nsf.app.towav;

interface APUSampleConsumer {
    
    void init() throws Exception ;
    
    void consume(float sample) throws Exception;
    
    void finish() throws Exception;
}
