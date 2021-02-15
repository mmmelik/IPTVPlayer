package com.appbroker.livetvplayer.fragment;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.adapter.SearchListRecyclerAdapter;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private TextView searchResults;
    private RecyclerView recyclerView;

    private Application application;

    private ChannelViewModel channelViewModel;
    private CategoryViewModel categoryViewModel;

    private SearchListRecyclerAdapter searchListRecyclerAdapter;
    private List<Channel> channels;
    private List<Category> categories;
    private String lastQuery;

    public SearchFragment(Application application) {
        channels=new ArrayList<>();
        categories=new ArrayList<>();
        searchListRecyclerAdapter=new SearchListRecyclerAdapter(this);
        this.application=application;
    }

    public SearchFragment() {
        channels=new ArrayList<>();
        searchListRecyclerAdapter=new SearchListRecyclerAdapter(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (channelViewModel==null){
            channelViewModel = new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(ChannelViewModel.class);
            channelViewModel.getAllChannels().observe(this, new Observer<List<Channel>>() {
                @Override
                public void onChanged(List<Channel> channels) {
                    SearchFragment.this.channels=channels;
                    if (lastQuery!=null){
                        updateQuery(lastQuery);
                    }
                }
            });
        }
        if (categoryViewModel==null){
            categoryViewModel=new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(CategoryViewModel.class);
            categoryViewModel.getAllCategories().observe(this, new Observer<List<Category>>() {
                @Override
                public void onChanged(List<Category> categories) {
                    SearchFragment.this.categories=categories;
                    if (searchListRecyclerAdapter!=null){
                        searchListRecyclerAdapter.setCategories(categories);
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView=view.findViewById(R.id.fragment_search_recycler_view);
        searchResults=view.findViewById(R.id.fragment_search_search_results);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(searchListRecyclerAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        super.onViewCreated(view, savedInstanceState);
    }

    public void updateQuery(String query){
        //todo:searh channels in database
        lastQuery=query;
        if (searchListRecyclerAdapter!=null){
            List<Channel> list=filterList(query);
            if (list.size()==0){
                searchResults.setText(R.string.no_result);
            }else {
                searchResults.setText(R.string.search_results);
            }
            searchListRecyclerAdapter.updateList(list,categories);
        }else {
            Log.d("adapter null",query);
        }
    }

    private List<Channel> filterList(String query){
        if (query.equals("")){
            return new ArrayList<>();
        }

        List<Channel> channelList=new ArrayList<>();
        for (Channel channel:channels){
            if (channel.getName().toLowerCase().contains(query.toLowerCase())){
                channelList.add(channel);
            }
        }
        return channelList;
    }
}
