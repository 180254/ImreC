package p.lodz.pl.adi;

import com.amazonaws.services.devicefarm.model.ArgumentException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.model.Message;
import org.apache.commons.io.FilenameUtils;
import p.lodz.pl.adi.enum1.Meta;
import p.lodz.pl.adi.enum1.WorkStatus;
import p.lodz.pl.adi.utils.AmazonHelper;
import p.lodz.pl.adi.utils.ImageResizer;
import p.lodz.pl.adi.utils.InputStreamE;
import p.lodz.pl.adi.utils.Logger;

import java.io.IOException;
import java.io.InputStream;

public class ResizeTask implements Runnable {

    private Logger logger;
    private AmazonHelper am;
    private ImageResizer ir;

    private Message message;

    public ResizeTask(Message message, Logger logger, AmazonHelper am, ImageResizer ir) {
        this.logger = logger;
        this.am = am;
        this.ir = ir;
        this.message = message;
    }

    @Override
    public void run() {
        logger.log("MESSAGE_PROC_START", message.getBody());

        String itemName = message.getBody();
        ObjectMetadata itemMetadataDackup = null;

        try {
            S3Object itemObject = am.s3$getObject(itemName);
            ObjectMetadata itemMetadata = itemObject.getObjectMetadata();
            itemMetadataDackup = itemMetadata.clone();

            // interesting meta
            String meta_newSize = itemMetadata.getUserMetaDataOf(Meta.NEW_SIZE);
            String meta_oFilename = itemMetadata.getUserMetaDataOf(Meta.O_FILENAME);
            String meta_workStatus = itemMetadata.getUserMetaDataOf(Meta.WORK_STATUS);

            // process only "scheduled"
            if (!meta_workStatus.equals(WorkStatus.Scheduled.c())) {
                logger.log("MESSAGE_PROC_STOP", message.getBody(), "status=" + meta_workStatus);

                if (meta_workStatus.equals(WorkStatus.Done.c())) {
                    am.s3$deleteObject(itemName);
                }

                return;
            }

            // mark as processing
            copyWithNewStatus(itemName, itemMetadata, WorkStatus.Processing);

            // resize!!
            InputStream object$is = itemObject.getObjectContent();
            int sizeMultiplier = Integer.parseInt(meta_newSize);
            String imageType = FilenameUtils.getExtension(meta_oFilename);
            InputStreamE resized = ir.resize(object$is, sizeMultiplier, imageType);

            // update metadata
            itemMetadata.getUserMetadata().put(Meta.WORK_STATUS, WorkStatus.Done.c());
            itemMetadata.setContentLength(resized.getIsLength());

            // work done
            am.s3$putObject(itemName, resized.getIs(), itemMetadata, CannedAccessControlList.PublicRead);
            am.sqs$deleteMessageAsync(message);

        } catch (IOException | ArgumentException | AmazonS3Exception ex) {
            // bad/forbidden task
            logger.log("MESSAGE_PROC_STOP", message.getBody(), ex.getClass().getName(), ex.getMessage());

            am.s3$deleteObject(itemName);
            am.sqs$deleteMessageAsync(message);

        } catch (RuntimeException ex) {
            logger.log("SOME_EXCEPTION", message.getBody(), ex.getClass().getName(), ex.getMessage());

            // was may be marked as processing
            if (itemMetadataDackup != null) {
                copyWithNewStatus(itemName, itemMetadataDackup, WorkStatus.Scheduled);
            }
        }
    }

    private void copyWithNewStatus(String key, ObjectMetadata metadata, WorkStatus workStatus) {
        metadata.getUserMetadata().put(Meta.WORK_STATUS, workStatus.c());
        am.s3$copyObject(key, key, metadata, CannedAccessControlList.Private);
    }
}

