package p.lodz.pl.adi.utils;

import com.amazonaws.AmazonClientException;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

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
            System.out.println("? - Unable to send log.");
        }
    }

    public void log2(String action, Object... args) {
        logInternal(false, action, args);
    }

    private void logInternal(boolean sdbPut, String action, Object... args) {
        String text = String.format("1 | %s | ? | %s", LocalDateTime.now().format(dtf), action);

        for (Object mes : args) {
            String mesAsString = mes != null ? mes.toString() : "NULL";
            text += String.format(" | %s", mesAsString);
        }

        System.out.println(text);

        if (sdbPut) {
            Collection<Pair<String, String>> attrs = new ArrayList<>();
            attrs.add(Pair.of("0_Source", "1"));
            attrs.add(Pair.of("1_Date", LocalDateTime.now().format(dtf)));
            attrs.add(Pair.of("2_IP", "?"));
            attrs.add(Pair.of("3_Action", action));

            for (int i = 0; i < args.length; i++) {
                Object mes = args[i];
                String mesAsString = mes != null ? mes.toString() : "NULL";
                attrs.add(Pair.of("4_Arg_" + i, mesAsString));
            }

            am.sdb$putLogAsync(attrs);
        }
    }
}
