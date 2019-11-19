package com.atdev.airlinetickets;

public class Helper {


    public String formatString(String s) {
        return "₹" + String.format("%.0f", s);
    }
}
