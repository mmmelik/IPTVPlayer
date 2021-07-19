package com.appbroker.livetvplayer.util;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import java.util.ArrayList;
import java.util.List;

public class BillingUtil implements PurchasesUpdatedListener{

    private final String TAG=BillingUtil.class.getName();
    private Activity activity;
    private BillingClient billingClient;
    private List<SkuDetails> skuDetails;


    public BillingUtil(Activity activity) {
        this.activity = activity;
        this.billingClient= BillingClient.newBuilder(activity)
                .setListener(this)
                .enablePendingPurchases()
                .build();
        startConnection();
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode()== BillingClient.BillingResponseCode.OK && list!=null){
            for (Purchase purchase:list){
                handlePurchase(purchase);
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState()== Purchase.PurchaseState.PURCHASED){
            if (!purchase.isAcknowledged()){
                AcknowledgePurchaseParams params=AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        Log.d(TAG,"AcknowledgePurchaseResponse: "+billingResult.getDebugMessage());
                    }
                });
            }

            switch (purchase.getSkus().get(0)) {//TODO:Consider multiple purchases.
                case Constants.SKU_REMOVE_ADS:
                    PrefHelper prefHelper=new PrefHelper(activity);
                    prefHelper.writePref(Constants.PREF_IS_PREMIUM,true);
                    Log.d(TAG,"Handle Purchase "+ purchase.getSkus().get(0));
                    break;
            }
        }
    }


    private void startConnection(){
        final BillingClientStateListener billingClientStateListener=new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG,"Billing service disconnected.");
                if (ConnectionUtils.isInternetAvailable(activity)){
                    billingClient.startConnection(this);
                }
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.d(TAG,"Billing service is ready. "+billingResult.getDebugMessage());
                getSkuDetails();
            }
        };
        billingClient.startConnection(billingClientStateListener);
    }

    private void getSkuDetails(){
        List<String> skuList=new ArrayList<>();
        skuList.add(Constants.SKU_REMOVE_ADS);
        SkuDetailsParams.Builder builder=SkuDetailsParams.newBuilder();
        builder.setSkusList(skuList);
        billingClient.querySkuDetailsAsync(builder.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                skuDetails=list;
                Log.d(TAG,"BillingResult: "+billingResult.getDebugMessage()+" ___"+list.size());
            }
        });
    }

    public @BillingClient.BillingResponseCode int launchPurchaseFlow(){
        if (billingClient.isReady()){
            if (skuDetails.size()>0){
                BillingFlowParams billingFlowParams=BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetails.get(0))
                        .build();
                return billingClient.launchBillingFlow(activity,billingFlowParams).getResponseCode();
            }
            return BillingClient.BillingResponseCode.DEVELOPER_ERROR;
        }else {
            return BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE;
        }

    }
}
