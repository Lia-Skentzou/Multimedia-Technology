package com.taskmanager;


public class Category {
    private String name;

    //default constructor
    public Category() {}

    //constructor
    public Category(String name) {
        this.name = name;
    }

    //getters ans setters 
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category other = (Category) obj;
        return name != null ? name.equalsIgnoreCase(other.name) : other.name == null;
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.toLowerCase().hashCode();
    }
}