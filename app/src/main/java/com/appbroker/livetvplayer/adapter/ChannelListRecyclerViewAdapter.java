package com.appbroker.livetvplayer.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.appbroker.livetvplayer.ExoPlayerActivity;
import com.appbroker.livetvplayer.MainActivity;
import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChannelListRecyclerViewAdapter extends RecyclerView.Adapter {
    private Fragment fragment;
    private int categoryId;
    private List<Channel> channels;
    private CategoryViewModel categoryViewModel;
    private ChannelViewModel channelViewModel;

    public ChannelListRecyclerViewAdapter(Fragment fragment, int categoryId) {
        this.fragment=fragment;
        this.categoryId = categoryId;
        this.channels=new ArrayList<>();
        channelViewModel = new ViewModelProvider(fragment,ViewModelProvider.AndroidViewModelFactory.getInstance(fragment.getActivity().getApplication())).get(ChannelViewModel.class);
        channelViewModel.getAllOf(categoryId).observe(fragment, new Observer<List<Channel>>() {
            @Override
            public void onChanged(List<Channel> channels) {
                ChannelListRecyclerViewAdapter.this.channels=channels;
                ChannelListRecyclerViewAdapter.this.notifyDataSetChanged();
            }
        });
        categoryViewModel=new ViewModelProvider(fragment,ViewModelProvider.AndroidViewModelFactory.getInstance(fragment.getActivity().getApplication())).get(CategoryViewModel.class);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_channel,parent,false);
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
        private CardView cardView;
        private TextView nameTextView;
        private TextView subTextView;
        private ImageView favImage;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.fav_list_item_card);
            nameTextView=itemView.findViewById(R.id.fav_list_item_name);
            subTextView =itemView.findViewById(R.id.fav_list_item_sub_text);
            favImage=itemView.findViewById(R.id.fav_list_item_fav);
        }
        public void bind(int position){
            Channel channel=channels.get(position);
            nameTextView.setText(channel.getName());
            if (Constants.CATEGORY_ID_FAV==categoryId){
                categoryViewModel.getCategory(channel.getCategory_id()).observe(fragment, new Observer<Category>() {
                    @Override
                    public void onChanged(Category category) {
                        subTextView.setText(category.getName());
                    }
                });
            }else {
                subTextView.setText(channel.getUri().getPath());
            }
            favImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    channel.setFavorite(!channel.isFavorite());
                    channelViewModel.updateChannel(channel);
                }
            });
            if (channel.isFavorite()){
                favImage.setImageDrawable(ResourcesCompat.getDrawable(fragment.getResources(),R.drawable.ic_baseline_favorite_white_24,fragment.getContext().getTheme()));
            }else {
                favImage.setImageDrawable(ResourcesCompat.getDrawable(fragment.getResources(),R.drawable.ic_baseline_favorite_border_24,fragment.getContext().getTheme()));
            }

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(fragment.getContext(),ExoPlayerActivity.class);
                    intent.putExtra(Constants.ARGS_CHANNEL_ID,channel.getId());
                    fragment.getContext().startActivity(intent);
                }
            });
        }
    }

}
