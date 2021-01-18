package com.appbroker.livetvplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryListSpinnerAdapter extends BaseAdapter {
    private final Fragment fragment;
    private List<Category> categoryList;

    public CategoryListSpinnerAdapter(Fragment fragment) {
        this.fragment=fragment;
        this.categoryList=new ArrayList<>();
        CategoryViewModel categoryViewModel = new ViewModelProvider(fragment, new ViewModelProvider.AndroidViewModelFactory(fragment.getActivity().getApplication())).get(CategoryViewModel.class);
        categoryViewModel.getAllCategories().observe(fragment, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                CategoryListSpinnerAdapter.this.categoryList=categories;
                CategoryListSpinnerAdapter.this.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return categoryList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater=fragment.getLayoutInflater();
        CheckedTextView checkedTextView= (CheckedTextView) layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item,parent,false);
        checkedTextView.setText(categoryList.get(position).getName());
        return checkedTextView;
    }

}
