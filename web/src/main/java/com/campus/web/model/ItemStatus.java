package com.campus.web.model;
public enum ItemStatus { OPEN("Open"), MATCHED("Matched"), CLAIMED("Claimed"), RECOVERED("Recovered"), CLOSED("Closed"); private final String label; ItemStatus(String l){label=l;} public String getLabel(){return label;} }
