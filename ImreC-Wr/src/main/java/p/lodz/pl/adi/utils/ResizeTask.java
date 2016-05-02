package p.lodz.pl.adi.utils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.devicefarm.model.ArgumentException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import org.apache.commons.io.FilenameUtils;
import p.lodz.pl.adi.config.Conf;
import p.lodz.pl.adi.enum1.Meta;
import p.lodz.pl.adi.enum1.WorkStatus;

import java.io.IOException;
import java.io.InputStream;

public class ResizeTask implements Runnable {

    private Message message;
    private Logger logger;

    private Conf conf;
    private AmazonSQSAsync sqs;
    private AmazonS3 s3;

    private ImageResizer ir = new ImageResizer();

    public ResizeTask(Message message, Logger logger, Conf conf, AmazonSQSAsync sqs, AmazonS3 s3) {
        this.message = message;
        this.logger = logger;
        this.conf = conf;
        this.sqs = sqs;
        this.s3 = s3;
    }

    @Override
    public void run() {
        logger.log("MESSAGE_PROC_START", message.getBody());

        String itemName = message.getBody();

        try {
            S3Object itemObject = getObject(itemName);
            ObjectMetadata metadata = itemObject.getObjectMetadata();

            String newSize = ensureNotNull(metadata.getUserMetaDataOf(Meta.NEW_SIZE), Meta.NEW_SIZE);
            String oFilename = ensureNotNull(metadata.getUserMetaDataOf(Meta.O_FILENAME), Meta.O_FILENAME);
            String workStatus = ensureNotNull(metadata.getUserMetaDataOf(Meta.WORK_STATUS), Meta.WORK_STATUS);

            if (!workStatus.equals(WorkStatus.SCHEDULED)) {
                logger.log("MESSAGE_PROC_STOP", message.getBody(), "Not scheduled(status=" + workStatus + ").");

                if (workStatus.equals(WorkStatus.DONE)) {
                    deleteObject(itemName);
                }
                return;
            }

            InputStream objectIs = itemObject.getObjectContent();
            int sizeMultiplier = Integer.parseInt(newSize);
            String imageType = FilenameUtils.getExtension(oFilename);

            InputStreamEnh resized = ir.resize(objectIs, sizeMultiplier, imageType);

            metadata.setContentLength(resized.getIsLength());
            metadata.getUserMetadata().put(Meta.WORK_STATUS, WorkStatus.DONE);

            putObject(itemName, metadata, resized.getIs());
            deleteMessage();

        } catch (IOException | ArgumentException | AmazonS3Exception ex) {
            logger.log("MESSAGE_PROC_STOP", message.getBody(), ex.getMessage());

            deleteObject(itemName);
            deleteMessage();

        } catch (AmazonClientException ex) {
            logger.log("AMAZON_CLIENT_EXCEPTION", ex.getMessage());

        } catch (RuntimeException ex) {
            logger.log("OTHER_EXCEPTION", ex.getMessage());
        }
    }

    private S3Object getObject(String itemName) {
        logger.log("OBJECT_GET", itemName);

        S3ObjectId objectId = new S3ObjectId(conf.getS3().getName(), itemName);
        GetObjectRequest itemObjectRequest = new GetObjectRequest(objectId);

        return s3.getObject(itemObjectRequest);
    }

    private void putObject(String itemName, ObjectMetadata metadata, InputStream objectIs) {
        logger.log("OBJECT_PUT", itemName, metadata.getUserMetadata().toString());

        PutObjectRequest request2 = new PutObjectRequest(conf.getS3().getName(), itemName, objectIs, metadata);
        request2.withCannedAcl(CannedAccessControlList.PublicRead);
        s3.putObject(request2);
    }

    private void deleteObject(String itemName) {
        DeleteObjectRequest request = new DeleteObjectRequest(conf.getS3().getName(), itemName);
        s3.deleteObject(request);
    }

    private void deleteMessage() {
        logger.log("MESSAGE_DELETE", message.getBody());

        DeleteMessageRequest delRequest = new DeleteMessageRequest();
        delRequest.setQueueUrl(conf.getSqs().getUrl());
        delRequest.setReceiptHandle(message.getReceiptHandle());

        sqs.deleteMessageAsync(delRequest);
    }

    private <T> T ensureNotNull(T obj, String varName) {
        if (obj == null) {
            throw new ArgumentException(varName);
        } else {
            return obj;
        }
    }
}

