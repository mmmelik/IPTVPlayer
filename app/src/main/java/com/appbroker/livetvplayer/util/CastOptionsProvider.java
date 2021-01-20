package com.appbroker.livetvplayer.util;

import android.content.Context;

import androidx.annotation.Nullable;

import com.appbroker.livetvplayer.R;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.List;

public class CastOptionsProvider implements OptionsProvider {
    @Override
    public CastOptions getCastOptions(Context context) {
        CastOptions castOptions = new CastOptions.Builder()
                .setReceiverApplicationId(Constants.APP_ID)
                .build();
        return castOptions;
    }

    @Nullable
    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}
