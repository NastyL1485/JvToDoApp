package com.example.myapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.room.Room;

public class App extends Application {

    private static volatile AppDatabase instance;

    @Override
    public void onCreate() {
        super.onCreate();
        if (instance == null) {
            synchronized (App.class) {
                if (instance == null) {
                    try {
                        instance = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "todo_database")
                                .fallbackToDestructiveMigration()
                                .build();
                        Log.d("App", "База данных успешно инициализирована.");
                    } catch (Exception e) {
                        Log.e("App", "Ошибка при инициализации базы данных.", e);
                    }
                }
            }
        }
    }

    public static AppDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (App.class) {
                if (instance == null) {
                    try {
                        instance = Room.databaseBuilder(context.getApplicationContext(),
                                        AppDatabase.class, "todo_database")
                                .fallbackToDestructiveMigration()
                                .build();
                        Log.d("App", "База данных успешно инициализирована.");
                    } catch (Exception e) {
                        Log.e("App", "Ошибка при инициализации базы данных.", e);
                    }
                }
            }
        }
        if (instance == null) {
            Log.e("App", "Ошибка: база данных не инициализирована.");
        }
        return instance;
    }
}