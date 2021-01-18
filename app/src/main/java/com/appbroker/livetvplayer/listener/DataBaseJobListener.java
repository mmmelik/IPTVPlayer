package com.appbroker.livetvplayer.listener;

import androidx.annotation.Nullable;

import com.appbroker.livetvplayer.model.Channel;

public interface DataBaseJobListener {
    void onStart();
    void onFinish(@Nullable Channel channel);
}
