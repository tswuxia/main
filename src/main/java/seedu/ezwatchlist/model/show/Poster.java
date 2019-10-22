package seedu.ezwatchlist.model.show;

import java.io.File;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import seedu.ezwatchlist.api.ImageRetrieval;

/**
 * Represents a Show's poster in the watchlist.
 */
public class Poster {
    private static final String PLACEHOLDER_IMAGE = "/images/poster-placeholder.png";
    private Image image;
    private String imagePath;

    /**
     * Constructs a {@code Poster}.
     */
    public Poster() {
        imagePath = PLACEHOLDER_IMAGE;
    }

    /**
     * Constructs a {@code Poster} with a path given.
     * @param path the path of the image in the save location.
     */
    public Poster(String path) {
        imagePath = path;
    }

    /**
     * returns the image of the Poster.
     */
    public Image getImage() {
        try {
            String ss = ImageRetrieval.IMAGE_CACHE_LOCATION + File.separator + imagePath;
            File file = new File(ss);
            System.out.println("File path in Poster is :" + ss);
            image = SwingFXUtils.toFXImage(ImageIO.read(file), null);

            if (image == null) {
                throw new NullPointerException("image is null in poster");
            }
            return image;
        } catch (IIOException i) {
            System.err.print(i.getMessage() + " in Poster");
            return new Image(PLACEHOLDER_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Cause: " + e.getCause() + "Message: " + e.getMessage() + " from Poster and imagePath ");
            return new Image(PLACEHOLDER_IMAGE);
        }
    }
}
