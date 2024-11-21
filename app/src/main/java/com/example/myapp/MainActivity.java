package com.example.myapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
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
                                dateTextView.setPadding(60, 0, 40, 20);
                                dateLayout.addView(dateTextView);
                                  // Добавляем TextView с датой

                                // Для каждой задачи на эту дату создаем новый TextView и добавляем
                                for (Task task : tasksByDate.get(date)) {

                                    TextView taskTextView = new TextView(MainActivity.this);
                                    taskTextView.setText(task.getTitle());  // Устанавливаем название задачи
                                    taskTextView.setTextSize(16);
                                    taskTextView.setPadding(60, 0, 40, 30);
                                    dateLayout.addView(taskTextView);  // Добавляем TextView с задачей
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