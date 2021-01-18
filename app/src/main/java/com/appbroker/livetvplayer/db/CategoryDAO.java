package com.appbroker.livetvplayer.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.appbroker.livetvplayer.model.Category;

import java.util.List;

@Dao
public interface CategoryDAO {
    @Query("SELECT * FROM category")
    LiveData<List<Category>> getAll();

    @Query("SELECT * FROM category WHERE id=:id")
    LiveData<Category> getCategoryById(int id);

    @Insert
    void addCategory(Category category);

    @Update
    void updateCategory(Category category);

    @Delete
    void deleteCategory(Category category);


}

