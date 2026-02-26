package com.jpmc.midascore.foundation;

public class Incentive {

    private float amount;

    protected Incentive() {
        // for JSON deserialization
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}