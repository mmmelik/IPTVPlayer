package com.appbroker.livetvplayer.db;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.appbroker.livetvplayer.MainActivity;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.StringUtils;

@Database(entities = {Channel.class,Category.class}, version = 2)
public abstract class LiveTVDatabase extends RoomDatabase {
    private static LiveTVDatabase instance;

    public abstract ChannelDAO channelDAO();

    public abstract CategoryDAO categoryDAO();

    public static synchronized LiveTVDatabase getInstance(Context context){
        if(instance==null){
            instance= Room.databaseBuilder(context, LiveTVDatabase.class,context.getPackageName())
                    .fallbackToDestructiveMigration()//TODO: version migration.
                    .addCallback(databaseCallback)
                    .build();
        }
        return instance;
    }
    public static RoomDatabase.Callback databaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new Thread(){
                @Override
                public void run() {
                    instance.categoryDAO().addCategory(new Category("Test"));
                    instance.channelDAO().addChannel(new Channel(1,"Test Channel", StringUtils.makeUri("https://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_720p_stereo.avi")));
                }
            }.start();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new Thread(){
                @Override
                public void run() {
                    instance.channelDAO().deleteChannelsInCategory(Constants.CATEGORY_ID_TEMP);
                }
            }.start();
        }
    };
}
