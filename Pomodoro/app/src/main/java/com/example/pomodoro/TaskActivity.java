package com.example.pomodoro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity {

    private Button btnHome, btnTasks, btnPending, btnCompleted, btnAddTask;
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private Handler handler;
    private TaskManager taskManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        taskManager = new TaskManager(this);

        taskList = taskManager.loadTasks();

        // Configurar RecyclerView y Adapter
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, taskList, taskManager);
        recyclerViewTasks.setAdapter(taskAdapter);

        // Inicializar las vistas
        btnHome = findViewById(R.id.btnHome);
        btnTasks = findViewById(R.id.btnTasks);
        btnPending = findViewById(R.id.btnPending);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnAddTask = findViewById(R.id.btnAddTask);

        // Configurar los botones de navegación
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(TaskActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnTasks.setEnabled(false); // Deshabilitar btnTasks ya que ya estamos en TaskActivity

        // Botones de filtro
        btnPending.setOnClickListener(v -> {
            filterTasks("Pendiente");
            getIntent().putExtra("filter", "Pendiente");
        });

        btnCompleted.setOnClickListener(v -> {
            filterTasks("Completada");
            getIntent().putExtra("filter", "Completada");
        });

        // Botón para agregar tareas
        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskActivity.this, AddTaskActivity.class);
            startActivityForResult(intent, 1);
        });

        // Leer el filtro pasado por el intent
        String filter = getIntent().getStringExtra("filter");
        if (filter != null && filter.equals("Completada")) {
            filterTasks("Completada");
        } else {
            // Filtrar tareas pendientes por defecto al abrir la pantalla
            filterTasks("Pendiente");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        taskList = taskManager.loadTasks(); // Recargar la lista de tareas desde SharedPreferences

        // Aplicar el filtro actual - por defecto "Pendiente" si no hay filtro almacenado
        String filter = getIntent().getStringExtra("filter");
        if (filter != null && filter.equals("Completada")) {
            filterTasks("Completada");
        } else {
            filterTasks("Pendiente"); // Filtrar por pendientes por defecto
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String taskName = data.getStringExtra("taskName");
            String taskDescription = data.getStringExtra("taskDescription");
            int taskTime = data.getIntExtra("taskTime", 5);

            // Crear la nueva tarea y agregarla a la lista
            Task newTask = new Task(taskName, taskDescription, "Pendiente", taskTime);
            taskList.add(newTask);

            // Guardar las tareas actualizadas en SharedPreferences
            taskManager.saveTasks(taskList);

            // Actualizar el adaptador y la lista de tareas
            taskAdapter.notifyDataSetChanged();
            filterTasks("Pendiente"); // Mantener el filtro de tareas pendientes por defecto
        }
    }
    private void filterTasks(String status) {
        List<Task> filteredList = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getStatus().equals(status)) {
                filteredList.add(task);
            }
        }
        taskAdapter.updateTasks(filteredList);
    }

    public void removeTask(Task task) {
        taskList.remove(task);
    }



    @Override
    protected void onPause() {
        super.onPause();
        taskManager.saveTasks(taskList);  // Guarda las tareas cuando la actividad se pausa
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        taskManager.saveTasks(taskList);  // Guarda las tareas cuando la actividad se destruye
    }
}