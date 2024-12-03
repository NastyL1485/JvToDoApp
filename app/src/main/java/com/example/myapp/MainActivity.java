package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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
        new Thread(new Runnable() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void run() {
                try {
                    List<Task> dataList = database.taskDao().getAllTasks();
                    if (dataList != null && !dataList.isEmpty()) {
                        // Группируем задачи по дате
                        Map<String, List<Task>> tasksByDate = new HashMap<>();
                        for (Task task : dataList) {
                            String date = task.getDate();
                            if (!tasksByDate.containsKey(date)) {
                                tasksByDate.put(date, new ArrayList<>());
                            }
                            tasksByDate.get(date).add(task);
                        }

                        // Обновление UI в главном потоке
                        runOnUiThread(() -> {
                            tasksLayout.removeAllViews();  // Очистить контейнер перед добавлением новых элементов

                            // Для каждой уникальной даты создаем новый LinearLayout
                            for (String date : tasksByDate.keySet()) {
                                LinearLayout dateLayout = new LinearLayout(MainActivity.this);
                                dateLayout.setBackgroundResource(R.drawable.rounded_bg); // Устанавливаем фон для dateLayout
                                dateLayout.setOrientation(LinearLayout.VERTICAL);  // Вертикальная ориентация для задач

                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, // Ширина, можно использовать MATCH_PARENT
                                        LinearLayout.LayoutParams.WRAP_CONTENT // Высота, можно использовать WRAP_CONTENT
                                );

                                // Устанавливаем отступы (10dp) снизу
                                int marginBottom = (int) (10 * getResources().getDisplayMetrics().density); // Переводим dp в пиксели
                                layoutParams.setMargins(0, 0, 0, marginBottom); // Отступы: (левая, верхняя, правая, нижняя)

                                dateLayout.setLayoutParams(layoutParams);

                                // Создаем TextView для отображения даты
                                TextView dateTextView = new TextView(MainActivity.this);
                                dateTextView.setText(date);  // Устанавливаем дату как заголовок
                                dateTextView.setTextSize(18);
                                dateTextView.setTypeface(null, Typeface.BOLD);
                                dateTextView.setPadding(60, 20, 40, 20);
                                int dateTextColor = ContextCompat.getColor(MainActivity.this, R.color.commonText);
                                dateTextView.setTextColor(dateTextColor);
                                dateLayout.addView(dateTextView);

                                // Для каждой задачи на эту дату создаем новый LinearLayout с TextView и ImageView
                                for (Task task : tasksByDate.get(date)) {
                                    // Составляем строку с названием и временем задачи
                                    String taskDetails = task.getTitle() + " - " + task.getTime();

                                    // Создаем новый LinearLayout для отображения задачи
                                    LinearLayout taskLayout = new LinearLayout(MainActivity.this);
                                    taskLayout.setOrientation(LinearLayout.HORIZONTAL);  // Горизонтальная ориентация

                                    // Создаем TextView для названия задачи с фиксированной шириной
                                    TextView taskTextView = new TextView(MainActivity.this);
                                    taskTextView.setText(task.getTitle());  // Устанавливаем название задачи
                                    taskTextView.setTextSize(16);
                                    int taskTextColor = ContextCompat.getColor(MainActivity.this, R.color.commonText);
                                    // Устанавливаем цвет для текста
                                    taskTextView.setTextColor(taskTextColor);

                                    // Устанавливаем фиксированную ширину (190 пикселей) и allow wrapping
                                    LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                                            (int) (190 * getResources().getDisplayMetrics().density),
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    );
                                    taskTextView.setLayoutParams(titleParams);  // Устанавливаем LayoutParams с фиксированной шириной

                                    taskTextView.setSingleLine(false);  // Разрешаем несколько строк
                                    taskTextView.setEllipsize(null);  // Убираем обрезку текста
                                    taskTextView.setPadding(60, 0, 0, 30);  // Добавляем отступ слева
                                    taskLayout.addView(taskTextView);  // Добавляем название задачи в layout

                                    taskTextView.setOnTouchListener(new View.OnTouchListener() {
                                        private static final int LONG_PRESS_THRESHOLD = 1000; // 1 секунда
                                        @SuppressLint("ClickableViewAccessibility")
                                        private Handler handler = new Handler();
                                        private Runnable runnable;

                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            switch (event.getAction()) {
                                                case MotionEvent.ACTION_DOWN:
                                                    // Запускаем отсчет времени
                                                    runnable = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            showDeleteConfirmationDialog(task.getId());  // Показать диалог удаления
                                                        }
                                                    };
                                                    handler.postDelayed(runnable, LONG_PRESS_THRESHOLD); // Запускать через 3 секунды
                                                    return true;

                                                case MotionEvent.ACTION_UP:
                                                case MotionEvent.ACTION_CANCEL:
                                                    // Если пользователь отпустил палец или отменил действие, отменяем обработку
                                                    handler.removeCallbacks(runnable);

                                                    // Важно! Вызов performClick() для правильной обработки кликов (предупреждение)
                                                    v.performClick(); // Это вызовет корректную обработку клика
                                                    return true;
                                            }
                                            return false;
                                        }
                                    });


                                    // Создаем TextView для времени задачи с отступом 20 пикселей
                                    TextView timeTextView = new TextView(MainActivity.this);
                                    timeTextView.setText(task.getTime());  // Устанавливаем время задачи
                                    timeTextView.setTextSize(16);
                                    timeTextView.setPadding(20, 0, 0, 30);  // Отступы: 20 пикселей справа от названия
                                    int timeTextColor = ContextCompat.getColor(MainActivity.this, R.color.commonText);
                                    timeTextView.setTextColor(timeTextColor);
                                    taskLayout.addView(timeTextView);  // Добавляем время в layout

                                    // Создаем ImageView для отображения статуса задачи
                                    ImageView statusImageView = new ImageView(MainActivity.this);
                                    if (task.getStatus()) {
                                        statusImageView.setImageResource(R.drawable.checkbox_checked);  // Закрашенный квадратик
                                    } else {
                                        statusImageView.setImageResource(R.drawable.checkbox_unchecked);  // Незакрашенный квадратик
                                    }


                                    // Устанавливаем параметры для ImageView
                                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    statusImageView.setLayoutParams(imageParams);
                                    imageParams.setMargins(30, 5 ,0, 40);

                                    // Добавляем ImageView в LinearLayout с задачей
                                    taskLayout.addView(statusImageView);

                                    statusImageView.setOnTouchListener(new View.OnTouchListener() {
                                        private static final int LONG_PRESS_THRESHOLD = 1;
                                        @SuppressLint("ClickableViewAccessibility")
                                        private Handler handler = new Handler();
                                        private Runnable runnable;

                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            switch (event.getAction()) {
                                                case MotionEvent.ACTION_DOWN:
                                                    // Запускаем отсчет времени
                                                    runnable = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            changeStatus(task.getId());  // Показать диалог удаления
                                                        }
                                                    };
                                                    handler.postDelayed(runnable, LONG_PRESS_THRESHOLD); // Запускать через 3 секунды
                                                    return true;

                                                case MotionEvent.ACTION_UP:
                                                case MotionEvent.ACTION_CANCEL:
                                                    // Если пользователь отпустил палец или отменил действие, отменяем обработку
                                                    handler.removeCallbacks(runnable);

                                                    // Важно! Вызов performClick() для правильной обработки кликов (предупреждение)
                                                    v.performClick(); // Это вызовет корректную обработку клика
                                                    return true;
                                            }
                                            return false;
                                        }
                                    });

                                    // Добавляем LinearLayout с задачей и статусом в основной layout
                                    dateLayout.addView(taskLayout);
                                }

                                // Добавляем весь LinearLayout для этой даты в основной контейнер
                                tasksLayout.addView(dateLayout);
                            }
                        });
                    } else {
                        // Если нет задач, выводим сообщение
                        runOnUiThread(() -> {
                            TextView emptyTextView = new TextView(MainActivity.this);
                            emptyTextView.setText("Нет задач для отображения");
                            tasksLayout.addView(emptyTextView);  // Добавляем TextView с сообщением
                        });
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Ошибка при загрузке данных", e);
                }
            }
        }).start();
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










//public class MainActivity extends AppCompatActivity {
//
//    private TextView taskTextView;
//    private AppDatabase database;
//    private ActivityResultLauncher<Intent> startForResult;
//    private LinearLayout tasksLayout;  // Родительский контейнер для динамически добавляемых TextView
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
////        taskTextView = findViewById(R.id.noteExample); // Это, скорее всего, один текстовый блок для примера, но не для вывода всех задач
//        tasksLayout = findViewById(R.id.tasksLayout);  // Это контейнер для всех динамически создаваемых TextView
//        database = Room.databaseBuilder(getApplicationContext(),
//                AppDatabase.class, "todo_database").build();
//
//        fetchData(); // Изначальный запрос данных
//
//        // Регистрация ActivityResultLauncher
//        startForResult = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK) {
//                        // После добавления новой заметки обновляем данные
//                        fetchData();  // Перезагружаем данные
//                    }
//                }
//        );
//    }
//
//    // Метод для создания новой заметки
//    public void createNewNote(View view) {
//        Intent intent = new Intent(this, AddNoteActivity.class);
//        startForResult.launch(intent); // Запускаем AddNoteActivity и ждем результат
//    }
//
//    // Метод для получения данных из базы данных
//    public void fetchData() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    List<Task> dataList = database.taskDao().getAllTasks();
//                    if (dataList != null && !dataList.isEmpty()) {
//                        // Группируем задачи по дате
//                        Map<String, List<Task>> tasksByDate = new HashMap<>();
//                        for (Task task : dataList) {
//                            String date = task.getDate();
//                            if (!tasksByDate.containsKey(date)) {
//                                tasksByDate.put(date, new ArrayList<>());
//                            }
//                            tasksByDate.get(date).add(task);
//                        }
//
//                        // Обновление UI в главном потоке
//                        runOnUiThread(() -> {
//                            tasksLayout.removeAllViews();  // Очистить контейнер перед добавлением новых элементов
//
//                            // Для каждой уникальной даты создаем новый TextView
//                            for (String date : tasksByDate.keySet()) {
//                                // Создаем TextView для отображения даты
//                                TextView dateTextView = new TextView(MainActivity.this);
//                                dateTextView.setText(date);  // Устанавливаем дату как заголовок
//                                dateTextView.setTextSize(18);
//                                dateTextView.setTypeface(null, Typeface.BOLD);
//                                tasksLayout.addView(dateTextView);  // Добавляем TextView с датой
//
//                                // Для каждой задачи на эту дату создаем новый TextView и добавляем
//                                for (Task task : tasksByDate.get(date)) {
//                                    TextView taskTextView = new TextView(MainActivity.this);
//                                    taskTextView.setText(task.getTitle());  // Устанавливаем название задачи
//                                    taskTextView.setTextSize(16);
//                                    tasksLayout.addView(taskTextView);  // Добавляем TextView с задачей
//                                }
//                            }
//                        });
//                    } else {
//                        // Если нет задач, выводим сообщение
//                        runOnUiThread(() -> taskTextView.setText("Нет задач для отображения"));
//                    }
//                } catch (Exception e) {
//                    Log.e("MainActivity", "Ошибка при загрузке данных", e);
//                }
//            }
//        }).start();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        fetchData();  // Загружаем данные при возвращении в MainActivity
//    }
//}








//public class MainActivity extends AppCompatActivity {
//
//    private TextView taskTextView;
//    private AppDatabase database;
//    private ActivityResultLauncher<Intent> startForResult;
//    private LinearLayout tasksLayout;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        taskTextView = findViewById(R.id.noteExample);
//        database = Room.databaseBuilder(getApplicationContext(),
//                AppDatabase.class, "todo_database").build();
//
//
//        fetchData(); // Изначальный запрос данных
//
//        // Регистрация ActivityResultLauncher
//        startForResult = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK) {
//                        // После добавления новой заметки обновляем данные
//                        fetchData();  // Перезагружаем данные
//                    }
//                }
//        );
//    }
//
//    // Метод для создания новой заметки
//    public void createNewNote(View view) {
//        Intent intent = new Intent(this, AddNoteActivity.class);
//        startForResult.launch(intent); // Запускаем AddNoteActivity и ждем результат
//    }
//
//    // Метод для получения данных из базы данных
//    public void fetchData() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    List<Task> dataList = database.taskDao().getAllTasks();
//                    if (dataList != null && !dataList.isEmpty()) {
//                        StringBuilder stringBuilder = new StringBuilder();
//                        for (Task task : dataList) {
//                            stringBuilder.append(task.getTitle()).append("\n");
//                        }
//                        String finalString = stringBuilder.toString();
//                        runOnUiThread(() -> taskTextView.setText(finalString)); // Обновляем UI
//                    } else {
//                        // Если данных нет, вывести сообщение
//                        runOnUiThread(() -> taskTextView.setText("Нет задач для отображения"));
//                    }
//                } catch (Exception e) {
//                    Log.e("MainActivity", "Ошибка при загрузке данных", e);
//                }
//            }
//        }).start();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        fetchData();  // Загружаем данные при возвращении в MainActivity
//    }
//}



















//import android.os.Bundle;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//import android.os.Bundle;
//import android.widget.TextView;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.room.Room;
//
//import java.util.List;
//
//
////import missing.namespace.R;
//
//public class MainActivity extends AppCompatActivity {
//
////    private TextView textViewOutput;
//    private TextView taskTextView;
//    private AppDatabase database;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
////        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
////            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
////            return insets;
////        });
////        textViewOutput = findViewById(R.id.noteExample);
//        taskTextView = findViewById(R.id.noteExample);
//
//        database = Room.databaseBuilder(getApplicationContext(),
//                AppDatabase.class, "tasks").build();
//
//        // Запрос данных из базы
//        fetchData();
//
//        // Получаем Intent и извлекаем данные
////        Intent intent = getIntent();
////        String name = intent.getStringExtra("EXTRA_NAME");
////        String time = intent.getStringExtra("EXTRA_TIME");
////        String date = intent.getStringExtra("EXTRA_DATE");
////
////        // Отображаем данные
////        textViewOutput.setText("Имя: " + name + "\nВремя: " + time + "\nДата: " + date);
//    }
//
//    public void createNewNote(View view){
//        Intent intent = new Intent(this, AddNoteActivity.class);
//        startActivity(intent);
//    }
//
//    public void fetchData() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("MainActivity", "Запрос данных из базы...");
//                List<Task> dataList = database.taskDao().getAllTasks();
//                Log.d("MainActivity", "Количество записей в базе: " + dataList.size());
//
//                // Преобразование данных в строку
//                StringBuilder stringBuilder = new StringBuilder();
//                for (Task data : dataList) {
//                    stringBuilder.append(data.getTitle()).append("\n");
//                }
//
//                // Обновление UI на главном потоке
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d("MainActivity", "Обновление UI с текстом: " + stringBuilder.toString());
//                        taskTextView.setText(stringBuilder.toString());
//                    }
//                });
//            }
//        }).start();
//    }
//}