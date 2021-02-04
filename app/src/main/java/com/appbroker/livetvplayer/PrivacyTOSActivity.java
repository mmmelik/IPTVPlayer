package com.appbroker.livetvplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.appbroker.livetvplayer.util.Constants;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;
import java.io.InputStream;

public class PrivacyTOSActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_tos);
        toolbar=findViewById(R.id.toolbar_tos_privacy);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        text=findViewById(R.id.text_tos_privacy);
        Intent intent=getIntent();
        String type=intent.getStringExtra(Constants.ARGS_ACTIVITY_TYPE);
        if(Constants.TYPE_TOS.equals(type)){
            loadType();
        }else if (Constants.TYPE_PRIVACY.equals(type)){
            loadPrivacy();
        }
    }

    private void loadPrivacy() {
        try {
            getSupportActionBar().setTitle(R.string.privacy);
            InputStream inputStream=getAssets().open("privacy.html");
            int c;
            StringBuilder builder=new StringBuilder();
            while ((c=inputStream.read())!=-1){
                builder.append((char) c);
            }
            text.setText(Html.fromHtml(builder.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadType() {
        try {
            getSupportActionBar().setTitle(R.string.tos);
            InputStream inputStream=getAssets().open("tos.html");
            int c;
            StringBuilder builder=new StringBuilder();
            while ((c=inputStream.read())!=-1){
                builder.append((char) c);
            }
            text.setText(Html.fromHtml(builder.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}