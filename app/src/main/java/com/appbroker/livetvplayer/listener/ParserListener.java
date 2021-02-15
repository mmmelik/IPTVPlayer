package com.appbroker.livetvplayer.listener;

import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Enums;

import java.io.File;
import java.util.List;

public interface ParserListener {
    void onFinish(Enums.ParseResult parseResult, List<Channel> channelList, String message);
    void onCreateFile(File f);
    void onError(Exception e);
}