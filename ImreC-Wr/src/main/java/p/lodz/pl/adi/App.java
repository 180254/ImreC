package p.lodz.pl.adi;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.imgscalr.Scalr;
import p.lodz.pl.adi.config.CoProvider;
import p.lodz.pl.adi.config.Conf;
import p.lodz.pl.adi.config.Config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

        AmazonS3 s3 = new AmazonS3Client(awsCredentials);
        sqs.setRegion(awsRegion);

        ReceiveMessageRequest request = new ReceiveMessageRequest();
        request.setQueueUrl(conf.getSqs().getUrl());
        request.setMaxNumberOfMessages(5);
        request.setVisibilityTimeout(2);

        for (Message message : sqs.receiveMessage(request).getMessages()) {
//            DeleteMessageRequest delRequest = new DeleteMessageRequest();
//            delRequest.setQueueUrl("https://sqs.us-west-2.amazonaws.com/079592274660/Imrec-SQS");
//            delRequest.setReceiptHandle(message.getReceiptHandle());
            System.out.println(message.getBody());

            GetObjectRequest request1 = new GetObjectRequest(conf.getS3().getName(), message.getBody());

            S3ObjectInputStream objectContent = s3.getObject(request1).getObjectContent();
            BufferedImage srcImage = ImageIO.read(objectContent);
            int w = (int) (srcImage.getWidth() * 0.4);
            int h = (int) (srcImage.getHeight() * 0.4);
            BufferedImage scaledImage = Scalr.resize(srcImage, w, h); // Scale image

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(scaledImage, "png", os);
            byte[] bytes = os.toByteArray();
            InputStream is = new ByteArrayInputStream(bytes);

            ObjectMetadata objectMetadata = s3.getObject(request1).getObjectMetadata();
            objectMetadata.setContentLength(bytes.length);
            PutObjectRequest request2 = new PutObjectRequest(conf.getS3().getName(), message.getBody(), is, objectMetadata);
            s3.putObject(request2);
        }


    }
}
