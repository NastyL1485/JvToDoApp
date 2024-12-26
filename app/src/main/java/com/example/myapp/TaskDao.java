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

    @Query("SELECT * FROM tasks ORDER BY date ASC")
    List<Task> getAllTasks();

    @Delete
    void deleteTask(Task task);

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    void updateTaskStatus(int taskId, boolean status);

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskById(int id);
}
