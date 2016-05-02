package p.lodz.pl.adi;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import p.lodz.pl.adi.config.CoProvider;
import p.lodz.pl.adi.config.Conf;
import p.lodz.pl.adi.config.Config;
import p.lodz.pl.adi.utils.ExecutorUtil;
import p.lodz.pl.adi.utils.Logger;
import p.lodz.pl.adi.utils.ResizeTask;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class App {

    public Logger logger;

    private Conf conf;
    private AmazonSQSAsync sqs;
    private AmazonS3 s3;

    public App() throws IOException {
        conf = CoProvider.getConf();

        Config config = CoProvider.getConfig();
        AWSCredentials awsCredentials = config.toAWSCredentials();
        Region awsRegion = config.getAWSRegion();

        sqs = new AmazonSQSAsyncClient(awsCredentials);
        sqs.setRegion(awsRegion);

        s3 = new AmazonS3Client(awsCredentials);
        sqs.setRegion(awsRegion);

        AmazonSimpleDBAsync sdb = new AmazonSimpleDBAsyncClient(awsCredentials);
        sdb.setRegion(awsRegion);
        logger = new Logger(conf, sdb);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        App app = new App();

        ReceiveMessageRequest request = new ReceiveMessageRequest();
        request.setQueueUrl(app.conf.getSqs().getUrl());
        request.setVisibilityTimeout(300);

        ExecutorUtil executor = new ExecutorUtil();

        //noinspection InfiniteLoopStatement
        do {
            app.logger.log2("COMPLETED", Long.toString(executor.getCompletedTaskCount()));

            request.setMaxNumberOfMessages(executor.needTasks());
            ReceiveMessageResult result = app.sqs.receiveMessage(request);
            List<Message> messages = result.getMessages();

            for (Message message : messages) {
                Runnable resizeTask = new ResizeTask(message, app.logger, app.conf, app.sqs, app.s3);
                executor.submit(resizeTask);
            }

            if (executor.getActiveCount() == 0 && messages.isEmpty()) {
                app.logger.log2("NOP", "NOP");
            }

            TimeUnit.SECONDS.sleep(20);
        } while (true);
    }
}
