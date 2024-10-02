package com.example.pomodoro;

public class Task {
    private String name;
    private String description;
    private String status;
    private int time; // Tiempo en minutos
    private long remainingTime; // Tiempo restante

    public Task(String name, String description, String status, int time) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.time = time;
        this.remainingTime = time * 60000L; // conversion minutos a milisegundos

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public int getTime() {
        return time;
    }
    public long getRemainingTime() { return remainingTime; }

    public void setStatus(String status) { this.status = status; }
    public void setRemainingTime(long remainingTime) { this.remainingTime = remainingTime; }
}