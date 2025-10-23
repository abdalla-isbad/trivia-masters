package com.example.triviaapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public interface PlayerInfoDAO {
    @Query("UPDATE playerinfo SET cash = cash + :cashDelta WHERE id = :playerId")
    void addCash(int playerId, int cashDelta);
    @Query("SELECT * FROM playerinfo WHERE id = :id")
    PlayerInfo loadById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlayerInfo(PlayerInfo playerInfo);

    @Transaction
    default void updatePlayerAndCash(PlayerInfo playerInfo, int cash) {
        updatePlayerInfo(playerInfo);
        addCash(playerInfo.id, cash);
    }
    @Update
    void updatePlayerInfo(PlayerInfo playerInfo);

    @Delete
    void delete(PlayerInfo playerInfo);

    @Query("SELECT * FROM playerinfo ORDER BY id DESC LIMIT 1")
    PlayerInfo getLatestPlayerInfo();
}
