package com.appbroker.livetvplayer.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.appbroker.livetvplayer.MainActivity;
import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;

import java.io.File;

public class DialogUtils {
    public interface CreateAddCategoryDialogInterface
    {
        void onCreateCategory(Category category);
    }

    public static AlertDialog createAddCategoryDialog(AppCompatActivity activity) {
        return createAddCategoryDialog(activity, null);
    }

    public static AlertDialog createAddCategoryDialog(AppCompatActivity activity, CreateAddCategoryDialogInterface createAddCategoryDialogInterface){
        CategoryViewModel categoryViewModel= new ViewModelProvider(activity,new ViewModelProvider.AndroidViewModelFactory(activity.getApplication())).get(CategoryViewModel.class);
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        View dialogView=View.inflate(activity, R.layout.dialog_add_category,null);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String s =((EditText)dialogView.findViewById(R.id.dialog_add_category_edit)).getText().toString();
                if (!s.equals("")){
                    Category category = new Category(s);
                    if (createAddCategoryDialogInterface != null)
                        createAddCategoryDialogInterface.onCreateCategory(category);
                    categoryViewModel.addCategory(category);
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
        builder.setTitle(R.string.add_category);
        return builder.create();
    }
    public static AlertDialog deleteChannelDialog(AppCompatActivity activity,int id){
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.delete_channel_are_you_sure);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ChannelViewModel channelViewModel=new ViewModelProvider(activity, ViewModelProvider.AndroidViewModelFactory.getInstance(activity.getApplication())).get(ChannelViewModel.class);
                channelViewModel.deleteChannel(id);
                ((MainActivity)activity).snackbar(activity.getString(R.string.channel_deleted),null,null);
                //todo:add undo delete
                dialog.dismiss();
            }
        });
        return builder.create();
    }
    public static void showEditChannelDialog(AppCompatActivity activity,int id){
        ChannelViewModel channelViewModel=new ViewModelProvider(activity, ViewModelProvider.AndroidViewModelFactory.getInstance(activity.getApplication())).get(ChannelViewModel.class);
        LiveData<Channel> channelLiveData=channelViewModel.getChannelById(id,false);
        Observer<Channel> observer=new Observer<Channel>() {
            @Override
            public void onChanged(Channel channel) {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity);
                builder.setTitle(R.string.edit_channel);
                View view=View.inflate(activity,R.layout.dialog_edit_channel,null);
                EditText name_edit=view.findViewById(R.id.dialog_edit_channel_name);
                EditText url_edit=view.findViewById(R.id.dialog_edit_channel_url);
                name_edit.setText(channel.getName());
                url_edit.setText(UriTypeConverter.toString(channel.getUri()));
                builder.setView(view);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        channel.setName(name_edit.getText().toString());
                        channel.setUri(StringUtils.makeUri(url_edit.getText().toString()));
                        channelViewModel.updateChannel(channel);
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
                channelLiveData.removeObserver(this);
            }
        };
        channelLiveData.observe(activity, observer);
    }
    public static void showShareDialog(Context context,File file){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle(R.string.share_playlist);
        builder.setMessage(R.string.want_to_share);
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("application/x-mpegurl");
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "com.appbroker.livetvplayer.provider", file));
                context.startActivity(Intent.createChooser(intent,context.getString(R.string.share_playlist)));
            }
        });
        builder.create().show();
    }

    public static void showLeaveApplicationWarningDialog(Context context, DialogInterface.OnClickListener onConfirmListener){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle(R.string.leave);
        builder.setMessage(R.string.about_to_leave);
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.yes,onConfirmListener);
        builder.create().show();
    }
}
