package com.appbroker.livetvplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.appbroker.livetvplayer.listener.DataBaseJobListener;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.flv.FlvExtractor;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.gms.cast.MediaInfo;

import java.net.MalformedURLException;
import java.net.URL;

public class ExoPlayerActivity extends AppCompatActivity implements Player.EventListener {
    private int channelId;
    private ChannelViewModel channelViewModel;
    private SimpleExoPlayer player;
    private boolean isPlaying=false;
    private boolean wasPlaying=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_exo_player);

        View decorView = getWindow().getDecorView();
        int uiOptions =View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        Intent intent=getIntent();
        channelId=intent.getIntExtra(Constants.ARGS_CHANNEL_ID,0);
        PlayerView playerView = findViewById(R.id.player_view);
        channelViewModel=new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ChannelViewModel.class);
        player = new SimpleExoPlayer.Builder(this).build();
        player.addListener(this);
        playerView.setPlayer(player);
        channelViewModel.getChannelById(channelId, new DataBaseJobListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(@Nullable Channel channel) {
                Log.d("Player",channel.getUri().getPath());
                String s=channel.getUri().getPath();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new URL(s);
                        }catch (MalformedURLException e){

                        }
                        MediaItem mediaItem = MediaItem.fromUri(channel.getUri().getPath());
                        player.setMediaItem(mediaItem);
                        player.setPlayWhenReady(true);
                        player.prepare();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasPlaying=isPlaying;
        player.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(wasPlaying){
            player.play();
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        this.isPlaying=isPlaying;
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (isBehindLiveWindow(error)) {
            // Re-initialize player at the live edge.
            Log.d("Exoplayer","lagging behind.");
            player.stop();
            player.play();
            Mp4Extractor flvExtractor;
            FlvExtractor f;
        } else {
            // Handle other errors
        }
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}