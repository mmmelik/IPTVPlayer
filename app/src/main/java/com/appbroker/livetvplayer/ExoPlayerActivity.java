package com.appbroker.livetvplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Visibility;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appbroker.livetvplayer.listener.DataBaseJobListener;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.PrefHelper;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.api.App;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.flv.FlvExtractor;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class ExoPlayerActivity extends AppCompatActivity implements Player.EventListener {
    private int channelId;
    private ChannelViewModel channelViewModel;
    private SimpleExoPlayer player;
    private ImageView favIcon;
    private TextView playerControllerTitle;

    private Channel currentChannel;
    private boolean isPlaying=false;
    private boolean wasPlaying=false;

    private PrefHelper prefHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        prefHelper=new PrefHelper(this);
        setContentView(R.layout.activity_exo_player);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            WindowInsetsController windowInsetsController=getWindow().getInsetsController();
            windowInsetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars() | WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            windowInsetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE);
        }if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }else {
            getWindow().addFlags(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }


        Intent intent=getIntent();
        channelId=intent.getIntExtra(Constants.ARGS_CHANNEL_ID,0);
        PlayerView playerView = findViewById(R.id.player_view);
        channelViewModel=new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ChannelViewModel.class);
        player = new SimpleExoPlayer.Builder(this).build();
        player.addListener(this);
        playerView.setPlayer(player);
        playerView.setControllerShowTimeoutMs(5000);
        channelViewModel.getChannelById(channelId,true).observe(ExoPlayerActivity.this, new Observer<Channel>() {
            @Override
            public void onChanged(Channel channel) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        favIcon.setSelected(channel.isFavorite());
                        channelUpdated(channel);
                    }
                });
            }
        });
        favIcon=playerView.findViewById(R.id.player_controller_fav_icon);
        favIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentChannel.setFavorite(!favIcon.isSelected());
                channelViewModel.updateChannel(currentChannel);
            }
        });
        playerControllerTitle=playerView.findViewById(R.id.player_controller_title);
        adworks();
    }

    private void adworks() {
        if (prefHelper.readBooleanPref(Constants.PREF_IS_PREMIUM)){
            Log.d("networks", Arrays.toString(Appodeal.getNetworks(ExoPlayerActivity.this,Appodeal.INTERSTITIAL).toArray()));

            if (Appodeal.canShow(Appodeal.INTERSTITIAL)){
                Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
                    @Override
                    public void onInterstitialLoaded(boolean b) {
                        Log.d("appodeal","loaded");
                    }

                    @Override
                    public void onInterstitialFailedToLoad() {
                        Log.d("appodeal","failed to load");
                    }

                    @Override
                    public void onInterstitialShown() {
                        player.pause();
                        Log.d("appodeal","show");
                    }

                    @Override
                    public void onInterstitialShowFailed() {
                        Log.d("appodeal","failed to show");
                    }

                    @Override
                    public void onInterstitialClicked() {
                        Log.d("appodeal","click");
                    }

                    @Override
                    public void onInterstitialClosed() {
                        player.play();
                        Appodeal.destroy(Appodeal.INTERSTITIAL);
                        Log.d("appodeal","closed");
                    }

                    @Override
                    public void onInterstitialExpired() {
                        Log.d("appodeal","expired");
                    }
                });
                Appodeal.show(ExoPlayerActivity.this,Appodeal.INTERSTITIAL);
            }
        }
    }

    private void channelUpdated(Channel channel) {
        Log.d("channel",channel.getUri().getPath());
        if (currentChannel==null||currentChannel.getId()!=channel.getId()){
            MediaItem mediaItem = MediaItem.fromUri(channel.getUri().getPath());
            player.setMediaItem(mediaItem);
            player.setPlayWhenReady(true);
            player.prepare();
            playerControllerTitle.setText(channel.getName());
        }
        currentChannel=channel;

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
        Log.d("exo_error",error.getMessage());
        if (isBehindLiveWindow(error)) {
            // Re-initialize player at the live edge.
            Log.d("Exoplayer","lagging behind.");
            player.stop();
            player.play();
        } else {
            // Handle other errors
           error.printStackTrace();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}