package com.appbroker.livetvplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefHelper {
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PrefHelper(Context context) {
        this.context=context;
        sharedPreferences=context.getSharedPreferences(Constants.APP_ID,Context.MODE_PRIVATE);
    }

    public String readStringPref(String name){
        return sharedPreferences.getString(name,null);
    }

    public Integer readIntPref(String name){
        return sharedPreferences.getInt(name,-1);
    }

    public Boolean readBooleanPref(String name){
        return sharedPreferences.getBoolean(name,false);
    }

    public void writePref(String name,int value){
        editor=sharedPreferences.edit();
        editor.putInt(name,value);
        editor.apply();
    }

    public void writePref(String name,boolean value){
        editor=sharedPreferences.edit();
        editor.putBoolean(name,value);
        editor.apply();
    }
}
