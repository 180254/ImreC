package p.lodz.pl.adi.utils;

import com.amazonaws.AmazonClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AmazonHelper am;

    public Logger(AmazonHelper am) {
        this.am = am;
    }

    public void log(String action, Object... args) {
        try {
            logInternal(true, action, args);

        } catch (AmazonClientException ignored) {
        }
    }

    public void log2(String action, Object... args) {
        logInternal(false, action, args);
    }

    private void logInternal(boolean sdbPut, String action, Object... args) {
        String text = String.format("1 | %s | ? | %s", LocalDateTime.now().format(dtf), action);

        for (Object mes : args) {
            text += String.format(" | %s", mes.toString());
        }

        System.out.println(text);
        if (sdbPut) am.sdb$putLogAsync(text);
    }
}
