package com.appbroker.livetvplayer.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Category {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    public Category() {
    }

    @Ignore
    public Category(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Category getCategoryById(List<Category> categories,int id){
        for (Category c:categories){
            if (c.id==id){
                return c;
            }
        }
        return null;
    }
}
