package com.appbroker.livetvplayer.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;

import java.util.List;

@Dao
public interface ChannelDAO {
    @Query("SELECT * FROM channel WHERE id=:id LIMIT 1")
    Channel getChannelById(int id);

    @Query("SELECT * FROM channel")
    LiveData<List<Channel>> getAll();

    @Query("SELECT * FROM channel WHERE favorite=1")
    LiveData<List<Channel>> getFavorites();

    @Query("SELECT * FROM channel WHERE category_id=:categoryId")
    LiveData<List<Channel>> getAllOf(int categoryId);

    @Insert
    void addChannel(Channel channel);

    @Insert
    void addChannelBatch(List<Channel> channels);

    @Update
    void updateChannel(Channel channel);


    @Delete
    void deleteChannel(Channel channel);

    @Query("DELETE FROM channel WHERE category_id=:categoryId")
    void deleteChannelsInCategory(int categoryId);

    @Query("UPDATE channel SET category_id=:categoryId WHERE category_id=-2 AND checked=1")
    void addTempChannels(int categoryId);

    @Query("UPDATE channel SET checked=:isChecked WHERE category_id=-2")
    void updateTempChannelsChecked(boolean isChecked);
}
