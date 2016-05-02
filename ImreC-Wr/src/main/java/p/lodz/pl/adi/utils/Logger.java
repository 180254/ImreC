package p.lodz.pl.adi.utils;

import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import org.apache.commons.lang3.RandomStringUtils;
import p.lodz.pl.adi.config.Conf;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class Logger {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Conf conf;
    private final AmazonSimpleDBAsync sdb;

    public Logger(Conf conf, AmazonSimpleDBAsync sdb) {
        this.conf = conf;
        this.sdb = sdb;
    }

    public void log(String action, String message) {
        logInternal(true, action, message);
    }

    public void log2(String action, String message) {
        logInternal(false, action, message);
    }

    private void logInternal(boolean sdbPut, String action, String message) {
        String uuid = RandomStringUtils.randomAlphanumeric(16);
        String textLog = String.format("1 | %s | ? | %s | %s", LocalDateTime.now().format(dtf), action, message);
        System.out.println(textLog);

        if (sdbPut) {
            ReplaceableAttribute attribute = new ReplaceableAttribute(uuid, textLog, true);
            PutAttributesRequest request = new PutAttributesRequest(
                    conf.getSimpleDb().getDomain(),
                    conf.getSimpleDb().getLogItemName(),
                    Collections.singletonList(attribute)
            );
            sdb.putAttributesAsync(request);
        }
    }
}
