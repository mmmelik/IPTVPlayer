package com.appbroker.livetvplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.PrefHelper;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class ExoPlayerActivity extends AppCompatActivity implements Player.EventListener{
    private int channelId;
    private ChannelViewModel channelViewModel;
    private SimpleExoPlayer player;
    private ImageView favIcon;
    private TextView playerControllerTitle;
    private LinearLayout container;
    private ImageView lockUIIcon;
    private ImageView unlockUIIcon;
    private PlayerView playerView;
    private Channel currentChannel;
    private RelativeLayout lockedController;
    private ImageView backIcon;

    private ViewSwitcher uiSwitcher;
    private boolean isPlaying=false;
    private boolean wasPlaying=false;
    private boolean isUILocked=false;
    private boolean unlockByTwoTap;
    private boolean playInBackground;

    private PrefHelper prefHelper;
    private GestureDetectorCompat gestureDetector;

    private final String TAG=this.getClass().getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSettings();
        prefHelper=new PrefHelper(this);
        setContentView(R.layout.activity_exo_player);

        removeSystemUI();

        Intent intent=getIntent();
        channelId=intent.getIntExtra(Constants.ARGS_CHANNEL_ID,0);
        playerView = findViewById(R.id.player_view);
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

        lockUIIcon=findViewById(R.id.player_controller_lock);
        lockUIIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockUI();
            }
        });

        uiSwitcher=findViewById(R.id.player_controller_switcher);

        unlockUIIcon=findViewById(R.id.locked_player_controller_unlock);
        unlockUIIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockUI();
            }
        });
        playerView.setControllerShowTimeoutMs(2000);
        backIcon=findViewById(R.id.player_controller_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExoPlayerActivity.this.finish();
            }
        });
        gestureDetector=new GestureDetectorCompat(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG,"one tap!");
                return super.onDown(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG,"double tap!");
                if (isUILocked&&unlockByTwoTap){
                    Log.d(TAG,"unlock!");
                    unlockUI();
                    return true;
                }
                return super.onDoubleTap(e);
            }
        });
        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void unlockUI() {
        isUILocked=false;
        uiSwitcher.showNext();
        playerView.setControllerHideOnTouch(true);
        unlockOrientation();
    }

    private void lockUI() {
        isUILocked=true;
        uiSwitcher.showNext();
        playerView.setControllerHideOnTouch(false);
        lockOrientation();
        Toast.makeText(this,R.string.ui_locked,Toast.LENGTH_SHORT).show();
    }

    private void lockOrientation(){
        int currentOrientation=getScreenOrientation();
        setRequestedOrientation(currentOrientation);
        Log.d(TAG, String.valueOf(currentOrientation));
    }

    private void unlockOrientation(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    private void removeSystemUI() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                {
                    removeSystemUI();
                }
            }
        });
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            WindowInsetsController windowInsetsController=getWindow().getInsetsController();
            windowInsetsController.hide(WindowInsets.Type.statusBars()
                    | WindowInsets.Type.navigationBars()
                    | WindowInsets.Type.systemBars()
                    | WindowInsets.Type.displayCutout());
            windowInsetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE);
        }if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        }else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private void adworks() {
        if (!prefHelper.readBooleanPref(Constants.PREF_IS_PREMIUM)){
            player.pause();
            View loading=View.inflate(ExoPlayerActivity.this,R.layout.loading_view,null);
            container=findViewById(R.id.player_activity_container);
            container.addView(loading);
            loading.bringToFront();
            InterstitialAd.load(ExoPlayerActivity.this,Constants.ADMOB_INTERSTITIAL,new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    Log.d("interstitial","loaded");
                    container.removeView(loading);
                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            Log.d("interstitial",adError.getMessage());
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                            Log.d("interstitial","show");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            Log.d("interstitial","dismiss");
                            removeSystemUI();
                            player.play();
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                            Log.d("interstitial","impression");
                        }
                    });
                    interstitialAd.show(ExoPlayerActivity.this);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    container.removeView(loading);
                    player.play();
                    super.onAdFailedToLoad(loadAdError);
                }
            });
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
        if (!playInBackground){
            wasPlaying=isPlaying;
            player.pause();
        }
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
        if (isUILocked){
            playerView.showController();
        }else {
            super.onBackPressed();
        }

    }

    private void getSettings(){
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(ExoPlayerActivity.this);
        unlockByTwoTap = sharedPreferences.getBoolean("pref_unlock_two_tap",true);
        playInBackground = sharedPreferences.getBoolean("pref_unlock_two_tap",false);
    }
}