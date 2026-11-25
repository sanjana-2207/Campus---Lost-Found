package com.campus.web.config;

import com.campus.web.model.Category;
import com.campus.web.model.Claim;
import com.campus.web.model.ClaimStatus;
import com.campus.web.model.Item;
import com.campus.web.model.ItemStatus;
import com.campus.web.model.ItemType;
import com.campus.web.model.Location;
import com.campus.web.model.Role;
import com.campus.web.model.User;
import com.campus.web.repository.CategoryRepository;
import com.campus.web.repository.ClaimRepository;
import com.campus.web.repository.ItemRepository;
import com.campus.web.repository.LocationRepository;
import com.campus.web.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository users, CategoryRepository categories,
                               LocationRepository locations, ItemRepository items,
                               ClaimRepository claims, PasswordEncoder encoder) {
        return args -> {
            if (users.count() > 0) {
                return;
            }

            Map<String, Category> categoryMap = new LinkedHashMap<>();
            for (String name : new String[]{
                    "Electronics", "ID Cards", "Keys", "Books & Notes", "Bags",
                    "Clothing", "Accessories", "Sports Equipment", "Other"}) {
                Category category = categories.save(new Category(name));
                categoryMap.put(name, category);
            }

            Map<String, Location> locationMap = new LinkedHashMap<>();
            for (String name : new String[]{
                    "A Block", "B Block", "C Block", "Sixth Sense", "Heca",
                    "Idea Factory", "D Block", "Boys Mess", "NRI Hostel",
                    "Girls Hostel", "Rec Cafe", "Hut Cafe", "Blackbug",
                    "Dominos", "Maingate"}) {
                Location location = locations.save(new Location(name));
                locationMap.put(name, location);
            }

            user(users, encoder, "Dr. Ramesh Kumar", "admin@campus.edu",
                    "9840010001", "admin123", Role.ADMIN);
            User security = user(users, encoder, "Suresh Babu", "security@campus.edu",
                    "9840010002", "staff123", Role.STAFF);
            User arjun = user(users, encoder, "Arjun Mehta", "arjun@campus.edu",
                    "9840010003", "student123", Role.STUDENT);
            User priya = user(users, encoder, "Priya Nair", "priya@campus.edu",
                    "9840010004", "student123", Role.STUDENT);
            User karthik = user(users, encoder, "Karthik Rao", "karthik@campus.edu",
                    "9840010005", "student123", Role.STUDENT);

            LocalDateTime now = LocalDateTime.now();
            List<Item> seedItems = new ArrayList<>();

            seedItems.add(lost(items, "Black Dell laptop charger",
                    "65W Dell charger with a small white label wrapped around the cable.",
                    categoryMap.get("Electronics"), locationMap.get("C Block"),
                    LocalDate.of(2026, 10, 10), arjun, ItemStatus.OPEN,
                    now.minusDays(20).withHour(9).withMinute(15)));

            seedItems.add(found(items, "Dell 65W charger with black cable",
                    "Charger found on the charging point in CS Block Lab 3. Cable has a white label.",
                    categoryMap.get("Electronics"), locationMap.get("C Block"),
                    LocalDate.of(2026, 10, 11), security, ItemStatus.OPEN,
                    "CS Block - Lab 3 cupboard", true, "Cable tagged with a white label.",
                    now.minusDays(19).withHour(10).withMinute(30), photo("Dell charger")));

            seedItems.add(lost(items, "Blue Milton steel water bottle",
                    "One litre steel bottle with a Chennai Super Kings sticker on the body.",
                    categoryMap.get("Accessories"), locationMap.get("B Block"),
                    LocalDate.of(2026, 10, 18), priya, ItemStatus.OPEN,
                    now.minusDays(16).withHour(8).withMinute(40)));

            seedItems.add(found(items, "Steel water bottle - blue with CSK sticker",
                    "Blue steel bottle left on canteen table 4. Kept at the canteen counter.",
                    categoryMap.get("Accessories"), locationMap.get("B Block"),
                    LocalDate.of(2026, 10, 19), security, ItemStatus.OPEN,
                    "Canteen counter", true, "Washed and kept at the counter.",
                    now.minusDays(15).withHour(12).withMinute(10), photo("Blue bottle")));

            seedItems.add(lost(items, "Student ID card - Arjun Mehta",
                    "Student ID card with a red lanyard. Roll number 21CS1043.",
                    categoryMap.get("ID Cards"), locationMap.get("C Block"),
                    LocalDate.of(2026, 10, 22), arjun, ItemStatus.OPEN,
                    now.minusDays(12).withHour(9).withMinute(5)));

            seedItems.add(found(items, "ID card near CS Block notice board",
                    "Student ID card with a red lanyard found near the CS Block notice board.",
                    categoryMap.get("ID Cards"), locationMap.get("C Block"),
                    LocalDate.of(2026, 10, 22), security, ItemStatus.RECOVERED,
                    "Security office", true, "Owner verified using the college register.",
                    now.minusDays(12).withHour(16).withMinute(45), photo("Student ID")));

            seedItems.add(lost(items, "Hostel room keys on a red key ring",
                    "Two keys on a red key ring with a small temple token attached.",
                    categoryMap.get("Keys"), locationMap.get("Sixth Sense"),
                    LocalDate.of(2026, 10, 5), karthik, ItemStatus.OPEN,
                    now.minusDays(24).withHour(7).withMinute(50)));

            Item keys = found(items, "Bunch of keys with red key ring",
                    "Two keys and a temple token, handed over to the hostel warden.",
                    categoryMap.get("Keys"), locationMap.get("Sixth Sense"),
                    LocalDate.of(2026, 10, 6), security, ItemStatus.CLAIMED,
                    "Hostel warden office", true, "Keys matched the room number in the register.",
                    now.minusDays(23).withHour(9).withMinute(20), photo("Keys"));
            seedItems.add(keys);

            seedItems.add(lost(items, "Logitech M235 wireless mouse",
                    "Black wireless mouse. USB receiver is stored inside the battery cover.",
                    categoryMap.get("Electronics"), locationMap.get("Heca"),
                    LocalDate.of(2026, 10, 12), karthik, ItemStatus.OPEN,
                    now.minusDays(18).withHour(11).withMinute(5)));

            seedItems.add(found(items, "Logitech wireless mouse with receiver",
                    "Black wireless mouse found near the parking lot gate.",
                    categoryMap.get("Electronics"), locationMap.get("Heca"),
                    LocalDate.of(2026, 10, 12), security, ItemStatus.OPEN,
                    "Security office", true, null,
                    now.minusDays(18).withHour(13).withMinute(30), null));

            seedItems.add(found(items, "Prescription spectacles in brown case",
                    "Black frame spectacles inside a brown case, found in the library reading room.",
                    categoryMap.get("Accessories"), locationMap.get("A Block"),
                    LocalDate.of(2026, 10, 25), security, ItemStatus.OPEN,
                    "Library reading room desk", false, "Handed to the librarian on duty.",
                    now.minusDays(9).withHour(14).withMinute(0), photo("Spectacles")));

            seedItems.add(lost(items, "Brown leather wallet",
                    "Wallet containing college ID and metro card. Cash is not important.",
                    categoryMap.get("Accessories"), locationMap.get("Idea Factory"),
                    LocalDate.of(2026, 10, 28), priya, ItemStatus.OPEN,
                    now.minusDays(7).withHour(17).withMinute(10)));

            seedItems.add(lost(items, "Casio fx-991 scientific calculator",
                    "Name written on the back cover: Priya N.",
                    categoryMap.get("Electronics"), locationMap.get("C Block"),
                    LocalDate.of(2026, 10, 20), priya, ItemStatus.OPEN,
                    now.minusDays(14).withHour(10).withMinute(0)));

            Item calculator = found(items, "Casio calculator with a name on the back",
                    "Scientific calculator found in CS Block room 204.",
                    categoryMap.get("Electronics"), locationMap.get("C Block"),
                    LocalDate.of(2026, 10, 20), security, ItemStatus.OPEN,
                    "CS Block room 204", true, null,
                    now.minusDays(14).withHour(11).withMinute(15), photo("Calculator"));
            seedItems.add(calculator);

            Item spectacles = seedItems.stream()
                    .filter(i -> i.getTitle().startsWith("Prescription spectacles"))
                    .findFirst().orElseThrow();
            Item idCard = seedItems.stream()
                    .filter(i -> i.getTitle().startsWith("ID card near"))
                    .findFirst().orElseThrow();

            claim(claims, keys, karthik, security, ClaimStatus.APPROVED,
                    "I lost two keys on a red ring with a temple token on 5 October near Hostel Block A. The warden can confirm my room number.",
                    "Verified against the hostel register.", now.minusDays(22));
            claim(claims, calculator, karthik, null, ClaimStatus.PENDING,
                    "Black Casio fx-991. My roll number 21CS1057 is written inside the battery cover, and the slide case has a hairline scratch on the front.",
                    null, now.minusDays(10));
            claim(claims, calculator, priya, null, ClaimStatus.PENDING,
                    "Casio fx-991 with \"Priya N.\" written on the back cover.",
                    null, now.minusDays(10).plusHours(1));
            claim(claims, idCard, arjun, security, ClaimStatus.RECOVERED,
                    "My student ID card, roll number 21CS1043, with a red lanyard.",
                    "Returned at the security office after ID verification.",
                    now.minusDays(11));
            claim(claims, spectacles, arjun, security, ClaimStatus.REJECTED,
                    "Black frame spectacles in a brown case.",
                    "Frame colour and case do not match the item found.",
                    now.minusDays(8));
        };
    }

    private User user(UserRepository users, PasswordEncoder encoder, String name,
                      String email, String phone, String password, Role role) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(role);
        return users.save(user);
    }

    private Item lost(ItemRepository items, String title, String description,
                      Category category, Location location, LocalDate date,
                      User reporter, ItemStatus status, LocalDateTime createdAt) {
        Item item = new Item();
        item.setType(ItemType.LOST);
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setLocation(location);
        item.setEventDate(date);
        item.setReporter(reporter);
        item.setStatus(status);
        item.setCreatedAt(createdAt);
        item.setPhoto(photo(title));
        item.setPhotoContentType("image/png");
        return items.save(item);
    }

    private Item found(ItemRepository items, String title, String description,
                       Category category, Location location, LocalDate date,
                       User reporter, ItemStatus status, String storage,
                       boolean handedToSecurity, String notes,
                       LocalDateTime createdAt, byte[] photo) {
        Item item = new Item();
        item.setType(ItemType.FOUND);
        item.setTitle(title);
        item.setDescription(description);
        item.setCategory(category);
        item.setLocation(location);
        item.setEventDate(date);
        item.setReporter(reporter);
        item.setStatus(status);
        item.setStorageLocation(storage);
        item.setHandedToSecurity(handedToSecurity);
        item.setFinderNotes(notes);
        item.setCreatedAt(createdAt);
        item.setPhoto(photo != null ? photo : photo(title));
        item.setPhotoContentType("image/png");
        return items.save(item);
    }

    private void claim(ClaimRepository claims, Item item, User claimant, User verifier,
                       ClaimStatus status, String proof, String remarks,
                       LocalDateTime submittedAt) {
        Claim claim = new Claim();
        claim.setItem(item);
        claim.setClaimant(claimant);
        claim.setProof(proof);
        claim.setStatus(status);
        claim.setSubmittedAt(submittedAt);
        claim.setRemarks(remarks);
        if (verifier != null) {
            claim.setVerifiedBy(verifier);
            claim.setVerifiedAt(submittedAt.plusHours(5));
        }
        claims.save(claim);
    }

    private byte[] photo(String label) {
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

            drawItemIcon(g, label, width, height, base);

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

    private void drawItemIcon(Graphics2D g, String label, int width, int height,
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

    private List<String> wrapLabel(Graphics2D g, String label, int maxWidth) {
        List<String> lines = new ArrayList<>();
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
