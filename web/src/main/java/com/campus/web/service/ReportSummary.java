package com.campus.web.service;

import com.campus.web.model.ClaimStatus;
import com.campus.web.model.ItemStatus;
import com.campus.web.model.ItemType;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportSummary {

    private long totalItems;
    private long totalLost;
    private long totalFound;
    private long totalRecovered;
    private long totalClaimed;
    private long totalOpen;
    private long totalClaims;
    private long pendingClaims;
    private double recoveryRate;

    private Map<ItemStatus, Long> byStatus = new LinkedHashMap<>();
    private Map<ClaimStatus, Long> claimsByStatus = new LinkedHashMap<>();
    private Map<String, Long> byCategory = new LinkedHashMap<>();
    private Map<String, Map<String, Long>> monthly = new LinkedHashMap<>();

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getTotalLost() {
        return totalLost;
    }

    public void setTotalLost(long totalLost) {
        this.totalLost = totalLost;
    }

    public long getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(long totalFound) {
        this.totalFound = totalFound;
    }

    public long getTotalRecovered() {
        return totalRecovered;
    }

    public void setTotalRecovered(long totalRecovered) {
        this.totalRecovered = totalRecovered;
    }

    public long getTotalClaimed() {
        return totalClaimed;
    }

    public void setTotalClaimed(long totalClaimed) {
        this.totalClaimed = totalClaimed;
    }

    public long getTotalOpen() {
        return totalOpen;
    }

    public void setTotalOpen(long totalOpen) {
        this.totalOpen = totalOpen;
    }

    public long getTotalClaims() {
        return totalClaims;
    }

    public void setTotalClaims(long totalClaims) {
        this.totalClaims = totalClaims;
    }

    public long getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(long pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public double getRecoveryRate() {
        return recoveryRate;
    }

    public void setRecoveryRate(double recoveryRate) {
        this.recoveryRate = recoveryRate;
    }

    public Map<ItemStatus, Long> getByStatus() {
        return byStatus;
    }

    public void setByStatus(Map<ItemStatus, Long> byStatus) {
        this.byStatus = byStatus;
    }

    public Map<ClaimStatus, Long> getClaimsByStatus() {
        return claimsByStatus;
    }

    public void setClaimsByStatus(Map<ClaimStatus, Long> claimsByStatus) {
        this.claimsByStatus = claimsByStatus;
    }

    public Map<String, Long> getByCategory() {
        return byCategory;
    }

    public void setByCategory(Map<String, Long> byCategory) {
        this.byCategory = byCategory;
    }

    public Map<String, Map<String, Long>> getMonthly() {
        return monthly;
    }

    public void setMonthly(Map<String, Map<String, Long>> monthly) {
        this.monthly = monthly;
    }
}
