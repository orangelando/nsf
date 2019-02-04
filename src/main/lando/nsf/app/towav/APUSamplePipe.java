package lando.nsf.app.towav;

import java.util.Objects;

final class APUSamplePipe {

    final APUSampleSupplier sampleSupplier;
    final APUSampleConsumer sampleConsumer;
    
    APUSamplePipe(APUSampleSupplier sampleSupplier, APUSampleConsumer sampleConsumer) {
        this.sampleSupplier = Objects.requireNonNull(sampleSupplier);
        this.sampleConsumer = Objects.requireNonNull(sampleConsumer);
    }

    void sample(float scale) throws Exception {
        sampleConsumer.consume(
                scale*sampleSupplier.sample());
    }
}
