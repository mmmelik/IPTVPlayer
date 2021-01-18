package com.appbroker.livetvplayer.listener;

import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Enums;

import java.util.List;

public interface ParserListener {
    void onFinish(Enums.ParseResult parseResult, List<Channel> channelList, String message);
    void onError(Exception e);
}