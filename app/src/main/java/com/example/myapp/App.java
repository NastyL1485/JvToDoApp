package com.example.myapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.room.Room;

public class App extends Application {

    // Используем volatile для безопасного многозадачного доступа
    private static volatile AppDatabase instance;

    @Override
    public void onCreate() {
        super.onCreate();
        // Инициализация базы данных
        if (instance == null) {
            synchronized (App.class) {
                if (instance == null) {
                    try {
                        // Попытка инициализации базы данных
                        instance = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "todo_database")
                                .fallbackToDestructiveMigration() // Поддержка миграций
                                .build();
                        Log.d("App", "База данных успешно инициализирована.");
                    } catch (Exception e) {
                        Log.e("App", "Ошибка при инициализации базы данных.", e);
                    }
                }
            }
        }
    }

    // Метод для получения экземпляра базы данных
    public static AppDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (App.class) {
                if (instance == null) {
                    try {
                        // Если база данных не инициализирована, инициализируем ее
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

    // Метод для разрушения экземпляра базы данных (например, при закрытии приложения)
    public static void destroyInstance() {
        if (instance != null) {
            try {
                instance.close(); // Закрытие базы данных
                instance = null;
                Log.d("App", "Экземпляр базы данных уничтожен.");
            } catch (Exception e) {
                Log.e("App", "Ошибка при разрушении экземпляра базы данных.", e);
            }
        }
    }
}






//import android.app.Application;
//import android.content.Context;
//import androidx.room.Room;
//
//public class App extends Application {
//    private static AppDatabase instance;
//    public static AppDatabase getDatabase(final Context context) {
//        if (instance == null) {
//            synchronized (AppDatabase.class) {
//                if (instance == null) {
//                    instance = Room.databaseBuilder(context.getApplicationContext(),
//                                    AppDatabase.class, "todo_database") // Изменено имя базы
//                            .fallbackToDestructiveMigration()
//                            .build();
//                }
//            }
//        }
//        return instance;
//    }
//    public static void destroyInstance() {
//        instance = null;
//    }
//
//}
