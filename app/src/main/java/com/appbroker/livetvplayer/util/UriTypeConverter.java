package com.appbroker.livetvplayer.util;

import android.net.Uri;

import androidx.room.TypeConverter;

public class UriTypeConverter {
    @TypeConverter
    public static Uri toUri(String s){
        return Uri.parse(s);
    }
    @TypeConverter
    public static String toString(Uri uri){
        return uri.toString();
    }
}
