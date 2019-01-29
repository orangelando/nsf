package lando.nsf.app.info.towav;

import org.apache.commons.lang3.Validate;

public final class PeriodTimestampFinder {

    private final long baseTime;
    private final long periodTime;
    
    public PeriodTimestampFinder(final long baseTime, final long periodTime) {
        Validate.isTrue(baseTime >= 0);
        Validate.isTrue(periodTime > 0);
        
        this.baseTime = baseTime;
        this.periodTime = periodTime;
    }
    
    /**
     * returns the lowest timestamp that is >= now and can
     * be written in the form baseTime + N*periodTime where N is an integer.
     */
    public long findNextPeriod(long now) {
        
        long n = (now - baseTime)/periodTime;
        long next = baseTime + n*periodTime;
        
        if( next < now ) {
            return baseTime + (n + 1)*periodTime;
        }
        
        return next;
    }
}
