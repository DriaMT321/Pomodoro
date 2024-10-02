package com.example.pomodoro;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskManager taskManager;
    private Context context;

    public TaskAdapter(Context context, List<Task> taskList, TaskManager taskManager) {
        this.context = context;
        this.taskList = taskList;
        this.taskManager = taskManager;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTaskName.setText(task.getName());

        // Mostrar la descripción si está disponible, de lo contrario, ocultar el TextView
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.tvTaskStatus.setText(task.getDescription());
            holder.tvTaskStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvTaskStatus.setVisibility(View.GONE);
        }

        // Ocultar el botón de seleccionar tarea si la tarea está completada
        if (task.getStatus().equals("Completada")) {
            holder.btnSelectTask.setVisibility(View.GONE);
        } else {
            holder.btnSelectTask.setVisibility(View.VISIBLE);
        }

        holder.btnSelectTask.setOnClickListener(v -> {
            // Lógica para seleccionar la tarea
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("taskName", task.getName());
            intent.putExtra("taskTime", task.getTime());
            intent.putExtra("startPomodoro", true);
            context.startActivity(intent);
        });

        holder.btnDeleteTask.setOnClickListener(v -> {
            // Lógica para eliminar la tarea de manera permanente
            removeTask(task, position);
        });
    }
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Método para actualizar la lista de tareas
    public void updateTasks(List<Task> newTasks) {
        taskList.clear(); // Limpiar la lista actual
        taskList.addAll(newTasks); // Agregar las nuevas tareas filtradas
        notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvTaskStatus;
        Button btnSelectTask, btnDeleteTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.taskName);
            tvTaskStatus = itemView.findViewById(R.id.taskStatus);
            btnSelectTask = itemView.findViewById(R.id.btnSelectTask);
            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask);
        }
    }
    private void removeTask(Task task, int position) {
        // Eliminar la tarea de la lista filtrada en el adaptador
        taskList.remove(task);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, taskList.size());

        // Eliminar la tarea de la lista principal en la actividad
        if (context instanceof TaskActivity) {
            ((TaskActivity) context).removeTask(task);
        }

        // Guardar la lista actualizada en SharedPreferences
        taskManager.saveTasks(taskList);
    }

}