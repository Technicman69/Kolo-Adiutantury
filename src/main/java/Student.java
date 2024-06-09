package main.java;

import java.awt.*;

public class Student {
    String fullName;
    double weight;
    Color color;

    public Student(String fullName, double weight, Color color) {
        this.fullName = fullName;
        this.weight = weight;
        this.color = color;
    }

    public double angleInRadians(double totalWeight) {
        return 2 * Math.PI * weight / totalWeight;
    }
}
