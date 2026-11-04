package com.campus.web.model;
public enum ClaimStatus { PENDING("Pending"), APPROVED("Approved"), REJECTED("Rejected"), RECOVERED("Recovered"); private final String label; ClaimStatus(String l){label=l;} public String getLabel(){return label;} }
