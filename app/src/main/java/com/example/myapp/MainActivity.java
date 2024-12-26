package com.example.myapp;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private LinearLayout tasksLayout;  // Родительский контейнер для всех динамически добавляемых LinearLayout
    private AppDatabase database;
    private ActivityResultLauncher<Intent> startForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasksLayout = findViewById(R.id.tasksLayout);  // Получаем контейнер для задач
        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "todo_database").build();

        fetchData(); // Изначальный запрос данных

        // Регистрация ActivityResultLauncher
        startForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // После добавления новой заметки обновляем данные
                        fetchData();  // Перезагружаем данные
                    }
                }
        );
    }

    // Метод для создания новой заметки
    public void createNewNote(View view) {
        Intent intent = new Intent(this, AddNoteActivity.class);
        startForResult.launch(intent); // Запускаем AddNoteActivity и ждем результат
    }

    // Метод для получения данных из базы данных
    public void fetchData() {
        new Thread(() -> {
            try {
                List<Task> dataList = database.taskDao().getAllTasks();
                if (dataList != null && !dataList.isEmpty()) {
                    Map<String, List<Task>> tasksByDate = groupTasksByDate(dataList);
                    runOnUiThread(() -> updateUIWithTasks(tasksByDate));
                } else {
                    runOnUiThread(this::showNoTasksMessage);
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Ошибка при загрузке данных", e);
            }
        }).start();
    }

    private Map<String, List<Task>> groupTasksByDate(List<Task> tasks) {
        Map<String, List<Task>> tasksByDate = new HashMap<>();
        for (Task task : tasks) {
            String date = task.getDate();
            tasksByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
        }
        return tasksByDate;
    }

    private void updateUIWithTasks(Map<String, List<Task>> tasksByDate) {
        tasksLayout.removeAllViews();

        for (String date : tasksByDate.keySet()) {
            LinearLayout dateLayout = createDateLayout(date);
            for (Task task : tasksByDate.get(date)) {
                LinearLayout taskLayout = createTaskLayout(task);
                dateLayout.addView(taskLayout);
            }
            tasksLayout.addView(dateLayout);
        }
    }

    private LinearLayout createDateLayout(String date) {
        LinearLayout dateLayout = new LinearLayout(this);
        dateLayout.setBackgroundResource(R.drawable.rounded_bg);
        dateLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int marginBottom = (int) (10 * getResources().getDisplayMetrics().density);
        layoutParams.setMargins(0, 0, 0, marginBottom);
        dateLayout.setLayoutParams(layoutParams);

        TextView dateTextView = new TextView(this);
        dateTextView.setText(date);
        dateTextView.setTextSize(18);
        dateTextView.setTypeface(null, Typeface.BOLD);
        dateTextView.setPadding(60, 20, 40, 20);
        dateTextView.setTextColor(ContextCompat.getColor(this, R.color.commonText));
        dateLayout.addView(dateTextView);

        return dateLayout;
    }

    private LinearLayout createTaskLayout(Task task) {
        LinearLayout taskLayout = new LinearLayout(this);
        taskLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView taskTextView = createTaskTextView(task);
        taskLayout.addView(taskTextView);

        TextView timeTextView = createTimeTextView(task);
        taskLayout.addView(timeTextView);

        ImageView statusImageView = createStatusImageView(task);
        taskLayout.addView(statusImageView);

        return taskLayout;
    }

    private TextView createTaskTextView(Task task) {
        TextView taskTextView = new TextView(this);
        taskTextView.setText(task.getTitle());
        taskTextView.setTextSize(16);
        taskTextView.setTextColor(ContextCompat.getColor(this, R.color.commonText));
        taskTextView.setSingleLine(false);
        taskTextView.setEllipsize(null);
        taskTextView.setPadding(60, 0, 0, 30);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                (int) (190 * getResources().getDisplayMetrics().density),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        taskTextView.setLayoutParams(titleParams);

        taskTextView.setOnTouchListener(createLongPressListener(task.getId(), id -> showDeleteConfirmationDialog(id)));

        return taskTextView;
    }

    private TextView createTimeTextView(Task task) {
        TextView timeTextView = new TextView(this);
        timeTextView.setText(task.getTime());
        timeTextView.setTextSize(16);
        timeTextView.setPadding(20, 0, 0, 30);
        timeTextView.setTextColor(ContextCompat.getColor(this, R.color.commonText));
        return timeTextView;
    }

    private ImageView createStatusImageView(Task task) {
        ImageView statusImageView = new ImageView(this);
        statusImageView.setImageResource(task.getStatus() ? R.drawable.checkbox_checked : R.drawable.checkbox_unchecked);

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        imageParams.setMargins(30, 5, 0, 40);
        statusImageView.setLayoutParams(imageParams);

        // Добавляем обработчик клика для изменения статуса задачи
        statusImageView.setOnClickListener(v -> {
            // Меняем статус задачи
            boolean newStatus = !task.getStatus();
            task.setStatus(newStatus);

            // Обновляем изображение для отображения нового статуса
            statusImageView.setImageResource(newStatus ? R.drawable.checkbox_checked : R.drawable.checkbox_unchecked);

            // Сохраняем изменения в базе данных (если необходимо)
            new Thread(() -> {
                try {
                    database.taskDao().updateTaskStatus(task.getId(), newStatus);
                } catch (Exception e) {
                    Log.e("MainActivity", "Ошибка при обновлении статуса задачи", e);
                }
            }).start();
        });

        return statusImageView;
    }

    private View.OnTouchListener createLongPressListener(int taskId, Consumer<Integer> action) {
        return new View.OnTouchListener() {
            private static final int LONG_PRESS_THRESHOLD = 1000;
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        runnable = () -> action.accept(taskId);
                        handler.postDelayed(runnable, LONG_PRESS_THRESHOLD);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(runnable);
                        v.performClick();
                        return true;
                }
                return false;
            }
        };
    }

    private void updateTaskStatusImageView(ImageView statusImageView, Task task) {
        task.setStatus(!task.getStatus());
        statusImageView.setImageResource(task.getStatus() ? R.drawable.checkbox_checked : R.drawable.checkbox_unchecked);
    }

    private void showNoTasksMessage() {
        TextView emptyTextView = new TextView(this);
        emptyTextView.setText("Нет задач для отображения");
        tasksLayout.addView(emptyTextView);
    }

    private void showDeleteConfirmationDialog(int taskId) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Подтверждение")
                .setMessage("Вы уверены, что хотите удалить задачу?")
                .setPositiveButton("Да", (dialog, which) -> {
                    // Если да, то удаляем задачу по id и обновляем данные
                    deleteTaskById(taskId);  // Удаление задачи по id
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    // Метод для удаления задачи по ее id
    private void deleteTaskById(int taskId) {
        new Thread(() -> {
            try {
                // Получаем задачу по id и удаляем
                Task taskToDelete = database.taskDao().getTaskById(taskId);
                if (taskToDelete != null) {
                    database.taskDao().deleteTask(taskToDelete);  // Удаляем задачу
                    fetchData();  // Обновляем отображение задач
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Ошибка при удалении задачи", e);
            }
        }).start();
    }

    private void changeStatus(int taskId) {
        new Thread(() -> {
            try {
                boolean currentStatus = database.taskDao().getStatus(taskId);

                if (currentStatus) {
                    database.taskDao().setUncompletedStatus(taskId);  // Меняем на невыполненный
                } else {
                    database.taskDao().setCompletedStatus(taskId);    // Меняем на выполненный
                }

                runOnUiThread(() -> fetchData());  // Обновляем данные на UI

            } catch (Exception e) {
                Log.e("MainActivity", "Ошибка при изменении статуса задачи", e);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchData();  // Загружаем данные при возвращении в MainActivity
    }
}
