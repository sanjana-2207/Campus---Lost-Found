package com.campus.lostfound.util;

import com.campus.lostfound.exception.ValidationException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

/**
 * Stores item photos beside the application as {@code uploads/item_<id>.<ext>}
 * and loads them back as scaled thumbnails.
 *
 * <p>Keeping photos on disk instead of in MySQL avoids adding a large BLOB
 * column and keeps the database schema unchanged. The item id in the filename
 * is enough to find the picture again when a table or dialog asks for it.</p>
 */
public final class ImageStore {

    private static final long MAX_BYTES = 8L * 1024L * 1024L;
    private static final String[] EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    private static final File UPLOAD_DIR =
            new File(System.getProperty("user.dir"), "uploads");

    private ImageStore() {
    }

    /** Validates a file chosen in the report form. */
    public static void validate(File file) {
        if (file == null || !file.isFile()) {
            throw new ValidationException("Choose a valid image file.");
        }
        if (file.length() > MAX_BYTES) {
            throw new ValidationException("The photo must be 8 MB or smaller.");
        }
        if (!isSupported(file)) {
            throw new ValidationException(
                    "Choose a JPG, PNG, GIF or BMP image.");
        }
        try {
            if (ImageIO.read(file) == null) {
                throw new ValidationException("That file is not a readable image.");
            }
        } catch (IOException ex) {
            throw new ValidationException("The photo could not be read.");
        }
    }

    /** Copies the chosen photo to uploads/item_<id>.<ext>. */
    public static void saveForItem(int itemId, File source) throws IOException {
        validate(source);
        if (!UPLOAD_DIR.exists() && !UPLOAD_DIR.mkdirs()) {
            throw new IOException("Could not create the uploads folder.");
        }

        deleteForItem(itemId);
        String extension = extensionOf(source);
        File target = new File(UPLOAD_DIR, "item_" + itemId + "." + extension);
        Files.copy(source.toPath(), target.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /** Scaled thumbnail for an item, or null when it has no photo. */
    public static ImageIcon loadForItem(int itemId, int maxWidth, int maxHeight) {
        File file = findForItem(itemId);
        return file == null ? null : loadFile(file, maxWidth, maxHeight);
    }

    /** Scaled preview for a file chosen in the form. */
    public static ImageIcon loadFile(File file, int maxWidth, int maxHeight) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return null;
            }
            double scale = Math.min(
                    (double) maxWidth / image.getWidth(),
                    (double) maxHeight / image.getHeight());
            int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight() * scale));

            BufferedImage scaled = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH),
                    0, 0, null);
            g2.dispose();
            return new ImageIcon(scaled);
        } catch (IOException ex) {
            return null;
        }
    }

    public static boolean hasPhoto(int itemId) {
        return findForItem(itemId) != null;
    }

    private static File findForItem(int itemId) {
        for (String extension : EXTENSIONS) {
            File file = new File(UPLOAD_DIR, "item_" + itemId + "." + extension);
            if (file.isFile()) {
                return file;
            }
        }
        return null;
    }

    private static void deleteForItem(int itemId) {
        for (String extension : EXTENSIONS) {
            File file = new File(UPLOAD_DIR, "item_" + itemId + "." + extension);
            if (file.isFile() && !file.delete()) {
                // A leftover file is harmless; the new copy still replaces the
                // extension it uses.
            }
        }
    }

    private static boolean isSupported(File file) {
        String extension = extensionOf(file);
        for (String supported : EXTENSIONS) {
            if (supported.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private static String extensionOf(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1);
    }
}
