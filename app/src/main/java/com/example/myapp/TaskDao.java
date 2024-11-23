package com.example.myapp;
import androidx.room.Dao;
import androidx.room.Delete;
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

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskById(int id);

    // другие методы по необходимости
}
