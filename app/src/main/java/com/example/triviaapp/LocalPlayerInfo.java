package com.example.triviaapp;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {PlayerInfo.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class LocalPlayerInfo extends RoomDatabase {
    public abstract PlayerInfoDAO playerInfoDAO();
}
