package com.example.weighttracker;

// Holds one weight entry: the database id, the value, and the date
public class Weight {
    private final int id;
    private final double value;
    private final String date;

    public Weight(int id, double value, String date) {
        this.id = id;
        this.value = value;
        this.date = date;
    }

    public int getId() { return id; }
    public double getValue() { return value; }
    public String getDate() { return date; }
}
