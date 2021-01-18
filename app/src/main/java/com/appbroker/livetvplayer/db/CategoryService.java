package com.appbroker.livetvplayer.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.appbroker.livetvplayer.model.Category;

import java.util.List;

public class CategoryService {
    private CategoryDAO categoryDAO;
    private LiveData<List<Category>> allCategories;
    private LiveData<Category> category;

    public CategoryService(Application application){
        LiveTVDatabase liveTVDatabase=LiveTVDatabase.getInstance(application);
        categoryDAO=liveTVDatabase.categoryDAO();
    }

    public LiveData<List<Category>> getAllCategories(){
        return categoryDAO.getAll();
    }

    public LiveData<Category> getCategory(int id){
        return categoryDAO.getCategoryById(id);
    }

    public void addCategory(Category category){
        new Thread() {
            @Override
            public void run() {
                categoryDAO.addCategory(category);
            }
        }.start();
    }
    public void updateCategory(Category category){
        new Thread() {
            @Override
            public void run() {
                categoryDAO.updateCategory(category);
            }
        }.start();
    }
    public void deleteCategory(Category category){
        new Thread() {
            @Override
            public void run() {
                categoryDAO.deleteCategory(category);
            }
        }.start();
    }
}
