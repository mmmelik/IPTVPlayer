package com.appbroker.livetvplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.appbroker.livetvplayer.db.ChannelService;
import com.appbroker.livetvplayer.listener.DataBaseJobListener;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;

import java.util.List;

public class ChannelViewModel extends AndroidViewModel {
    private ChannelService channelService;


    public ChannelViewModel(@NonNull Application application) {
        super(application);
        channelService=new ChannelService(application);
    }

    public void addMultipleChannels(List<Channel> channels, DataBaseJobListener dataBaseJobListener){
        channelService.addMultipleChannels(channels,dataBaseJobListener);
    }

    public void registerTempChannels(int categoryId){
        channelService.addTempChannels(categoryId);
    }

    public LiveData<Channel> getChannelById(int id,boolean update){
        return channelService.getChannelById(id, update);
    }

    public void dismissTempChannels(){
        channelService.dismissTempChannels();
    }

    public void addChannel(Channel channel){
        channelService.addChannel(channel);
    }

    public void deleteChannel(Channel channel){
        channelService.deleteChannel(channel);
    }

    public void updateChannel(Channel channel){
        channelService.updateChannel(channel);
    }

    public void updateTempChannelsChecked(boolean isChecked){
        channelService.updateTempChannelsChecked(isChecked);
    }

    public LiveData<List<Channel>> getAllOf(int categoryId) {
        if (categoryId == Constants.CATEGORY_ID_FAV){
            return getFavoriteChannels();
        }else {
            return channelService.getAllOf(categoryId);
        }
    }

    public LiveData<List<Channel>> getFavoriteChannels() {
        return channelService.getFavoriteChannels();
    }

    public LiveData<List<Channel>> getAllChannels() {
        return channelService.getAllChannels();
    }


}
