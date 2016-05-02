package p.lodz.pl.adi.utils;


import com.amazonaws.services.devicefarm.model.ArgumentException;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public class ImageResizer {

    private final String[] PROPER_IMG_TYPES = {
            "PNG", "GIF", "TIFF",
            "JPG", "JPEG", "TIFF"
    };

    /**
     * @throws IOException       if resizing failed due to IO
     * @throws ArgumentException if resizing failed due to arguments
     */
    public InputStreamEnh resize(InputStream is, int sizeMultiplier, String imageType) throws IOException {
        ensureImageType(imageType);
        ensureSizeMultiplier(sizeMultiplier);

        BufferedImage srcImage = ImageIO.read(is);

        double sizeMultiplier2 = sizeMultiplier / 100.0;
        int newWidth = (int) (srcImage.getWidth() * sizeMultiplier2);
        int newHeight = (int) (srcImage.getHeight() * sizeMultiplier2);

        BufferedImage scaledImage = Scalr.resize(srcImage, newWidth, newHeight);

        return makeResult(scaledImage, imageType);
    }

    private InputStreamEnh makeResult(RenderedImage image, String imageType) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, imageType, os);

        byte[] bytes = os.toByteArray();
        InputStream is = new ByteArrayInputStream(bytes);
        return new InputStreamEnh(is, bytes.length);
    }

    private void ensureImageType(String imageType) {
        if (!Stream.of(PROPER_IMG_TYPES).anyMatch(p -> p.equals(imageType))) {
            throw new ArgumentException("imageType");
        }
    }

    private void ensureSizeMultiplier(int sizeMultiplier) {
        if (sizeMultiplier < 1 || sizeMultiplier > 200) {
            throw new ArgumentException("sizeMultiplier");
        }
    }
}
