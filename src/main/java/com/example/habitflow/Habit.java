// Habit.java (Model class)
package com.example.habitflow;

public class Habit {
    private int id;
    private String name;

    public Habit(String name) {
        this.name = name;
    }

    public Habit(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}