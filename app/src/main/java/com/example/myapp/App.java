package com.example.myapp;
import android.app.Application;
import android.content.Context;
import androidx.room.Room;

public class App extends Application {
    private static AppDatabase instance;
    public static AppDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "todo_database") // Изменено имя базы
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
    public static void destroyInstance() {
        instance = null;
    }

}
