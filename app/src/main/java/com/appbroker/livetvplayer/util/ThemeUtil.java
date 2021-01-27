package com.appbroker.livetvplayer.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.StyleRes;

import com.appbroker.livetvplayer.MainActivity;
import com.appbroker.livetvplayer.R;

public class ThemeUtil {
    public static @StyleRes int getPrefTheme(PrefHelper prefHelper){
        int id=prefHelper.readIntPref(Constants.PREF_THEME);
        if (id==-1){
            return R.style.Theme_IPTVPlayerDark;
        }else {
            return id;
        }
    }
    public static boolean isDarkMode(PrefHelper prefHelper){
        if (getPrefTheme(prefHelper)==R.style.Theme_IPTVPlayerDark){
            return true;
        }else {
            return false;
        }
    }
    public static @ColorInt int getColorFromAttr(Context context,@AttrRes int attr){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
    public static @StyleRes int getStyleFromAttr(Context context,@AttrRes int attr){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
}
