package com.example.safetyappv1;

public class CreateMessage {

    String date,message,time,name;

    public CreateMessage(String date, String message, String time, String name) {
        this.date = date;
        this.message = message;
        this.time = time;
        this.name = name;
    }

    CreateMessage()
    {

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
