package com.mymq.commons.global;

public enum ClientType {

    PRODUCE(0,"PRODUCE"),
    CONSUMER(1, "CONSUMER");


    private int key;
    private String type;
    ClientType(int key, String type) {
        this.key = key;
        this.type = type;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
