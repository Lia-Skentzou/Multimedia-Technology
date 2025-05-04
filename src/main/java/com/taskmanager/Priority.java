package com.taskmanager;


public class Priority {
    private String name;

    // default constructor 
    public Priority() {}

    // constructor 
    public Priority(String name) {
        this.name = name;
    }

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}