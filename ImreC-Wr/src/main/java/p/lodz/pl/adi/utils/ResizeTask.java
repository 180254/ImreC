package p.lodz.pl.adi.utils;

import com.amazonaws.services.devicefarm.model.ArgumentException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
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

    private Conf conf;
    private AmazonSQS sqs;
    private AmazonS3 s3;

    private ImageResizer ir = new ImageResizer();

    public ResizeTask(Message message, Conf conf, AmazonSQS sqs, AmazonS3 s3) {
        this.message = message;
        this.conf = conf;
        this.sqs = sqs;
        this.s3 = s3;
    }

    @Override
    public void run() {
        String itemName = message.getBody();
        S3Object itemObject = getObject(itemName);
        ObjectMetadata metadata = itemObject.getObjectMetadata();

        try {
            String newSize = ensureNotNull(metadata.getUserMetaDataOf(Meta.NEW_SIZE), Meta.NEW_SIZE);
            String oFilename = ensureNotNull(metadata.getUserMetaDataOf(Meta.O_FILENAME), Meta.O_FILENAME);
            String workStatus = ensureNotNull(metadata.getUserMetaDataOf(Meta.WORK_STATUS), Meta.WORK_STATUS);

            if (!workStatus.equals(WorkStatus.SCHEDULED)) {
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

        } catch (IOException | ArgumentException e) {
            metadata.getUserMetadata().put(Meta.WORK_STATUS, WorkStatus.DONE);
            changeMetadata(itemName, metadata);

            deleteMessage();
        }
    }


    private S3Object getObject(String itemName) {
        S3ObjectId objectId = new S3ObjectId(conf.getS3().getName(), itemName);
        GetObjectRequest itemObjectRequest = new GetObjectRequest(objectId);

        return s3.getObject(itemObjectRequest);
    }

    private void putObject(String itemName, ObjectMetadata metadata, InputStream objectIs) {
        PutObjectRequest request2 = new PutObjectRequest(
                conf.getS3().getName(), itemName, objectIs, metadata
        );

        s3.putObject(request2);
    }

    private void changeMetadata(String itemName, ObjectMetadata metadata) {
        CopyObjectRequest request = new CopyObjectRequest(
                conf.getS3().getName(), itemName, conf.getS3().getName(), itemName
        );
        request.withNewObjectMetadata(metadata);

        s3.copyObject(request);
    }

    private void deleteMessage() {
        DeleteMessageRequest delRequest = new DeleteMessageRequest();
        delRequest.setQueueUrl(conf.getSqs().getUrl());
        delRequest.setReceiptHandle(message.getReceiptHandle());

        sqs.deleteMessage(delRequest);
    }

    private <T> T ensureNotNull(T obj, String varName) {
        if (obj == null) {
            throw new ArgumentException(varName);
        } else {
            return obj;
        }
    }
}

