package com.campus.lostfound.util;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemFilter;
import com.campus.lostfound.service.ItemService;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Generates the demo photos used by the seeded reports so the desktop tables
 * and dialogs have something to show on a fresh database.
 */
public final class DemoPhotos {

    private static final Set<String> DEMO_TITLES = new HashSet<String>(Arrays.asList(
            "Black Dell laptop charger",
            "Dell 65W charger with black cable",
            "Blue Milton steel water bottle",
            "Steel water bottle - blue with CSK sticker",
            "Student ID card - Arjun Mehta",
            "ID card near CS Block notice board",
            "Hostel room keys on a red key ring",
            "Bunch of keys with red key ring",
            "Logitech M235 wireless mouse",
            "Logitech wireless mouse with receiver",
            "Prescription spectacles in brown case",
            "Brown leather wallet",
            "Casio fx-991 scientific calculator",
            "Casio calculator with a name on the back"));

    private DemoPhotos() {
    }

    /** Creates a picture for every seeded report that has none yet. */
    public static void ensureDemoPhotos(ItemService itemService) {
        try {
            List<Item> items = itemService.search(new ItemFilter());
            for (Item item : items) {
                if (!DEMO_TITLES.contains(item.getTitle())) {
                    continue;
                }
                byte[] bundled = bundledPhoto(item.getTitle());
                if (bundled != null) {
                    ImageStore.saveBytesForItem(item.getItemId(), bundled, "jpg");
                    continue;
                }
                if (ImageStore.hasPhoto(item.getItemId())) {
                    continue;
                }
                byte[] png = render(item.getTitle());
                if (png != null) {
                    ImageStore.saveBytesForItem(item.getItemId(), png, "png");
                }
            }
        } catch (Exception ex) {
            // Demo photos are optional and must never stop the app from starting.
        }
    }

    private static byte[] bundledPhoto(String label) {
        String file = demoImageFile(label);
        if (file == null) {
            return null;
        }
        try (InputStream in = DemoPhotos.class.getResourceAsStream("/demo/" + file)) {
            return in == null ? null : readAll(in);
        } catch (IOException ex) {
            return null;
        }
    }

    private static String demoImageFile(String label) {
        String key = label == null ? "" : label.toLowerCase(Locale.ROOT);
        if (key.contains("charger")) {
            return "charger.jpg";
        }
        if (key.contains("bottle")) {
            return "bottle.jpg";
        }
        if (key.contains("id card") || key.contains("card")) {
            return "id-card.jpg";
        }
        if (key.contains("key")) {
            return "keys.jpg";
        }
        if (key.contains("mouse")) {
            return "mouse.jpg";
        }
        if (key.contains("spectacle") || key.contains("glasses")) {
            return "spectacles.jpg";
        }
        if (key.contains("wallet")) {
            return "wallet.jpg";
        }
        if (key.contains("calculator")) {
            return "calculator.jpg";
        }
        return null;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static byte[] render(String label) {
        try {
            int width = 640;
            int height = 440;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color[] palette = {
                    new Color(0x6D28D9), new Color(0x2563EB), new Color(0x0F766E),
                    new Color(0xB45309), new Color(0xBE123C), new Color(0x4338CA)
            };
            Color base = palette[Math.floorMod(label.hashCode(), palette.length)];
            g.setPaint(new GradientPaint(0, 0, base.brighter(), width, height, base.darker()));
            g.fillRect(0, 0, width, height);

            g.setColor(new Color(255, 255, 255, 28));
            g.fillOval(-120, -120, 420, 420);
            g.fillOval(width - 260, height - 220, 380, 380);

            drawIcon(g, label, width, height, base);

            g.setColor(new Color(255, 255, 255, 238));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
            List<String> lines = wrapLabel(g, label, width - 120);
            int lineHeight = g.getFontMetrics().getHeight();
            int y = (int) (height * 0.70);
            for (String line : lines) {
                int x = (width - g.getFontMetrics().stringWidth(line)) / 2;
                g.drawString(line, x, y);
                y += lineHeight;
            }

            g.setColor(new Color(255, 255, 255, 170));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
            String caption = "REC LOST & FOUND - DEMO PHOTO";
            int captionX = (width - g.getFontMetrics().stringWidth(caption)) / 2;
            g.drawString(caption, captionX, height - 34);

            g.dispose();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        } catch (Exception ex) {
            return null;
        }
    }

    private static void drawIcon(Graphics2D g, String label, int width, int height,
                                 Color base) {
        String key = label == null ? "" : label.toLowerCase(Locale.ROOT);
        int cx = width / 2;
        int cy = (int) (height * 0.38);
        g.setColor(new Color(255, 255, 255, 238));

        if (key.contains("charger") || key.contains("cable")) {
            g.fill(new RoundRectangle2D.Double(cx - 45, cy - 45, 90, 72, 20, 20));
            g.fill(new Rectangle2D.Double(cx - 24, cy - 72, 13, 30));
            g.fill(new Rectangle2D.Double(cx + 11, cy - 72, 13, 30));
            g.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(cx + 40, cy - 5, 95, 95, 280, 130, Arc2D.OPEN));
            g.setColor(new Color(0, 0, 0, 55));
            g.fill(new RoundRectangle2D.Double(cx - 18, cy - 18, 36, 12, 8, 8));
        } else if (key.contains("bottle") || key.contains("water")) {
            g.fill(new RoundRectangle2D.Double(cx - 34, cy - 55, 68, 118, 28, 28));
            g.fill(new Rectangle2D.Double(cx - 14, cy - 82, 28, 32));
            g.fill(new RoundRectangle2D.Double(cx - 19, cy - 101, 38, 22, 10, 10));
            g.setColor(new Color(255, 255, 255, 110));
            g.fill(new RoundRectangle2D.Double(cx - 20, cy - 34, 12, 72, 8, 8));
        } else if (key.contains("id card") || key.contains("card")) {
            g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(cx - 52, cy - 105, 104, 86, 200, 140, Arc2D.OPEN));
            g.fill(new RoundRectangle2D.Double(cx - 82, cy - 48, 164, 104, 16, 16));
            g.setColor(base.darker());
            g.fill(new Ellipse2D.Double(cx - 62, cy - 26, 36, 36));
            g.fill(new RoundRectangle2D.Double(cx - 18, cy - 24, 76, 10, 6, 6));
            g.fill(new RoundRectangle2D.Double(cx - 18, cy - 4, 60, 8, 6, 6));
            g.fill(new RoundRectangle2D.Double(cx - 62, cy + 24, 118, 8, 6, 6));
        } else if (key.contains("key")) {
            g.setStroke(new BasicStroke(13f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(cx - 70, cy - 55, 76, 76));
            g.draw(new java.awt.geom.Line2D.Double(cx - 6, cy + 2, cx + 78, cy + 2));
            g.draw(new java.awt.geom.Line2D.Double(cx + 44, cy + 2, cx + 44, cy + 28));
            g.draw(new java.awt.geom.Line2D.Double(cx + 66, cy + 2, cx + 66, cy + 20));
        } else if (key.contains("mouse")) {
            g.fill(new RoundRectangle2D.Double(cx - 32, cy - 62, 64, 116, 32, 32));
            g.setColor(new Color(0, 0, 0, 55));
            g.fill(new RoundRectangle2D.Double(cx - 4, cy - 48, 8, 26, 6, 6));
            g.setColor(new Color(255, 255, 255, 238));
            g.fill(new RoundRectangle2D.Double(cx - 3, cy - 4, 6, 46, 6, 6));
        } else if (key.contains("spectacle") || key.contains("glasses")) {
            g.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(cx - 82, cy - 34, 66, 60));
            g.draw(new Ellipse2D.Double(cx + 16, cy - 34, 66, 60));
            g.draw(new java.awt.geom.Line2D.Double(cx - 16, cy - 10, cx + 16, cy - 10));
            g.draw(new java.awt.geom.Line2D.Double(cx - 82, cy - 20, cx - 112, cy - 46));
            g.draw(new java.awt.geom.Line2D.Double(cx + 82, cy - 20, cx + 112, cy - 46));
        } else if (key.contains("wallet")) {
            g.fill(new RoundRectangle2D.Double(cx - 74, cy - 46, 148, 92, 16, 16));
            g.setColor(base.darker());
            g.fill(new RoundRectangle2D.Double(cx - 56, cy - 26, 112, 16, 8, 8));
            g.fill(new Ellipse2D.Double(cx + 34, cy + 8, 20, 20));
        } else if (key.contains("calculator")) {
            g.fill(new RoundRectangle2D.Double(cx - 52, cy - 74, 104, 148, 14, 14));
            g.setColor(new Color(0, 0, 0, 70));
            g.fill(new RoundRectangle2D.Double(cx - 34, cy - 56, 68, 28, 6, 6));
            g.setColor(base.darker());
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    g.fill(new Ellipse2D.Double(
                            cx - 38 + c * 26, cy - 12 + r * 24, 16, 16));
                }
            }
        } else {
            g.fill(new RoundRectangle2D.Double(cx - 72, cy - 48, 144, 96, 14, 14));
            g.setColor(new Color(0, 0, 0, 55));
            g.fill(new Rectangle2D.Double(cx - 10, cy - 48, 20, 96));
        }
    }

    private static List<String> wrapLabel(Graphics2D g, String label, int maxWidth) {
        java.util.ArrayList<String> lines = new java.util.ArrayList<String>();
        StringBuilder line = new StringBuilder();
        for (String word : label.split("\\s+")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (line.length() > 0 && g.getFontMetrics().stringWidth(candidate) > maxWidth) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}
