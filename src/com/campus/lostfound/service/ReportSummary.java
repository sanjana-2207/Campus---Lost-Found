package com.campus.lostfound.service;

import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.model.ItemStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable-by-convention snapshot of every number shown on the Dashboard and
 * Reports screens. One object per refresh keeps the UI consistent: all counters
 * come from the same point in time.
 */
public class ReportSummary {

    private int totalItems;
    private int totalLost;
    private int totalFound;
    private int totalRecovered;
    private int totalClaimed;
    private int totalOpen;
    private int totalClaims;
    private int pendingClaims;
    private double recoveryRate;

    private Map<ItemStatus, Integer> byStatus = new LinkedHashMap<ItemStatus, Integer>();
    private Map<ClaimStatus, Integer> claimsByStatus = new LinkedHashMap<ClaimStatus, Integer>();
    private Map<String, Integer> byCategory = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> byLocation = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> monthlyLost = new LinkedHashMap<String, Integer>();
    private Map<String, Integer> monthlyFound = new LinkedHashMap<String, Integer>();

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalLost() {
        return totalLost;
    }

    public void setTotalLost(int totalLost) {
        this.totalLost = totalLost;
    }

    public int getTotalFound() {
        return totalFound;
    }

    public void setTotalFound(int totalFound) {
        this.totalFound = totalFound;
    }

    public int getTotalRecovered() {
        return totalRecovered;
    }

    public void setTotalRecovered(int totalRecovered) {
        this.totalRecovered = totalRecovered;
    }

    public int getTotalClaimed() {
        return totalClaimed;
    }

    public void setTotalClaimed(int totalClaimed) {
        this.totalClaimed = totalClaimed;
    }

    public int getTotalOpen() {
        return totalOpen;
    }

    public void setTotalOpen(int totalOpen) {
        this.totalOpen = totalOpen;
    }

    public int getTotalClaims() {
        return totalClaims;
    }

    public void setTotalClaims(int totalClaims) {
        this.totalClaims = totalClaims;
    }

    public int getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(int pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public double getRecoveryRate() {
        return recoveryRate;
    }

    public void setRecoveryRate(double recoveryRate) {
        this.recoveryRate = recoveryRate;
    }

    public Map<ItemStatus, Integer> getByStatus() {
        return byStatus;
    }

    public void setByStatus(Map<ItemStatus, Integer> byStatus) {
        this.byStatus = byStatus;
    }

    public Map<ClaimStatus, Integer> getClaimsByStatus() {
        return claimsByStatus;
    }

    public void setClaimsByStatus(Map<ClaimStatus, Integer> claimsByStatus) {
        this.claimsByStatus = claimsByStatus;
    }

    public Map<String, Integer> getByCategory() {
        return byCategory;
    }

    public void setByCategory(Map<String, Integer> byCategory) {
        this.byCategory = byCategory;
    }

    public Map<String, Integer> getByLocation() {
        return byLocation;
    }

    public void setByLocation(Map<String, Integer> byLocation) {
        this.byLocation = byLocation;
    }

    public Map<String, Integer> getMonthlyLost() {
        return monthlyLost;
    }

    public void setMonthlyLost(Map<String, Integer> monthlyLost) {
        this.monthlyLost = monthlyLost;
    }

    public Map<String, Integer> getMonthlyFound() {
        return monthlyFound;
    }

    public void setMonthlyFound(Map<String, Integer> monthlyFound) {
        this.monthlyFound = monthlyFound;
    }
}
