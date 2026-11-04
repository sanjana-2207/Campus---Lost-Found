package com.campus.lostfound.ui;

import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.FoundItem;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.Location;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.ImageStore;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Form used to report a FOUND item.
 *
 * <p>The form asks where the item is kept and who is holding it, because those
 * are the details the owner needs before an ownership claim can be made.</p>
 */
public class ReportItemPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final ItemService itemService;
    private final Runnable onBack;

    private final JTextField titleField = UIStyle.textField();
    private final JTextArea descriptionArea = UIStyle.textArea();
    private final JComboBox<Category> categoryBox = UIStyle.comboBox();
    private final JComboBox<Location> locationBox = UIStyle.comboBox();
    private final JSpinner dateSpinner;
    private final JTextField storageField = UIStyle.textField();
    private final JTextField finderNotesField = UIStyle.textField();
    private final JCheckBox handedCheck = new JCheckBox("Handed over to the security office", true);
    private final JLabel photoPreview = new JLabel();
    private File selectedPhoto;

    private final JLabel messageLabel = new JLabel(" ");
    private boolean lookupsLoaded;

    public ReportItemPanel(ItemService itemService, Runnable onBack) {
        this.itemService = itemService;
        this.onBack = onBack;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        dateSpinner = buildDateSpinner();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildFormScroll(), BorderLayout.CENTER);
    }

    // ==================================================================
    //  Layout
    // ==================================================================

    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = UIStyle.heading("Report a found item");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Describe the item you found and where you are keeping it.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(8));
        header.add(UIStyle.backLink(onBack));
        return header;
    }

    private JComponent buildFormScroll() {
        JPanel card = UIStyle.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // --- title ------------------------------------------------------
        titleField.setToolTipText("For example: Black Dell laptop charger");
        card.add(UIStyle.labeled("Item title *", titleField));
        card.add(Box.createVerticalStrut(12));

        // --- category + location ---------------------------------------
        card.add(twoColumns(
                UIStyle.labeled("Category *", categoryBox),
                UIStyle.labeled("Location *", locationBox)));

        // --- date + handover -------------------------------------------
        card.add(twoColumns(
                UIStyle.labeled("Date of incident *", dateSpinner),
                buildHandoverField()));
        card.add(Box.createVerticalStrut(12));

        // --- storage + notes -------------------------------------------
        card.add(twoColumns(
                UIStyle.labeled("Where is the item kept? *", storageField),
                UIStyle.labeled("Finder notes (optional)", finderNotesField)));
        card.add(Box.createVerticalStrut(12));

        // --- description -------------------------------------------------
        descriptionArea.setRows(4);
        descriptionArea.setPreferredSize(new Dimension(400, 96));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));
        descriptionScroll.getViewport().setBackground(UIStyle.CARD);

        JPanel descriptionRow = new JPanel(new BorderLayout(0, 4));
        descriptionRow.setOpaque(false);
        descriptionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 126));
        descriptionRow.add(UIStyle.fieldLabel("Description *  colour, brand, marks, contents"),
                BorderLayout.NORTH);
        descriptionRow.add(descriptionScroll, BorderLayout.CENTER);
        card.add(descriptionRow);
        card.add(Box.createVerticalStrut(12));

        // --- photo ------------------------------------------------------
        card.add(buildPhotoSection());
        card.add(Box.createVerticalStrut(16));

        // --- message + submit -------------------------------------------
        messageLabel.setFont(UIStyle.FONT_SMALL);
        messageLabel.setForeground(UIStyle.DANGER);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submit = UIStyle.primaryButton("Submit report");
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                submitReport();
            }
        });
        JButton clear = UIStyle.secondaryButton("Clear form");
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                resetForm();
            }
        });
        actions.add(submit);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(clear);

        card.add(actions);
        card.add(Box.createVerticalStrut(6));
        card.add(messageLabel);

        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UIStyle.BG);
        scroll.setBackground(UIStyle.BG);
        return scroll;
    }

    private JComponent buildHandoverField() {
        JPanel foundCard = new JPanel(new BorderLayout(0, 4));
        foundCard.setOpaque(false);
        foundCard.add(UIStyle.fieldLabel("Handover"), BorderLayout.NORTH);
        styleCheckBox(handedCheck);
        foundCard.add(handedCheck, BorderLayout.CENTER);
        return foundCard;
    }

    private JComponent buildPhotoSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = UIStyle.fieldLabel("Photo (optional)");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(label);
        section.add(Box.createVerticalStrut(6));

        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        photoPreview.setPreferredSize(new Dimension(210, 150));
        photoPreview.setMaximumSize(new Dimension(210, 150));
        photoPreview.setHorizontalAlignment(JLabel.CENTER);
        photoPreview.setVerticalAlignment(JLabel.CENTER);
        photoPreview.setOpaque(true);
        photoPreview.setBackground(UIStyle.blend(UIStyle.CARD, UIStyle.TEXT, 0.06f));
        photoPreview.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));
        photoPreview.setFont(UIStyle.FONT_SMALL);
        photoPreview.setForeground(UIStyle.MUTED);
        photoPreview.setText("No photo selected");

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        JButton choose = UIStyle.secondaryButton("Choose photo");
        choose.setAlignmentX(Component.LEFT_ALIGNMENT);
        choose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                choosePhoto();
            }
        });

        JButton remove = UIStyle.secondaryButton("Remove photo");
        remove.setAlignmentX(Component.LEFT_ALIGNMENT);
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                clearPhoto();
            }
        });

        JLabel hint = UIStyle.muted("JPG, PNG, GIF or BMP - up to 8 MB");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        controls.add(choose);
        controls.add(Box.createVerticalStrut(8));
        controls.add(remove);
        controls.add(Box.createVerticalStrut(8));
        controls.add(hint);

        row.add(photoPreview, BorderLayout.WEST);
        row.add(controls, BorderLayout.CENTER);
        section.add(row);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        return section;
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a photo of the found item");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Images (JPG, PNG, GIF, BMP)", "jpg", "jpeg", "png", "gif", "bmp"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            ImageStore.validate(file);
            ImageIcon preview = ImageStore.loadFile(file, 204, 144);
            if (preview == null) {
                throw new ValidationException("The photo could not be previewed.");
            }
            selectedPhoto = file;
            photoPreview.setIcon(preview);
            photoPreview.setText("");
            messageLabel.setText(" ");
        } catch (ValidationException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearPhoto() {
        selectedPhoto = null;
        photoPreview.setIcon(null);
        photoPreview.setText("No photo selected");
    }

    private JComponent twoColumns(JComponent left, JComponent right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.add(left);
        row.add(right);
        return row;
    }

    private JSpinner buildDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, new Date(),
                Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd-MM-yyyy");
        spinner.setEditor(editor);
        UIStyle.styleSpinner(spinner);
        spinner.setPreferredSize(new Dimension(160, 34));
        return spinner;
    }

    private void styleCheckBox(JCheckBox box) {
        box.setFont(UIStyle.FONT);
        box.setForeground(UIStyle.TEXT);
        box.setBackground(UIStyle.CARD);
        box.setFocusPainted(false);
    }

    // ==================================================================
    //  Behaviour
    // ==================================================================

    @Override
    public void refreshData() {
        if (lookupsLoaded) {
            return;
        }
        try {
            List<Category> categories = itemService.categories();
            List<Location> locations = itemService.locations();

            categoryBox.removeAllItems();
            for (Category category : categories) {
                categoryBox.addItem(category);
            }
            locationBox.removeAllItems();
            for (Location location : locations) {
                locationBox.addItem(location);
            }
            lookupsLoaded = !categories.isEmpty() && !locations.isEmpty();

            if (!lookupsLoaded) {
                showError("Category or location master data is empty. "
                        + "Import database/schema.sql again.");
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    // ==================================================================
    //  Submit
    // ==================================================================

    private void submitReport() {
        messageLabel.setText(" ");
        try {
            Item item = buildItem();
            int id = itemService.reportItem(item);

            boolean photoSaved = true;
            if (selectedPhoto != null) {
                try {
                    ImageStore.saveForItem(id, selectedPhoto);
                } catch (IOException ex) {
                    photoSaved = false;
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Report #" + id + " saved.\n\n"
                    + (photoSaved
                            ? "Owners can now find it in Search and claim."
                            : "The report was saved, but the photo could not be "
                              + "stored. You can still find it in My Reports."),
                    "Report submitted", JOptionPane.INFORMATION_MESSAGE);

            resetForm();
        } catch (ValidationException ex) {
            showError(ex.getMessage());
        } catch (RuntimeException ex) {
            messageLabel.setText("Could not save the report. See the error dialog.");
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Item buildItem() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        Category category = (Category) categoryBox.getSelectedItem();
        Location location = (Location) locationBox.getSelectedItem();
        LocalDate date = toLocalDate(dateSpinner.getValue());
        User reporter = SessionContext.getCurrentUser();

        return new FoundItem(title, description, category, location, date, reporter,
                storageField.getText(),
                handedCheck.isSelected(),
                finderNotesField.getText());
    }

    private void resetForm() {
        titleField.setText("");
        descriptionArea.setText("");
        storageField.setText("");
        finderNotesField.setText("");
        clearPhoto();
        handedCheck.setSelected(true);
        dateSpinner.setValue(new Date());
        if (categoryBox.getItemCount() > 0) {
            categoryBox.setSelectedIndex(0);
        }
        if (locationBox.getItemCount() > 0) {
            locationBox.setSelectedIndex(0);
        }
        messageLabel.setText(" ");
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return LocalDate.now();
    }

    private void showError(String text) {
        messageLabel.setText(text);
    }
}
