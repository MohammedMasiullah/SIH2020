package com.example.safetyappv1;

public class Contacts {

    public String name,image,email;

    public Contacts(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Contacts()
    {

    }
    //Todo need to do for image
}
