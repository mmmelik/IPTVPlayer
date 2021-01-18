package com.appbroker.livetvplayer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.adapter.ChannelListRecyclerViewAdapter;
import com.appbroker.livetvplayer.util.Constants;

public class ChannelListFragment extends Fragment {
    private RecyclerView channelRecyclerView;
    private TextView textView;
    private int categoryId;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args=getArguments();
        categoryId=args.getInt(Constants.ARGS_CATEGORY_ID,-1);

        channelRecyclerView=view.findViewById(R.id.fragment_channel_recycler_view);
        channelRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        channelRecyclerView.setAdapter(new ChannelListRecyclerViewAdapter(this,categoryId));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel_list,container,false);
    }

}
