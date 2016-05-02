package p.lodz.pl.adi;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import p.lodz.pl.adi.config.CoProvider;
import p.lodz.pl.adi.config.Conf;
import p.lodz.pl.adi.config.Config;
import p.lodz.pl.adi.utils.ImageResizer;
import p.lodz.pl.adi.utils.ResizeTask;

import java.io.IOException;

public class App {
    private Conf conf;
    private AmazonSQS sqs;
    private AmazonS3 s3;

    private ImageResizer ir;

    public App() throws IOException {
        conf = CoProvider.getConf();

        Config config = CoProvider.getConfig();
        AWSCredentials awsCredentials = config.toAWSCredentials();
        Region awsRegion = config.getAWSRegion();

        sqs = new AmazonSQSClient(awsCredentials);
        sqs.setRegion(awsRegion);

        s3 = new AmazonS3Client(awsCredentials);
        sqs.setRegion(awsRegion);

        ir = new ImageResizer();
    }


    public static void main(String[] args) throws IOException {

        App app = new App();

        ReceiveMessageRequest request = new ReceiveMessageRequest();
        request.setQueueUrl(app.conf.getSqs().getUrl());
        request.setMaxNumberOfMessages(5);
        request.setVisibilityTimeout(2);

        for (Message message : app.sqs.receiveMessage(request).getMessages()) {
            new ResizeTask(message, app.conf, app.sqs, app.s3).run();
        }
    }
}
