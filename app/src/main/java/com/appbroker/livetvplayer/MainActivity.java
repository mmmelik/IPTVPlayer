package com.appbroker.livetvplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.appbroker.livetvplayer.fragment.FavoriteFragment;
import com.appbroker.livetvplayer.fragment.MyPlaylistsFragment;
import com.appbroker.livetvplayer.fragment.NotificationsFragment;
import com.appbroker.livetvplayer.fragment.SearchFragment;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.PrefHelper;
import com.appbroker.livetvplayer.util.ThemeUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.appbroker.livetvplayer.util.Constants.SKU_REMOVE_ADS;

public class MainActivity extends AppCompatActivity {
    private FrameLayout bannerContainer;
    private RelativeLayout rootLayout;
    private RelativeLayout rootContainer;
    private RelativeLayout contentFrameContainer;
    private BottomNavigationView bottomNavigationView;
    public DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar materialToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private View loadingView;

    private PrefHelper prefHelper;

    private FavoriteFragment favoriteFragment;
    private MyPlaylistsFragment myPlaylistsFragment;
    private NotificationsFragment notificationsFragment;
    private SearchFragment searchFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefHelper=new PrefHelper(MainActivity.this);
        setTheme(ThemeUtil.getPrefTheme(prefHelper));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateServices();
    }

    private void checkPremium() {
        if(prefHelper.readBooleanPref(Constants.PREF_IS_PREMIUM)){
            navigationView.getMenu().findItem(R.id.navigation_drawer_account_status).setIcon(R.drawable.ic_baseline_stars_24);
            navigationView.getMenu().findItem(R.id.navigation_drawer_account_status).setTitle(R.string.premium);
        }else {
            navigationView.getMenu().findItem(R.id.navigation_drawer_account_status).setIcon(R.drawable.ic_baseline_block_24);
            navigationView.getMenu().findItem(R.id.navigation_drawer_account_status).setTitle(R.string.free);
        }

    }

    private void initiateViews() {
        bannerContainer=findViewById(R.id.banner_layout);
        contentFrameContainer=findViewById(R.id.contentFrameContainer);
        rootContainer=findViewById(R.id.rootContainer);
        materialToolbar=findViewById(R.id.toolbar);
        setSupportActionBar(materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.navigation_view);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,materialToolbar,R.string.open_drawer,R.string.close_drawer);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        rootContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //rootContainer.getChildAt(0).getHitRect();
                Log.d("touch", String.valueOf(event.getButtonState()));
                return true;
            }
        });

        SwitchMaterial switchMaterial=navigationView.getMenu().findItem(R.id.navigation_drawer_dark_mode).getActionView().findViewById(R.id.nav_switch);
        switchMaterial.setChecked(ThemeUtil.isDarkMode(prefHelper));
        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    changeTheme(R.style.Theme_IPTVPlayerDark);
                }else {
                    changeTheme(R.style.Theme_IPTVPlayerLight);
                }
            }
        });
        checkPremium();

        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                Log.d("item", (String) item.getTitle());
                if (id==R.id.navigation_drawer_dark_mode){
                    SwitchMaterial switchMaterial=item.getActionView().findViewById(R.id.nav_switch);
                    switchMaterial.setChecked(!ThemeUtil.isDarkMode(prefHelper));
                    return true;
                }else if (id==R.id.navigation_drawer_contact){
                    Intent intent=new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",getString(R.string.dev_mail),null));
                    startActivity(Intent.createChooser(intent,getString(R.string.send_mail)));
                }else if (id==R.id.navigation_drawer_tos){
                    Intent intent=new Intent(MainActivity.this,PrivacyTOSActivity.class);
                    intent.putExtra(Constants.ARGS_ACTIVITY_TYPE,Constants.TYPE_TOS);
                    startActivity(intent);
                }else if (id==R.id.navigation_drawer_privacy){
                    Intent intent=new Intent(MainActivity.this,PrivacyTOSActivity.class);
                    intent.putExtra(Constants.ARGS_ACTIVITY_TYPE,Constants.TYPE_PRIVACY);
                    startActivity(intent);
                }else if (id==R.id.navigation_drawer_account_status){
                    if (!prefHelper.readBooleanPref(Constants.PREF_IS_PREMIUM)){
                        showBuyPremiumDialog();
                    }
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });


        rootLayout=findViewById(R.id.rootLayout);
        bottomNavigationView=findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager fragmentManager=getSupportFragmentManager();
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                if (item.getItemId()==R.id.bottom_nav_favorite){
                    favoriteFragment= (FavoriteFragment) fragmentManager.findFragmentByTag(Constants.TAG_FAV_FRAGMENT);
                    if (favoriteFragment==null){
                        favoriteFragment=new FavoriteFragment();
                    }
                    replaceFragment(favoriteFragment,Constants.TAG_FAV_FRAGMENT);
                }else if (item.getItemId()==R.id.bottom_nav_playlist){
                    myPlaylistsFragment=(MyPlaylistsFragment) fragmentManager.findFragmentByTag(Constants.TAG_MY_PLAYLIST_FRAGMENT);
                    if (myPlaylistsFragment==null){
                        myPlaylistsFragment=new MyPlaylistsFragment();
                    }
                    replaceFragment(myPlaylistsFragment,Constants.TAG_MY_PLAYLIST_FRAGMENT);
                }else if (item.getItemId()==R.id.bottom_nav_notifications){
                    notificationsFragment=(NotificationsFragment) fragmentManager.findFragmentByTag(Constants.TAG_NOTIFICATIONS_FRAGMENT);
                    if (notificationsFragment==null){
                        notificationsFragment=new NotificationsFragment();
                    }
                    replaceFragment(notificationsFragment,Constants.TAG_NOTIFICATIONS_FRAGMENT);
                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.bottom_nav_playlist);
    }

    private void showBuyPremiumDialog() {
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void initiateServices() {
        initiateViews();
        adWorks();
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    public void setLoading(boolean b){

        if (b){
            if (loadingView==null){
                loadingView=View.inflate(this,R.layout.loading_view,null);
            }
            RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            rootContainer.addView(loadingView,layoutParams);
            loadingView.bringToFront();
        }else {
            if (loadingView!=null){
                rootContainer.removeView(loadingView);
            }
        }
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            drawerLayout.openDrawer(Gravity.LEFT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void snackbar(String message, String actionLabel, View.OnClickListener onClickListener){
        if (actionLabel!=null){
            Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).setAction(actionLabel,onClickListener).show();
        }else {
            Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show();
        }

    }

    private void adWorks() {
        if (!prefHelper.readBooleanPref(Constants.PREF_IS_PREMIUM)){//todo:check
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    Map<String, AdapterStatus> map=initializationStatus.getAdapterStatusMap();
                    for (String key:map.keySet()){
                        AdapterStatus adapterStatus=map.get(key);
                        Log.d("Ad Network Init",key+":"+adapterStatus.getInitializationState().toString());
                    }
                }
            });
            AdView banner=new AdView(MainActivity.this);
            banner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            banner.setAdUnitId(Constants.ADMOB_BANNER_TEST);
            banner.setAdSize(AdSize.SMART_BANNER);
            bannerContainer.addView(banner);
            AdRequest adRequest=new AdRequest.Builder().build();
            banner.loadAd(adRequest);
        }
    }



    private synchronized void changeTheme(@StyleRes int id){
        new Thread(){
            @Override
            public void run() {
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                prefHelper.writePref(Constants.PREF_THEME,id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.recreate();
                    }
                });

            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)){
            drawerLayout.closeDrawer(Gravity.LEFT);
        }else if (myPlaylistsFragment.isVisible()&&myPlaylistsFragment.isFABOpen()){
            myPlaylistsFragment.closeFAB();
        }else {
            if(!prefHelper.readBooleanPref(Constants.PREF_IS_RATED)){

                prefHelper.writePref(Constants.PREF_IS_RATED,true);
            }else {
                finish();
            }
        }
    }

    public void detachSearchFragment(){
        searchFragmentChangeState(false,null);
        searchFragment=null;
    }
    public void searchFragmentChangeState(boolean show,String query){
        Log.d("searchFragment", String.valueOf(show));
        if(show){
            if(searchFragment==null){
                searchFragment=new SearchFragment(getApplication());
                FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.search_frame,searchFragment,Constants.TAG_SEARCH_FRAGMENT);
                fragmentTransaction.commit();
            }
            contentFrameContainer.findViewById(R.id.search_frame).setVisibility(View.VISIBLE);
            contentFrameContainer.findViewById(R.id.contentFrame).setVisibility(View.GONE);

            searchFragment.updateQuery(query);
        }else {
            contentFrameContainer.findViewById(R.id.contentFrame).setVisibility(View.VISIBLE);
            contentFrameContainer.findViewById(R.id.search_frame).setVisibility(View.GONE);
        }
    }

    private void replaceFragment(Fragment fragment,String tag){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.contentFrame,fragment,tag);
        fragmentTransaction.commit();
    }
}