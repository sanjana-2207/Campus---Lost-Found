package com.campus.lostfound.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Concrete item reported by the owner who LOST it.
 *
 * <p>Adds three fields that only make sense for a lost report: contact details,
 * an optional reward and an urgency level.</p>
 */
public class LostItem extends Item {

    private String contact;
    private BigDecimal rewardOffered = BigDecimal.ZERO;
    private Urgency urgency = Urgency.MEDIUM;

    public LostItem() {
        super();
    }

    public LostItem(String title, String description, Category category,
                    Location location, LocalDate eventDate, User reportedBy,
                    String contact, BigDecimal rewardOffered, Urgency urgency) {
        super(title, description, category, location, eventDate, reportedBy);
        this.contact = contact;
        this.rewardOffered = rewardOffered;
        this.urgency = urgency;
    }

    /** Overrides the abstract method: a lost report is always LOST. */
    @Override
    public ItemType getItemType() {
        return ItemType.LOST;
    }

    @Override
    public String getSpecificDetails() {
        return "Contact      : " + (contact == null ? "-" : contact)
                + "\nReward       : Rs " + (rewardOffered == null
                        ? "0.00" : rewardOffered.toPlainString())
                + "\nUrgency      : " + urgency.getLabel();
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public BigDecimal getRewardOffered() {
        return rewardOffered;
    }

    public void setRewardOffered(BigDecimal rewardOffered) {
        this.rewardOffered = rewardOffered == null ? BigDecimal.ZERO : rewardOffered;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency == null ? Urgency.MEDIUM : urgency;
    }
}
