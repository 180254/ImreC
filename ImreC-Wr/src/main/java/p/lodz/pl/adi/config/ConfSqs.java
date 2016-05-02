package p.lodz.pl.adi.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfSqs {

    private final String url;

    @JsonCreator
    public ConfSqs(@JsonProperty("Url") String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
