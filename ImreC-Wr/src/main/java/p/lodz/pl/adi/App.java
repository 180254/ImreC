package p.lodz.pl.adi;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.devicefarm.model.ArgumentException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.apache.commons.io.FilenameUtils;
import p.lodz.pl.adi.config.CoProvider;
import p.lodz.pl.adi.config.Conf;
import p.lodz.pl.adi.config.Config;
import p.lodz.pl.adi.utils.ImageResizer;
import p.lodz.pl.adi.utils.InputStreamEnh;

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

            try {
                String itemName = message.getBody();
                System.out.println(itemName);
                GetObjectRequest itemObjectRequest = new GetObjectRequest(app.conf.getS3().getName(), itemName);
                S3Object itemObject = app.s3.getObject(itemObjectRequest);
                ObjectMetadata objectMetadata1 = itemObject.getObjectMetadata();

                String task = objectMetadata1.getUserMetaDataOf("task");
                String filename = objectMetadata1.getUserMetaDataOf("filename");

                if (task == null || filename == null) {
                    throw new IllegalArgumentException();
                }

                int sizeMultiplier = Integer.parseInt(task);
                S3ObjectInputStream objectContent = itemObject.getObjectContent();
                String imageType = FilenameUtils.getExtension(filename);

                InputStreamEnh resize = app.ir.resize(objectContent, sizeMultiplier, imageType);
                objectMetadata1.setContentLength(resize.getIsLength());
                objectMetadata1.getUserMetadata().put("progress", "2");

                PutObjectRequest request2 = new PutObjectRequest(app.conf.getS3().getName(), itemName, resize.getIs(), objectMetadata1);
                app.s3.putObject(request2);

                DeleteMessageRequest delRequest = new DeleteMessageRequest();
                delRequest.setQueueUrl("https://sqs.us-west-2.amazonaws.com/079592274660/Imrec-SQS");
                delRequest.setReceiptHandle(message.getReceiptHandle());
                app.sqs.deleteMessage(delRequest);

            } catch (IOException | ArgumentException e) {
                DeleteMessageRequest delRequest = new DeleteMessageRequest();
                delRequest.setQueueUrl("https://sqs.us-west-2.amazonaws.com/079592274660/Imrec-SQS");
                delRequest.setReceiptHandle(message.getReceiptHandle());
                app.sqs.deleteMessage(delRequest);
            }
        }


    }
}
