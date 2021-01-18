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
                    for (int i=0;i<5;i++){
                        instance.categoryDAO().addCategory(new Category("Category "+i));
                        for (int j=0;j<50;j++){
                            instance.channelDAO().addChannel(new Channel(i+1,randomString(10), Uri.parse(randomString(50))));
                        }
                    }
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
    public static String randomString(int size) {
        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(size);

        for (int i = 0; i < size; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
