package com.example.pomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etTaskDescription;
    private Spinner spinnerTaskTime;
    private Button btnSaveTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Inicializar las vistas
        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        spinnerTaskTime = findViewById(R.id.spinnerTaskTime);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        // Configurar el Spinner
        Integer[] timeOptions = {1, 5, 10, 15, 20, 25};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskTime.setAdapter(adapter);

        // Configurar el botón "Hecho"
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String taskName = etTaskName.getText().toString().trim();
        String taskDescription = etTaskDescription.getText().toString().trim();
        int taskTime = (int) spinnerTaskTime.getSelectedItem();

        if (taskName.isEmpty()) {
            Toast.makeText(this, "El nombre de la tarea es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un intent para devolver los datos a la actividad anterior
        Intent resultIntent = new Intent();
        resultIntent.putExtra("taskName", taskName);
        resultIntent.putExtra("taskDescription", taskDescription);
        resultIntent.putExtra("taskTime", taskTime);
        setResult(RESULT_OK, resultIntent);
        finish(); // Cerrar la actividad y regresar a TaskActivity
    }

}
