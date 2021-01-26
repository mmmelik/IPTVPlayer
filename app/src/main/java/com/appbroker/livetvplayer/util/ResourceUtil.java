package com.appbroker.livetvplayer.util;


import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

public class ResourceUtil {
    public static @ColorInt int getColorFromAttr(Resources.Theme theme, @AttrRes int attr){
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
}
