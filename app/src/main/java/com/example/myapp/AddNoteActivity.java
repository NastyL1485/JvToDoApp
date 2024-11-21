package com.example.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.DatePickerDialog;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddNoteActivity extends AppCompatActivity {

    private TextView selectedDateTextView;
    private EditText noteName;
    private EditText noteTime;
    private TextView noteDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Button chooseDateButton = findViewById(R.id.choose_date_button);
        selectedDateTextView = findViewById(R.id.selected_date);

        noteName = findViewById(R.id.noteName);
        noteDate = findViewById(R.id.selected_date);
        noteTime = findViewById(R.id.noteTime);
        Button createNote = findViewById(R.id.createNote);
        createNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask();
            }
        });

        chooseDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    public void backToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDatePickerDialog() {
        // Получаем текущую дату
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Создаем диалог выбора даты
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Форматируем выбранную дату в формат yyyy-MM-dd
                    String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);

                    // Обновляем текст на TextView с выбранной датой в нужном формате
                    selectedDateTextView.setText("Выбранная дата: " + formattedDate);

                    // Сохраняем эту дату в поле, которое будет использоваться при сохранении задачи
                    noteDate.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show(); // Показываем диалог выбора даты
    }

    private void saveTask() {
        // Получаем данные из полей ввода
        String title = noteName.getText().toString();
        String date = noteDate.getText().toString();  // Дата уже отформатирована в yyyy-MM-dd
        String time = noteTime.getText().toString();

        // Логирование полученных данных
        Log.d("AddNoteActivity", "Заголовок заметки: " + title);
        Log.d("AddNoteActivity", "Дата: " + date);
        Log.d("AddNoteActivity", "Время: " + time);

        // Проверка на пустые поля
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Log.e("AddNoteActivity", "Ошибка: одно из полей пустое!");
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return; // Прерываем выполнение, если одно из полей пустое
        }

        // Логирование, что поля заполнены
        Log.d("AddNoteActivity", "Все поля заполнены, продолжаем сохранение.");

        // Получаем объект базы данных и DAO
        AppDatabase db = App.getDatabase(getApplicationContext());
        TaskDao taskDao = db.taskDao();
        Task task = new Task();
        task.title = title;
        task.date = date;  // Здесь сохраняем дату в формате yyyy-MM-dd
        task.time = time;
        task.status = false;

        // Логирование данных задачи перед сохранением
        Log.d("AddNoteActivity", "Создан объект Task: " + task.toString());

        // Асинхронная вставка данных в базу данных
        new Thread(() -> {
            try {
                // Проверка и логирование DAO
                if (taskDao != null) {
                    Log.d("AddNoteActivity", "TaskDao инициализировано.");
                } else {
                    Log.e("AddNoteActivity", "Ошибка: taskDao не инициализировано.");
                }

                // Вставка данных в базу
                taskDao.insert(task);
                Log.d("AddNoteActivity", "Задача успешно сохранена в базе данных.");

                // Обновление UI на главном потоке
                runOnUiThread(() -> {
                    Toast.makeText(this, "Задача сохранена!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);  // Возвращаем результат в MainActivity
                    finish(); // Закрытие активности после сохранения
                });
            } catch (Exception e) {
                Log.e("AddNoteActivity", "Ошибка при сохранении задачи в базу данных.", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    //    private void sendData(){
//        // Получаем данные из EditText
//        String name = noteName.getText().toString();
//        String time = noteTime.getText().toString();
//        String date = noteDate.getText().toString();
//
//        // Создаём Intent для перехода на вторую активность
//        Intent intent = new Intent(this, MainActivity.class);
//        // Передаём данные через Intent
//        intent.putExtra("EXTRA_NAME", name);
//        intent.putExtra("EXTRA_TIME", time);
//        intent.putExtra("EXTRA_DATE", date);
//
//        // Запускаем вторую активность
//        startActivity(intent);
//    }
}