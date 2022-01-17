package com.example.mystorage;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ResourceData.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ResourceDataDao getResourceDataDao();
}//*/
