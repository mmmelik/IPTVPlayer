package com.appbroker.livetvplayer.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.appbroker.livetvplayer.ExoPlayerActivity;
import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchListRecyclerAdapter extends RecyclerView.Adapter {
    private Fragment fragment;
    private List<Channel> channels;
    private List<Category> categories;

    public SearchListRecyclerAdapter(Fragment fragment) {
        this.fragment = fragment;
        this.channels = new ArrayList<>();
        this.categories=new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(fragment.getContext()).inflate(R.layout.list_item_search_channel,parent,false);
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

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout container;
        private TextView nameText;
        private TextView categoryText;
        private TextView lastWatchText;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            container=itemView.findViewById(R.id.list_item_search_container);
            nameText=itemView.findViewById(R.id.list_item_search_channel_name);
            categoryText=itemView.findViewById(R.id.list_item_search_channel_category);
            lastWatchText=itemView.findViewById(R.id.list_item_search_channel_last_watch);
        }

        public void bind(int position){
            Channel channel=channels.get(position);
            nameText.setText(channel.getName());
            categoryText.setText(getCategoryNameFromList(channel.getCategory_id()));
            if (channel.getLastWatch()!=0){
                String l=getString(R.string.last_watch)+" "+parseLastWatch(channel.getLastWatch());
                lastWatchText.setText(l);
            }
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(fragment.getContext(), ExoPlayerActivity.class);
                    intent.putExtra(Constants.ARGS_CHANNEL_ID,channel.getId());
                    fragment.getContext().startActivity(intent);
                }
            });
        }
        private String parseLastWatch(long lastWatch){
            long diff=new Date().getTime()-lastWatch;
            if (diff<60000){
                return getString(R.string.recently);
            }else if (diff<3600000){
                diff=diff/60000;
                return diff+" "+getString(R.string.minutes_ago);
            }else if (diff<86400000){
                diff=diff/3600000;
                return diff+" "+getString(R.string.hours_ago);
            }else {
                diff=diff/86400000;
                return diff+" "+getString(R.string.days_ago);
            }

        }

        private String getCategoryNameFromList(int id){
            for (Category category:categories){
                if (category.getId()==id){
                    return category.getName();
                }
            }
            return "";
        }
        private String getString(@StringRes int id){
            return fragment.getResources().getString(id);
        }
    }

    public void updateList(List<Channel> channelList,List<Category> categoryList){
        this.channels=channelList;
        this.categories=categoryList;
        this.notifyDataSetChanged();
        Log.d("new List size ", String.valueOf(getItemCount()));
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
}
