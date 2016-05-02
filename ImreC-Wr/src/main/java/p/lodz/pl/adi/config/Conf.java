package p.lodz.pl.adi.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Conf {

    private final ConfSdb simpleDb;
    private final ConfSqs sqs;
    private final ConfS3 s3;

    @JsonCreator
    public Conf(@JsonProperty("SimpleDb") ConfSdb simpleDb,
                @JsonProperty("Sqs") ConfSqs sqs,
                @JsonProperty("S3") ConfS3 s3) {

        this.simpleDb = simpleDb;
        this.sqs = sqs;
        this.s3 = s3;
    }

    public ConfSdb getSimpleDb() {
        return simpleDb;
    }

    public ConfSqs getSqs() {
        return sqs;
    }

    public ConfS3 getS3() {
        return s3;
    }
}
