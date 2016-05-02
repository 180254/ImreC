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
import p.lodz.pl.adi.config.CoProvider;
import p.lodz.pl.adi.config.Conf;
import p.lodz.pl.adi.config.Config;
import p.lodz.pl.adi.utils.Logger;
import p.lodz.pl.adi.utils.ResizeTask;

import java.io.IOException;
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
        request.setMaxNumberOfMessages(5);
        request.setVisibilityTimeout(300);

        //noinspection InfiniteLoopStatement
        do {
            for (Message message : app.sqs.receiveMessage(request).getMessages()) {
                new ResizeTask(message, app.logger, app.conf, app.sqs, app.s3).run();
            }

            TimeUnit.SECONDS.sleep(20);
        } while (true);
    }
}
