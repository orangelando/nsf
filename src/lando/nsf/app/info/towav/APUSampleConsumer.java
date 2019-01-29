package lando.nsf.app.info.towav;

interface APUSampleConsumer {
    
    void init() throws Exception ;
    
    void consume(float sample) throws Exception;
    
    void finish() throws Exception;
}
