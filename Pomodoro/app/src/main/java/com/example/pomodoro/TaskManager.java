package com.example.pomodoro;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {

    private static final String PREFS_NAME = "tasks_prefs";
    private static final String TASKS_KEY = "tasks_key";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public TaskManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveTasks(List<Task> taskList) {
        String json = gson.toJson(taskList);
        sharedPreferences.edit().putString(TASKS_KEY, json).apply();
    }

    public List<Task> loadTasks() {
        String json = sharedPreferences.getString(TASKS_KEY, null);
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }
}