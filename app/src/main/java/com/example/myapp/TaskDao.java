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

    @Query("SELECT status FROM tasks WHERE id = :id")
    boolean getStatus(int id);  // Возвращаем только статус

    @Query("UPDATE tasks SET status = 1 WHERE id = :id")
    void setCompletedStatus(int id); // Метод для изменения статуса на выполненный

    @Query("UPDATE tasks SET status = 0 WHERE id = :id")
    void setUncompletedStatus(int id); // Метод для изменения статуса на невыполненный

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM tasks WHERE id = :id")
    Task getTaskById(int id);
}
