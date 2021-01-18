package com.appbroker.livetvplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.appbroker.livetvplayer.db.CategoryService;
import com.appbroker.livetvplayer.model.Category;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {
    private CategoryService categoryService;
    private LiveData<List<Category>> allCategories;
    private LiveData<Category> category;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        categoryService=new CategoryService(application);

    }

    public void addCategory(Category category){
        categoryService.addCategory(category);
    }

    public void updateCategory(Category category){
        categoryService.updateCategory(category);
    }

    public void deleteCategory(Category category){
        categoryService.deleteCategory(category);
    }

    public LiveData<List<Category>> getAllCategories(){
        return categoryService.getAllCategories();
    }

    public LiveData<Category> getCategory(int id) {
        return categoryService.getCategory(id);
    }
}
