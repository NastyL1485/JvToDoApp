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

import java.util.Calendar;

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

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);

                    selectedDateTextView.setText("Выбранная дата: " + formattedDate);

                    noteDate.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void saveTask() {
        // Получаем данные из полей ввода
        String title = noteName.getText().toString();
        String date = noteDate.getText().toString();
        String time = noteTime.getText().toString();

        Log.d("AddNoteActivity", "Заголовок заметки: " + title);
        Log.d("AddNoteActivity", "Дата: " + date);
        Log.d("AddNoteActivity", "Время: " + time);

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Log.e("AddNoteActivity", "Ошибка: одно из полей пустое!");
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("AddNoteActivity", "Все поля заполнены, продолжаем сохранение.");

        AppDatabase db = App.getDatabase(getApplicationContext());
        TaskDao taskDao = db.taskDao();
        Task task = new Task();
        task.title = title;
        task.date = date;
        task.time = time;
        task.status = false;

        Log.d("AddNoteActivity", "Создан объект Task: " + task.toString());

        new Thread(() -> {
            try {
                if (taskDao != null) {
                    Log.d("AddNoteActivity", "TaskDao инициализировано.");
                } else {
                    Log.e("AddNoteActivity", "Ошибка: taskDao не инициализировано.");
                }

                taskDao.insert(task);
                Log.d("AddNoteActivity", "Задача успешно сохранена в базе данных.");

                runOnUiThread(() -> {
                    Toast.makeText(this, "Задача сохранена!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                Log.e("AddNoteActivity", "Ошибка при сохранении задачи в базу данных.", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}