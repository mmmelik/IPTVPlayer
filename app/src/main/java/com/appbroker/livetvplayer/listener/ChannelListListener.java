package com.appbroker.livetvplayer.listener;

import com.appbroker.livetvplayer.model.Channel;

import java.util.List;

public interface ChannelListListener {
    void update(List<Channel> channels);
}
