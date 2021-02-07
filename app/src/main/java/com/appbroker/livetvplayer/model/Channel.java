package com.appbroker.livetvplayer.model;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.appbroker.livetvplayer.util.UriTypeConverter;

@Entity
public class Channel {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ForeignKey(entity = Category.class,parentColumns = "id",childColumns = "category_id")
    private int category_id;

    private String name;

    @TypeConverters(UriTypeConverter.class)
    private Uri uri;

    private boolean favorite;

    private boolean checked=true;

    private long lastWatch;

    public Channel() {
    }

    public Channel(int id) {
        this.id = id;
    }

    @Ignore
    public Channel(int category_id, String name, Uri uri) {
        this.category_id = category_id;
        this.name = name;
        this.uri = uri;
    }

    public long getLastWatch() {
        return lastWatch;
    }

    public void setLastWatch(long lastWatch) {
        this.lastWatch = lastWatch;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
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

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
