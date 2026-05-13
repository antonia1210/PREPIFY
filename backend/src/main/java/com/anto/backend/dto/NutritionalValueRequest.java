package com.anto.backend.dto;

public class NutritionalValueRequest {
    private String name;
    private double amount;
    private String unit;

    public String getName() { return name; }
    public double getAmount() { return amount; }
    public String getUnit() { return unit; }
    public void setName(String name) { this.name = name; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setUnit(String unit) { this.unit = unit; }
}