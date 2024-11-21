package com.example.myapp;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insert(Task task);

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks WHERE status = 1") // 1 - true
    List<Task> getCompletedTasks();

    @Query("SELECT * FROM tasks WHERE status = 0") // 0 - false
    List<Task> getPendingTasks();

    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteTask(int id);

    @Query("SELECT * FROM tasks WHERE date = :date")
    List<Task> getTasksByDate(String date);

    // другие методы по необходимости
}
