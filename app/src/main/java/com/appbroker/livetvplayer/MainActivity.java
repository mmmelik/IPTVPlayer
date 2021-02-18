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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.appbroker.livetvplayer.fragment.SearchFragment;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.PrefHelper;
import com.appbroker.livetvplayer.util.ThemeUtil;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.api.App;
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

import java.util.ArrayList;
import java.util.List;

import static com.appbroker.livetvplayer.util.Constants.SKU_REMOVE_ADS;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout rootLayout;
    private RelativeLayout rootContainer;
    private RelativeLayout contentFrameContainer;
    private BottomNavigationView bottomNavigationView;
    public DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar materialToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private View loadingView;

    private BillingClient billingClient;
    private List<String> skuList;
    private SkuDetails skuDetailsNoAds;
    private PrefHelper prefHelper;

    private FavoriteFragment favoriteFragment;
    private MyPlaylistsFragment myPlaylistsFragment;
    private NotificationsFragment notificationsFragment;
    private SearchFragment searchFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initiateServices();

        setTheme(ThemeUtil.getPrefTheme(prefHelper));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initiateViews();
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
                        removeAdsButton();
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
        prefHelper=new PrefHelper(MainActivity.this);
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
                    snackbar(getString(R.string.purchase_successful),null,null);
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
                    snackbar(getString(R.string.purchase_canceled), getString(R.string.undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeAdsButton();
                        }
                    });
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE){
                    snackbar(getString(R.string.service_unavailable),null,null);
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE){
                    snackbar(getString(R.string.billing_unavailable),null,null);
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE){
                    snackbar(getString(R.string.product_unavailable),null,null);
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR){
                    snackbar(getString(R.string.developer_error),null,null);
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ERROR){
                    snackbar(getString(R.string.error),null,null);
                }else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
                    snackbar(getString(R.string.product_already_owned),null,null);
                }else{
                    snackbar(getString(R.string.unknown_error),null,null);
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
                Log.d("removeads","purchased");
                checkPremium();
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
                Log.d("removeads","pending");
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
        Appodeal.destroy(Appodeal.BANNER_VIEW);
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
                        snackbar(getResources().getString(R.string.unknown_error),null,null);
                        Log.d("skuDetail", "list null");
                    }
                }
            }
        });

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
            Appodeal.setTesting(BuildConfig.DEBUG);
            Appodeal.disableLocationPermissionCheck();
            Appodeal.setBannerViewId(R.id.appodeal_banner);
            Appodeal.setBannerCallbacks(new BannerCallbacks() {
                @Override
                public void onBannerLoaded(int i, boolean b) {
                    Log.d("banner", String.valueOf(i));
                }

                @Override
                public void onBannerFailedToLoad() {
                    Log.d("banner","failed to load");
                }

                @Override
                public void onBannerShown() {
                    Log.d("banner","show");

                }

                @Override
                public void onBannerShowFailed() {
                    Log.d("banner","show failed");
                }

                @Override
                public void onBannerClicked() {
                    Log.d("banner","clicked");
                }

                @Override
                public void onBannerExpired() {
                    Log.d("banner","expired");
                }
            });
            Appodeal.initialize(this, Constants.APPODEAL_ID, Appodeal.BANNER|Appodeal.INTERSTITIAL,true);
            Appodeal.show(MainActivity.this,Appodeal.BANNER_VIEW);
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