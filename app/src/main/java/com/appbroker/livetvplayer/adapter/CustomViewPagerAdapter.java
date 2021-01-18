package com.appbroker.livetvplayer.adapter;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.appbroker.livetvplayer.fragment.ChannelListFragment;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class CustomViewPagerAdapter extends FragmentStateAdapter {
    private CategoryViewModel categoryViewModel;
    private List<Category> categoryList;

    public CustomViewPagerAdapter(@NonNull Fragment fragment, Application application) {
        super(fragment);
        this.categoryList = new ArrayList<Category>();
        categoryViewModel = new ViewModelProvider(fragment, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(CategoryViewModel.class);
        categoryViewModel.getAllCategories().observe(fragment, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                CustomViewPagerAdapter.this.categoryList = categories;
                CustomViewPagerAdapter.this.notifyDataSetChanged();
            }
        });
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ChannelListFragment channelListFragment = new ChannelListFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARGS_CATEGORY_ID, categoryList.get(position).getId());
        channelListFragment.setArguments(args);
        return channelListFragment;
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public Category getCategoryAt(int position) {
        return categoryList.get(position);
    }
}
