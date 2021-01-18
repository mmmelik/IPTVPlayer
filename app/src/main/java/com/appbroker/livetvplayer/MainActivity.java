package com.appbroker.livetvplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
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
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.PrefHelper;
import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.util.ArrayList;
import java.util.List;

import static com.appbroker.livetvplayer.util.Constants.SKU_REMOVE_ADS;

public class MainActivity extends AppCompatActivity {

    private FrameLayout bannerFrame;
    private RelativeLayout rootLayout;
    private RelativeLayout rootContainer;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar materialToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private View loadingView;

    private PrefHelper prefHelper;
    private BillingClient billingClient;
    private List<String> skuList;
    private SkuDetails skuDetailsNoAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initiateServices();
        setTheme(getPrefTheme());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initiateViews();

    }

    private void initiateViews() {
        rootContainer=findViewById(R.id.rootContainer);
        materialToolbar=findViewById(R.id.toolbar);
        setSupportActionBar(materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout=findViewById(R.id.drawer_layout);
        NavigationView navigationView=findViewById(R.id.navigation_view);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,materialToolbar,R.string.open_drawer,R.string.close_drawer);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        SwitchMaterial switchMaterial=navigationView.getMenu().findItem(R.id.navigation_drawer_dark_mode).getActionView().findViewById(R.id.nav_switch);
        switchMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMaterial.setChecked(!switchMaterial.isChecked());
            }
        });
        switchMaterial.setChecked(isDarkMode());

        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                Log.d("item", (String) item.getTitle());
                if (id==R.id.navigation_drawer_dark_mode){
                    SwitchMaterial switchMaterial=item.getActionView().findViewById(R.id.nav_switch);
                    switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                changeTheme(R.style.Theme_IPTVPlayerDark);
                            }else {
                                changeTheme(R.style.Theme_IPTVPlayer);
                            }
                        }
                    });

                    switchMaterial.setChecked(!isDarkMode());
                    return true;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });




        bannerFrame =findViewById(R.id.bannerFrame);
        rootLayout=findViewById(R.id.rootLayout);
        bottomNavigationView=findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager fragmentManager=getSupportFragmentManager();
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                if (item.getItemId()==R.id.bottom_nav_favorite){
                    FavoriteFragment f= (FavoriteFragment) fragmentManager.findFragmentByTag("fav_fragment");
                    if (f==null){
                        fragmentTransaction.replace(R.id.contentFrame,new FavoriteFragment(),"fav_fragment");
                    }else {
                        fragmentTransaction.replace(R.id.contentFrame,f,"fav_fragment");
                    }
                    fragmentTransaction.commit();
                }else if (item.getItemId()==R.id.bottom_nav_playlist){
                    MyPlaylistsFragment f= (MyPlaylistsFragment) fragmentManager.findFragmentByTag("playlist_fragment");
                    if (f==null){
                        fragmentTransaction.replace(R.id.contentFrame,new MyPlaylistsFragment(),"playlist_fragment");
                    }else {
                        fragmentTransaction.replace(R.id.contentFrame,f,"playlist_fragment");
                    }
                    fragmentTransaction.commit();
                }else if (item.getItemId()==R.id.bottom_nav_notifications){
                    NotificationsFragment f= (NotificationsFragment) fragmentManager.findFragmentByTag("notifications_fragment");
                    if (f==null){
                        fragmentTransaction.replace(R.id.contentFrame,new NotificationsFragment(),"notifications_fragment");
                    }else {
                        fragmentTransaction.replace(R.id.contentFrame,f,"notifications_fragment");
                    }
                    fragmentTransaction.commit();
                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.bottom_nav_playlist);

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
        prefHelper = new PrefHelper(MainActivity.this);
        billingWorks();
        adWorks();
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


    private void billingWorks(){
        PurchasesUpdatedListener purchasesUpdatedListener=new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                Log.d("purchase update",billingResult.getDebugMessage());
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list!=null){
                    for (Purchase purchase : list){
                        handlePurchase(purchase);
                    }
                    snackbar(getString(R.string.purchase_successful));
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
                    snackbar(getString(R.string.purchase_canceled));
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE){
                    snackbar(getString(R.string.service_unavailable));
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE){
                    snackbar(getString(R.string.billing_unavailable));
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE){
                    snackbar(getString(R.string.product_unavailable));
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR){
                    snackbar(getString(R.string.developer_error));
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ERROR){
                    snackbar(getString(R.string.error));
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
                    snackbar(getString(R.string.product_already_owned));
                }else{
                    snackbar(getString(R.string.unknown_error));
                }
                Log.d("response", String.valueOf(billingResult.getResponseCode()));
            }
        };

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.d("billingSetupFinished",billingResult.getDebugMessage());
                checkPurchases();
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d("billing","service disconnected");
            }
        });

    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getSku().equals(SKU_REMOVE_ADS)){
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                if(!purchase.isAcknowledged()){
                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                        @Override
                        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                            Log.d("acknowledgePurchase",billingResult.getDebugMessage());
                        }
                    });
                }
                removeAds();
            }else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING){
                AlertDialog alertDialog=new AlertDialog.Builder(this)
                        .setTitle(R.string.pending_recent_purchase)
                        .setMessage(R.string.pending_purchase_message)
                        .setCancelable(false)
                        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                alertDialog.show();
            }

        }

    }

    private void removeAds() {
        prefHelper.writePref(Constants.PREF_IS_PREMIUM,true);
        bannerFrame.removeAllViews();
    }
    private void checkPurchases() {
        Log.d("checkpurchase", "here");
        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (purchasesResult.getPurchasesList()!=null){
            Log.d("purchaseSize", String.valueOf(purchasesResult.getPurchasesList().size()));
            for (Purchase purchase:purchasesResult.getPurchasesList()){
                handlePurchase(purchase);
                Log.d("purchaseresult",purchase.getSku());
            }
        }else {
            Log.d("purchaseresult","null");
        }
    }

    private void removeAdsButton() {
        skuList = new ArrayList<> ();
        skuList.add(SKU_REMOVE_ADS);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (list != null) {
                        for (SkuDetails skuDetails : list) {
                            if (SKU_REMOVE_ADS.equals(skuDetails.getSku())) {
                                skuDetailsNoAds = skuDetails;
                                BillingFlowParams billingFlowParams=BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsNoAds)
                                        .build();
                                int responseCode=billingClient.launchBillingFlow(MainActivity.this,billingFlowParams).getResponseCode();
                                Log.d("remove_ads_response", String.valueOf(responseCode));
                            }
                        }
                    } else {
                        snackbar(getResources().getString(R.string.unknown_error));
                        Log.d("skuDetail", "list null");
                    }
                }
            }
        });

    }
    public void snackbar(String message){
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG).show();
    }
    private void adWorks() {
        if (prefHelper.readBooleanPref(Constants.PREF_IS_PREMIUM)){
            MobileAds.initialize(this);
            AdView admobBanner=new AdView(this);
            admobBanner.setAdSize(AdSize.SMART_BANNER);
            admobBanner.setAdUnitId(Constants.ADMOB_BANNER);//todo:remove test
            admobBanner.setAdListener(new AdListener(){
                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    Log.d("AdmobAdBanner",loadAdError.getMessage());
                    loadStartAppBanner();
                    //todo:load mopub banner
                }

                @Override
                public void onAdLoaded() {
                    bannerFrame.addView(admobBanner);
                }

            });
            admobBanner.loadAd(new AdRequest.Builder().build());
        }
    }

    private void loadStartAppBanner() {
        StartAppSDK.init(this,Constants.STARTAPP_ID,false);
        StartAppAd.disableSplash();
        StartAppAd.disableAutoInterstitial();
        StartAppSDK.setUserConsent (this,
                "pas",
                System.currentTimeMillis(),
                true);
        Banner banner=new Banner(this);
        banner.setBannerListener(new BannerListener() {
            @Override
            public void onReceiveAd(View view) {
                bannerFrame.addView(view);
            }

            @Override
            public void onFailedToReceiveAd(View view) {
                Log.d("StartAppAd","Failed to load Banner");

            }

            @Override
            public void onImpression(View view) {
                Log.d("StartAppAd","Impression Banner");
            }

            @Override
            public void onClick(View view) {
                Log.d("StartAppAd","Click Banner");
            }
        });
        banner.loadAd();
    }




    private @StyleRes int getPrefTheme(){
        int id=prefHelper.readIntPref(Constants.PREF_THEME);
        if (id==-1){
            return R.style.Theme_IPTVPlayer;
        }else {
            return id;
        }
    }
    private boolean isDarkMode(){
        if (getPrefTheme()==R.style.Theme_IPTVPlayerDark){
            return true;
        }else {
            return false;
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
        }else {
            if(!prefHelper.readBooleanPref(Constants.PREF_IS_RATED)){
                RatingDialog ratingDialog=new RatingDialog.Builder(this)
                        .threshold(4)
                        .title(getResources().getString(R.string.how_was_your_experience))
                        .positiveButtonText(getResources().getString(R.string.not_now))
                        .negativeButtonText(getResources().getString(R.string.never))
                        .formTitle(getResources().getString(R.string.submit))
                        .formHint(getResources().getString(R.string.tell_us))
                        .formSubmitText(getResources().getString(R.string.submit))
                        .formCancelText(getResources().getString(R.string.cancel))
                        .playstoreUrl(getResources().getString(R.string.app_url))
                        .build();
                ratingDialog.show();
                prefHelper.writePref(Constants.PREF_IS_RATED,true);
            }else {
                finish();
            }
        }

    }

}