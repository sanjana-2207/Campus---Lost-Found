package com.campus.lostfound.ui;

import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.FoundItem;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.Location;
import com.campus.lostfound.model.LostItem;
import com.campus.lostfound.model.Urgency;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Form used to report a LOST or a FOUND item.
 *
 * <p>The part of the form that changes with the report type sits in a small
 * {@link CardLayout}: a lost report asks for contact details and a reward, a
 * found report asks where the item is stored. The shared fields stay in place,
 * which is the visual equivalent of the {@code Item} super-class.</p>
 */
public class ReportItemPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final ItemService itemService;

    private final JRadioButton lostRadio = new JRadioButton("I lost an item", true);
    private final JRadioButton foundRadio = new JRadioButton("I found an item");
    private final CardLayout extraCards = new CardLayout();
    private final JPanel extraPanel = new JPanel(extraCards);

    private final JTextField titleField = UIStyle.textField();
    private final JTextArea descriptionArea = UIStyle.textArea();
    private final JComboBox<Category> categoryBox = UIStyle.comboBox();
    private final JComboBox<Location> locationBox = UIStyle.comboBox();
    private final JSpinner dateSpinner;
    private final JComboBox<Urgency> urgencyBox = UIStyle.comboBox();
    private final JTextField contactField = UIStyle.textField();
    private final JTextField rewardField = UIStyle.textField();
    private final JTextField storageField = UIStyle.textField();
    private final JTextField finderNotesField = UIStyle.textField();
    private final JCheckBox handedCheck = new JCheckBox("Handed over to the security office", true);

    private final JLabel messageLabel = new JLabel(" ");
    private boolean lookupsLoaded;

    public ReportItemPanel(ItemService itemService) {
        this.itemService = itemService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        dateSpinner = buildDateSpinner();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildFormScroll(), BorderLayout.CENTER);

        wireTypeToggle();
    }

    // ==================================================================
    //  Layout
    // ==================================================================

    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = UIStyle.heading("Report an item");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Describe the item clearly - a good description is what makes a match possible.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        return header;
    }

    private JComponent buildFormScroll() {
        JPanel card = UIStyle.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // --- report type ------------------------------------------------
        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        typeRow.setOpaque(false);
        typeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleRadio(lostRadio);
        styleRadio(foundRadio);
        ButtonGroup group = new ButtonGroup();
        group.add(lostRadio);
        group.add(foundRadio);
        typeRow.add(lostRadio);
        typeRow.add(foundRadio);
        card.add(UIStyle.fieldLabel("What happened?"));
        card.add(Box.createVerticalStrut(4));
        card.add(typeRow);
        card.add(Box.createVerticalStrut(16));

        // --- title ------------------------------------------------------
        titleField.setToolTipText("For example: Black Dell laptop charger");
        card.add(UIStyle.labeled("Item title *", titleField));
        card.add(Box.createVerticalStrut(12));

        // --- category + location ---------------------------------------
        card.add(twoColumns(
                UIStyle.labeled("Category *", categoryBox),
                UIStyle.labeled("Location *", locationBox)));

        // --- date + urgency / handover ---------------------------------
        card.add(twoColumns(
                UIStyle.labeled("Date of incident *", dateSpinner),
                buildExtraFields()));
        card.add(Box.createVerticalStrut(12));

        // --- contact + reward, or storage + notes -----------------------
        extraRow = buildSecondExtraRow();
        card.add(extraRow);
        card.add(Box.createVerticalStrut(12));
        rebuildExtraRow(true);

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

    /** The type-specific half of the form, driven by the CardLayout. */
    private JComponent buildExtraFields() {
        extraPanel.setOpaque(false);

        // LOST card: urgency (contact and reward are in the second row below)
        JPanel lostCard = new JPanel(new BorderLayout(0, 4));
        lostCard.setOpaque(false);
        lostCard.add(UIStyle.fieldLabel("Urgency"), BorderLayout.NORTH);
        urgencyBox.addItem(Urgency.LOW);
        urgencyBox.addItem(Urgency.MEDIUM);
        urgencyBox.addItem(Urgency.HIGH);
        urgencyBox.setSelectedItem(Urgency.MEDIUM);
        lostCard.add(urgencyBox, BorderLayout.CENTER);

        // FOUND card: handover check box
        JPanel foundCard = new JPanel(new BorderLayout(0, 4));
        foundCard.setOpaque(false);
        foundCard.add(UIStyle.fieldLabel("Handover"), BorderLayout.NORTH);
        styleCheckBox(handedCheck);
        foundCard.add(handedCheck, BorderLayout.CENTER);

        extraPanel.add(lostCard, "lost");
        extraPanel.add(foundCard, "found");
        extraCards.show(extraPanel, "lost");
        return extraPanel;
    }

    /** Contact + reward (lost) or storage + notes (found). */
    private JPanel buildSecondExtraRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return row;
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
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd MMM yyyy");
        spinner.setEditor(editor);
        spinner.setFont(UIStyle.FONT);
        spinner.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));
        editor.getTextField().setFont(UIStyle.FONT);
        editor.getTextField().setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
        spinner.setPreferredSize(new Dimension(160, 34));
        return spinner;
    }

    private void styleRadio(JRadioButton radio) {
        radio.setFont(UIStyle.FONT_BOLD);
        radio.setForeground(UIStyle.TEXT);
        radio.setBackground(UIStyle.CARD);
        radio.setFocusPainted(false);
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

    private void wireTypeToggle() {
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                boolean lost = lostRadio.isSelected();
                extraCards.show(extraPanel, lost ? "lost" : "found");
                rebuildExtraRow(lost);
            }
        };
        lostRadio.addActionListener(listener);
        foundRadio.addActionListener(listener);
    }

    /**
     * The second type-specific row is rebuilt when the type changes, because a
     * lost report needs contact + reward while a found report needs storage +
     * notes. Rebuilding keeps the code obvious instead of hiding fields.
     */
    private void rebuildExtraRow(boolean lost) {
        if (extraRow == null) {
            return;
        }
        extraRow.removeAll();
        extraRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        if (lost) {
            contactField.setText(defaultContact());
            extraRow.add(UIStyle.labeled("Contact e-mail / phone *", contactField));
            rewardField.setText("");
            extraRow.add(UIStyle.labeled("Reward offered (Rs, optional)", rewardField));
        } else {
            extraRow.add(UIStyle.labeled("Where is the item kept? *", storageField));
            extraRow.add(UIStyle.labeled("Finder notes (optional)", finderNotesField));
        }
        extraRow.revalidate();
        extraRow.repaint();
    }

    private JPanel extraRow;

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

            JOptionPane.showMessageDialog(this,
                    "Report #" + id + " saved.\n\n"
                    + "Matching lost and found reports are recalculated automatically.",
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

        if (lostRadio.isSelected()) {
            return new LostItem(title, description, category, location, date, reporter,
                    contactField.getText(),
                    parseReward(),
                    (Urgency) urgencyBox.getSelectedItem());
        }
        return new FoundItem(title, description, category, location, date, reporter,
                storageField.getText(),
                handedCheck.isSelected(),
                finderNotesField.getText());
    }

    private BigDecimal parseReward() {
        String text = rewardField.getText();
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("The reward must be a number, for example 100 or 250.50.");
        }
    }

    private void resetForm() {
        titleField.setText("");
        descriptionArea.setText("");
        contactField.setText(defaultContact());
        rewardField.setText("");
        storageField.setText("");
        finderNotesField.setText("");
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

    private String defaultContact() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return "";
        }
        return user.getEmail() + " / " + user.getPhone();
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
