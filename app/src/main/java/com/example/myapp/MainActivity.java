package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView taskTextView;
    private AppDatabase database;
    private ActivityResultLauncher<Intent> startForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskTextView = findViewById(R.id.noteExample);
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
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Task task : dataList) {
                            stringBuilder.append(task.getTitle()).append("\n");
                        }
                        String finalString = stringBuilder.toString();
                        runOnUiThread(() -> taskTextView.setText(finalString)); // Обновляем UI
                    } else {
                        // Если данных нет, вывести сообщение
                        runOnUiThread(() -> taskTextView.setText("Нет задач для отображения"));
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