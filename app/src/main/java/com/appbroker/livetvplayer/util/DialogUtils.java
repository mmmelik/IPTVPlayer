package com.appbroker.livetvplayer.util;

import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;

public class DialogUtils {
    public static AlertDialog createAddCategoryDialog(AppCompatActivity activity){
        CategoryViewModel categoryViewModel= new ViewModelProvider(activity,new ViewModelProvider.AndroidViewModelFactory(activity.getApplication())).get(CategoryViewModel.class);
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        View dialogView=View.inflate(activity, R.layout.dialog_add_category,null);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String s =((EditText)dialogView.findViewById(R.id.dialog_add_category_edit)).getText().toString();
                if (!s.equals("")){
                    categoryViewModel.addCategory(new Category(s));
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
