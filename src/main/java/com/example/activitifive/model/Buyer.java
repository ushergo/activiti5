package com.example.activitifive.model;

import java.io.Serializable;

public class Buyer extends ShoppingParticipater implements Serializable {

    private boolean isPay;

    public boolean isPay() {
        return isPay;
    }

    public void setPay(boolean pay) {
        isPay = pay;
    }
}
