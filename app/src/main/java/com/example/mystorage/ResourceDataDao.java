package com.example.mystorage;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Dao
public interface ResourceDataDao {
    // Добавление Person в бд
    @Insert
    void insert(ResourceData resourceData);

    @Update
    void update(ResourceData resourceData);

    @Update
    void updateMany(List<ResourceData> resourceDataList);

    // Удаление Person из бд
    @Delete
    void delete(ResourceData resourceData);

    @Query("DELETE FROM resourcedata")
    void deleteAll();

    @Query("DELETE FROM resourcedata WHERE id = :id")
    abstract void deleteById(long id);

    @Query("SELECT * FROM resourcedata WHERE id = :id")
    ResourceData getResourceDataById(long id);

    // Получение всех Person из бд
    @Query("SELECT * FROM resourcedata")
    List<ResourceData> getAllResourceData();

    @Query("SELECT id, resource FROM resourcedata")
    public List<ResourceDataMinimal> getAllResourceDataMinimal();



}//*/
