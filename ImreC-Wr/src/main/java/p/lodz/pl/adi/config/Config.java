package p.lodz.pl.adi.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Config {

    private final String accessKeyId;
    private final String secretAccessKey;
    private final String region;

    @JsonCreator
    public Config(@JsonProperty("accessKeyId") String accessKeyId,
                  @JsonProperty("secretAccessKey") String secretAccessKey,
                  @JsonProperty("region") String region) {

        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.region = region;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public String getRegion() {
        return region;
    }

    public AWSCredentials toAWSCredentials() {
        return new BasicAWSCredentials(accessKeyId, secretAccessKey);
    }

    public Region getAWSRegion() {
        return Region.getRegion(Regions.fromName(region));
    }
}
