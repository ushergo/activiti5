package com.example.activitifive.model;

import java.io.Serializable;

public class Seller extends ShoppingParticipater implements Serializable {

    private boolean isDelivery;

    public boolean isDelivery() {
        return isDelivery;
    }

    public void setDelivery(boolean delivery) {
        isDelivery = delivery;
    }
}
