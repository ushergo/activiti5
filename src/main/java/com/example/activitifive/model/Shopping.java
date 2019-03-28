package com.example.activitifive.model;


import java.io.Serializable;

public class Shopping implements Serializable {

    private static final long serialVersionUID = 6757393795687480331L;

    private String buyerId;
    private String sellerId;
    private boolean isPay;
    private boolean isDeliver;

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public boolean isPay() {
        return isPay;
    }

    public void setPay(boolean pay) {
        isPay = pay;
    }

    public boolean isDeliver() {
        return isDeliver;
    }

    public void setDeliver(boolean deliver) {
        isDeliver = deliver;
    }
}
