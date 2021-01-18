package com.appbroker.livetvplayer.adapter;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.listener.ChannelListListener;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChannelAddRecyclerViewAdapter extends RecyclerView.Adapter {
    private List<Channel> channels;
    private ChannelViewModel channelViewModel;
    private ChannelListListener channelListListener;

    public ChannelAddRecyclerViewAdapter(Fragment fragment) {
        this.channels=new ArrayList<>();
        this.channelViewModel=new ViewModelProvider(fragment, new ViewModelProvider.AndroidViewModelFactory(fragment.getActivity().getApplication())).get(ChannelViewModel.class);
        this.channelViewModel.getAllOf(Constants.CATEGORY_ID_TEMP).observe(fragment, new Observer<List<Channel>>() {
            @Override
            public void onChanged(List<Channel> channels) {
                ChannelAddRecyclerViewAdapter.this.channels=channels;
                ChannelAddRecyclerViewAdapter.this.notifyDataSetChanged();
                if (channelListListener!=null){
                    channelListListener.update(channels);
                }
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_channel_add,parent,false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((CustomViewHolder)holder).bind(position);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }
    private class CustomViewHolder extends RecyclerView.ViewHolder{
        private final TextView nameView;
        private final TextView uriView;
        private final CheckBox checkBox;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView=itemView.findViewById(R.id.add_list_item_name);
            uriView=itemView.findViewById(R.id.add_list_item_uri);
            checkBox=itemView.findViewById(R.id.add_list_item_check);
        }
        public void bind(int position){
            Channel channel=channels.get(position);
            nameView.setText(channel.getName());
            uriView.setText(channel.getUri().getPath());//todo:bak
            checkBox.setChecked(channel.isChecked());
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    channel.setChecked(!channel.isChecked());
                    channelViewModel.updateChannel(channel);
                }
            });
        }

    }

    public void setChannelListListener(ChannelListListener channelListListener) {
        this.channelListListener = channelListListener;
    }
}
