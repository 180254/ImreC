package p.lodz.pl.adi;

import com.amazonaws.services.sqs.model.Message;
import p.lodz.pl.adi.config.CoProvider;
import p.lodz.pl.adi.config.Conf;
import p.lodz.pl.adi.config.Config;
import p.lodz.pl.adi.utils.AmazonHelper;
import p.lodz.pl.adi.utils.ExecutorHelper;
import p.lodz.pl.adi.utils.ImageResizer;
import p.lodz.pl.adi.utils.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class App {

    private final Logger logger;

    private final ImageResizer im;
    private final AmazonHelper am;

    private final ExecutorHelper executor;

    public App() throws IOException {
        Conf conf = CoProvider.getConf();
        Config config = CoProvider.getConfig();

        am = new AmazonHelper(config, conf);
        logger = new Logger(am);
        im = new ImageResizer();

        am.setLogger(logger); // circular dependency!?

        executor = new ExecutorHelper();
    }

    public void service() throws InterruptedException {
        //noinspection InfiniteLoopStatement
        do {
            logger.log2("COMPLETED", executor.getCompletedTaskCount());

            int needTasks = executor.needTasks();
            List<Message> messages = am.sqs$receiveMessages(needTasks);

            for (Message message : messages) {
                Runnable resizeTask = new ResizeTask(message, logger, am, im);
                resizeTask.run();
//                executor.submit(resizeTask);
            }

            if (messages.isEmpty()) {
                logger.log2("NOP", executor.getActiveCount());
            }

            TimeUnit.SECONDS.sleep(20);
        } while (true);
    }

    public static void main(String[] args)
            throws IOException, InterruptedException {
        new App().service();
    }
}
