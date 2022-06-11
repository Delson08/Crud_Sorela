package com.example.crud_sorela;

public class Student {
    private String id, name, course, year,image;
    public Student(){

    }
    public Student(String id, String name, String course, String year) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.year = year;


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

}
