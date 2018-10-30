package com.example.verica.socialnetwork;

public class Messages
{
    public String date,time, message,from, type;

    public Messages(){}

    public Messages(String date, String time, String message, String from, String type) {
        this.date = date;
        this.time = time;
        this.message = message;
        this.from = from;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
