package com.appbroker.livetvplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import com.appbroker.livetvplayer.listener.DataBaseJobListener;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;

public class ExoPlayerActivity extends AppCompatActivity {
    private int channelId;
    private ChannelViewModel channelViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player);
        Intent intent=getIntent();
        channelId=intent.getIntExtra(Constants.ARGS_CHANNEL_ID,0);

        PlayerView playerView = findViewById(R.id.player_view);

        channelViewModel=new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ChannelViewModel.class);
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        channelViewModel.getChannelById(channelId, new DataBaseJobListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(@Nullable Channel channel) {
                if (channel != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MediaItem mediaItem = MediaItem.fromUri(channel.getUri());
                            player.setMediaItem(mediaItem);
                            player.setPlayWhenReady(true);
                            player.prepare();
                        }
                    });
                }
            }
        });
    }
}