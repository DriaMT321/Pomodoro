package com.example.pomodoro;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button btnPomodoro, btnShortBreak, btnLongBreak, btnStartStop;
    private TextView tvMinutes, tvSeconds, tvTaskName;
    private ProgressBar progressBar;
    private Button btnHome, btnTasks;

    private boolean isTimerRunning = false;
    private long timeLeftInMillis;
    private long initialTimeInMillis;

    private CountDownTimer countDownTimer;

    // Variables para manejar tareas
    private String taskName;
    private int taskTime;

    // Variables para el ciclo de Pomodoro
    private int cycleCount = 0;
    private final int TOTAL_CYCLES = 4;
    private boolean isBreakTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Crear el canal de notificación si es necesario
        createNotificationChannel();

        // Verificar y solicitar permiso de notificación (solo para Android 13 o superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Inicializar vistas
        btnPomodoro = findViewById(R.id.btnPomodoro);
        btnShortBreak = findViewById(R.id.btnShortBreak);
        btnLongBreak = findViewById(R.id.btnLongBreak);
        btnStartStop = findViewById(R.id.btnStartStop);
        tvMinutes = findViewById(R.id.tvMinutes);
        tvSeconds = findViewById(R.id.tvSeconds);
        progressBar = findViewById(R.id.progressBar);
        btnHome = findViewById(R.id.btnHome);
        btnTasks = findViewById(R.id.btnTasks);
        tvTaskName = findViewById(R.id.tvTaskName);

        tvMinutes.setVisibility(View.VISIBLE);
        tvSeconds.setVisibility(View.VISIBLE);
        tvTaskName.setVisibility(View.INVISIBLE); // Iniciar con el TextView invisible

        // Ocultar la ProgressBar al iniciar la aplicación
        progressBar.setVisibility(View.INVISIBLE);

        // Deshabilitar el botón Home en la pantalla principal
        btnHome.setEnabled(false);
        btnTasks.setEnabled(true);

        // Configurar los listeners de los botones
        btnPomodoro.setOnClickListener(v -> {
            if (tvTaskName.getVisibility() == View.VISIBLE) {
                tvTaskName.setText("");
                tvTaskName.setVisibility(View.INVISIBLE);
                enableModeButtons();
            }
            taskTime = 25; // Establecer tiempo de Pomodoro
            taskName = "Pomodoro";
            cycleCount = 0; // Reiniciar contador de ciclos
            isBreakTime = false;
            btnStartStop.setEnabled(true);
            startCycleTimer(); // Iniciar el ciclo de Pomodoro
        });

        btnShortBreak.setOnClickListener(v -> handleButtonClick(5));
        btnLongBreak.setOnClickListener(v -> handleButtonClick(10));

        btnStartStop.setOnClickListener(v -> {
            if (isTimerRunning) {
                stopTimer();
            } else {
                if (taskName == null || taskName.isEmpty()) {
                    // Si no se ha seleccionado una tarea o modo, establecer valores predeterminados
                    taskTime = 25; // Tiempo predeterminado
                    taskName = "Pomodoro";
                }
                cycleCount = 0; // Reiniciar contador de ciclos
                isBreakTime = false;
                startCycleTimer();
            }
        });

        // Configurar el modo Pomodoro por defecto
        setTimer(25);

        // Recibir la tarea seleccionada desde TaskActivity
        if (getIntent() != null) {
            Intent intent = getIntent();
            taskName = intent.getStringExtra("taskName");
            taskTime = intent.getIntExtra("taskTime", 25); // Si no se recibe nada, por defecto es 25 minutos
            boolean startPomodoro = intent.getBooleanExtra("startPomodoro", false);

            if (taskName != null) {
                tvTaskName.setText(taskName);
                tvTaskName.setVisibility(View.VISIBLE); // Mostrar el TextView cuando hay una tarea seleccionada
                disableModeButtons(); // Deshabilitar los botones de arriba

                cycleCount = 0; // Reiniciar contador de ciclos
                isBreakTime = false;

                setTimer(taskTime);
                if (startPomodoro) {
                    hideButtonsOnTimerStart();  // Ocultar los botones
                    progressBar.setVisibility(View.VISIBLE);  // Mostrar la ProgressBar
                    btnStartStop.setEnabled(true);
                    startCycleTimer();
                }
            }
        }

        // Configurar los botones de navegación
        btnHome.setOnClickListener(v -> {
            Intent newIntent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(newIntent);
        });

        btnTasks.setOnClickListener(v -> {
            Intent newIntent = new Intent(MainActivity.this, TaskActivity.class);
            startActivity(newIntent);
        });
    }

    private void setTimer(int minutes) {
        timeLeftInMillis = minutes * 60000;
        initialTimeInMillis = timeLeftInMillis;
        updateCountDownText();
        progressBar.setProgress(0);
        // No cambiar la visibilidad de la ProgressBar aquí
    }

    private void startCycleTimer() {
        // Mostrar la ProgressBar al iniciar el temporizador
        progressBar.setVisibility(View.VISIBLE);

        // Ocultar botones excepto btnStartStop
        hideButtonsOnTimerStart();

        updateCountDownText();
        updateProgressBar();
        isTimerRunning = true;
        btnStartStop.setText("Detener");
        btnHome.setEnabled(false);
        btnTasks.setEnabled(false);

        if (!isBreakTime) {
            // Tiempo de trabajo
            if (taskName != null && !taskName.isEmpty()) {
                tvTaskName.setText(taskName);
            }
            setTimer(taskTime);
        } else {
            // Tiempo de descanso
            tvTaskName.setText("Descanso");
            setTimer(5); // Descanso de 5 minutos
        }

        // Iniciar el temporizador
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                updateProgressBar();

                // Enviar notificación si quedan 30 segundos
                if (timeLeftInMillis <= 30000 && timeLeftInMillis > 29000) {
                    sendTimeWarningNotification();
                }
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;

                if (!isBreakTime) {
                    // Termina el tiempo de trabajo, iniciar descanso
                    isBreakTime = true;
                    startCycleTimer();
                } else {
                    // Termina el descanso
                    cycleCount++;
                    if (cycleCount < TOTAL_CYCLES) {
                        isBreakTime = false;
                        startCycleTimer();
                    } else {
                        // Ciclos completados, finalizar tarea
                        onTimerFinish();
                    }
                }
            }
        }.start();
    }

    private void stopTimer() {
        isTimerRunning = false;
        btnStartStop.setText("Iniciar");
        progressBar.setProgress(0);
        progressBar.setVisibility(View.INVISIBLE);
        enableModeButtons();
        setButtonsEnabled(true);

        // Hacer visibles los botones cuando se detiene el temporizador
        showButtonsOnTimerStop();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void onTimerFinish() {
        btnStartStop.setText("Iniciar");
        progressBar.setProgress(100);
        progressBar.setVisibility(View.INVISIBLE);
        enableModeButtons();
        setButtonsEnabled(true);  // Habilitar todos los botones cuando el temporizador finaliza

        // Hacer visibles los botones al finalizar el temporizador
        showButtonsOnTimerStop();

        if (taskName != null && !taskName.isEmpty()) {
            markTaskAsCompleted(taskName);
        }

        isBreakTime = false;
        cycleCount = 0;
        taskName = null;
        taskTime = 25;
        tvTaskName.setText("");
        tvTaskName.setVisibility(View.INVISIBLE);

        btnStartStop.setEnabled(false); // Deshabilitar el botón de inicio si el tiempo es 0
        showCompletionMessage();

        // Llevar al usuario a TaskActivity y seleccionar la sección "Completada"
        Intent intent = new Intent(MainActivity.this, TaskActivity.class);
        intent.putExtra("filter", "Completada");
        startActivity(intent);
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        tvMinutes.setText(String.format(Locale.getDefault(), "%02d", minutes));
        tvSeconds.setText(String.format(Locale.getDefault(), "%02d", seconds));
    }

    private void updateProgressBar() {
        int progress = (int) ((initialTimeInMillis - timeLeftInMillis) * 100 / initialTimeInMillis);
        progressBar.setProgress(progress);
    }

    private void enableModeButtons() {
        btnPomodoro.setEnabled(true);
        btnShortBreak.setEnabled(true);
        btnLongBreak.setEnabled(true);
    }

    private void disableModeButtons() {
        btnPomodoro.setEnabled(false);
        btnShortBreak.setEnabled(false);
        btnLongBreak.setEnabled(false);
    }

    private void handleButtonClick(int minutes) {
        if (tvTaskName.getVisibility() == View.VISIBLE) {
            tvTaskName.setText(""); // Limpiar el texto
            tvTaskName.setVisibility(View.INVISIBLE); // Hacer invisible el TextView
            enableModeButtons(); // Habilitar los botones de modo nuevamente
        }
        setTimer(minutes); // Establecer el temporizador con el tiempo del botón seleccionado
        btnStartStop.setEnabled(true); // Habilitar el botón de iniciar
    }

    private void setButtonsEnabled(boolean enabled) {
        btnPomodoro.setEnabled(enabled);
        btnShortBreak.setEnabled(enabled);
        btnLongBreak.setEnabled(enabled);
        btnHome.setEnabled(enabled);
        btnTasks.setEnabled(enabled);
    }

    private void hideButtonsOnTimerStart() {
        btnPomodoro.setVisibility(View.INVISIBLE);
        btnShortBreak.setVisibility(View.INVISIBLE);
        btnLongBreak.setVisibility(View.INVISIBLE);
        btnHome.setVisibility(View.INVISIBLE);
        btnTasks.setVisibility(View.INVISIBLE);
        // btnStartStop debe permanecer visible
    }

    private void showButtonsOnTimerStop() {
        btnPomodoro.setVisibility(View.VISIBLE);
        btnShortBreak.setVisibility(View.VISIBLE);
        btnLongBreak.setVisibility(View.VISIBLE);
        btnHome.setVisibility(View.VISIBLE);
        btnTasks.setVisibility(View.VISIBLE);
        // btnStartStop permanece visible
    }

    private void markTaskAsCompleted(String taskName) {
        TaskManager taskManager = new TaskManager(this);
        List<Task> tasks = taskManager.loadTasks();
        for (Task task : tasks) {
            if (task.getName().equals(taskName)) {
                task.setStatus("Completada");
                task.setRemainingTime(0);
            }
        }
        taskManager.saveTasks(tasks);
    }

    private void showCompletionMessage() {
        Toast.makeText(this, "¡Tarea completada!", Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        // Crear el canal de notificación para API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PomodoroChannel";
            String description = "Canal de notificaciones para Pomodoro";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("POMODORO_CHANNEL", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendTimeWarningNotification() {
        // Notificación de advertencia cuando quedan 30 segundos
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "POMODORO_CHANNEL")
                .setContentTitle("Advertencia de Pomodoro")
                .setContentText("El tiempo está a punto de acabar")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(2, notificationBuilder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes continuar mostrando notificaciones
                Log.d("MainActivity", "Permiso de notificaciones concedido.");
            } else {
                // Permiso denegado, notificaciones no funcionarán
                Log.d("MainActivity", "Permiso de notificaciones denegado.");
            }
        }
    }
}
