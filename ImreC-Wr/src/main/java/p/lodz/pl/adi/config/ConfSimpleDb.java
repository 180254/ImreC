package p.lodz.pl.adi.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfSimpleDb {

    private final String domain;
    private final String logItemName;

    @JsonCreator
    public ConfSimpleDb(@JsonProperty("Domain") String domain,
                        @JsonProperty("LogItemName") String logItemName) {

        this.domain = domain;
        this.logItemName = logItemName;
    }

    public String getDomain() {
        return domain;
    }

    public String getLogItemName() {
        return logItemName;
    }
}
