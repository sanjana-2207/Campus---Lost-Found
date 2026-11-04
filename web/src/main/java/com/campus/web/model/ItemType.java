package com.campus.web.model;
public enum ItemType { LOST("Lost"), FOUND("Found"); private final String label; ItemType(String l){label=l;} public String getLabel(){return label;} }
