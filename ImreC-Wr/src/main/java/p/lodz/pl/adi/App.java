package p.lodz.pl.adi;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import p.lodz.pl.adi.config.CoProvider;
import p.lodz.pl.adi.config.Conf;
import p.lodz.pl.adi.config.Config;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        Config config = CoProvider.getConfig();
        AWSCredentials awsCredentials = config.toAWSCredentials();
        Region awsRegion = config.getAWSRegion();

        Conf conf = CoProvider.getConf();

        AmazonSQS sqs = new AmazonSQSClient(awsCredentials);
        sqs.setRegion(awsRegion);

        ReceiveMessageRequest request = new ReceiveMessageRequest();
        request.setQueueUrl(conf.getSqs().getUrl());
        request.setMaxNumberOfMessages(5);
        request.setVisibilityTimeout(120);

        for (Message message : sqs.receiveMessage(request).getMessages()) {
//            DeleteMessageRequest delRequest = new DeleteMessageRequest();
//            delRequest.setQueueUrl("https://sqs.us-west-2.amazonaws.com/079592274660/Imrec-SQS");
//            delRequest.setReceiptHandle(message.getReceiptHandle());
            System.out.println(message.getBody());
        }

    }
}
