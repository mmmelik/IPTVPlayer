package com.appbroker.livetvplayer.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.appbroker.livetvplayer.MainActivity;
import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.adapter.CategoryListSpinnerAdapter;
import com.appbroker.livetvplayer.listener.ParserListener;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.DialogUtils;
import com.appbroker.livetvplayer.util.Enums;
import com.appbroker.livetvplayer.util.M3UParser;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;

import java.io.File;
import java.util.List;
import java.util.Random;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

public class ExportDialogFragment extends DialogFragment {
    private CategoryViewModel categoryViewModel;
    private ChannelViewModel channelViewModel;

    private AppCompatSpinner appCompatSpinner;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        PermissionGen.with(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .addRequestCode(101)
                .request();

        AppCompatActivity activity= (AppCompatActivity) context;
        channelViewModel=new ViewModelProvider(activity, ViewModelProvider.AndroidViewModelFactory.getInstance(activity.getApplication())).get(ChannelViewModel.class);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        channelViewModel=null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView path = view.findViewById(R.id.fragment_export_dialog_path_text);
        appCompatSpinner = view.findViewById(R.id.fragment_export_dialog_category_spinner);
        appCompatSpinner.setAdapter(new CategoryListSpinnerAdapter(getParentFragment()));

        File dir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+File.separator+"MicroPlaylists");
        if (!dir.exists()){
            dir.mkdir();
        }
        path.setText(dir.getPath());

        Button button=view.findViewById(R.id.fragment_export_dialog_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int catId=((Category)appCompatSpinner.getSelectedItem()).getId();
                String catName=((Category)appCompatSpinner.getSelectedItem()).getName();
                Observer<List<Channel>> observer=new Observer<List<Channel>>() {
                    @Override
                    public void onChanged(List<Channel> channels) {
                        M3UParser m3UParser=new M3UParser(getContext());
                        m3UParser.generateM3UPlaylist(catName + "_" + new Random().nextInt(100), channels, dir, new ParserListener() {
                            @Override
                            public void onFinish(Enums.ParseResult parseResult, List<Channel> channelList, String message) {
                                //dont fill
                            }

                            @Override
                            public void onCreateFile(File f) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogUtils.showShareDialog(getActivity(),f);
                                    }
                                });
                                dismiss();
                            }

                            @Override
                            public void onError(Exception e) {
                                dismiss();
                            }
                        });
                        channelViewModel.getAllOf(catId).removeObservers(ExportDialogFragment.this);
                    }
                };
                channelViewModel.getAllOf(catId).observe(ExportDialogFragment.this,observer);
            }
        });

    }
    @PermissionSuccess(requestCode = 101)
    public void  onSuccessRead(){
        PermissionGen.with(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .addRequestCode(102)
                .request();
    }

    @PermissionFail(requestCode = 101)
    public void onFailReadPermission(){
        final MainActivity activity= (MainActivity) getActivity();
        ((MainActivity)getActivity()).snackbar(getString(R.string.storage_permission_required), getString(R.string.grant), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionGen.with(activity)
                        .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .addRequestCode(101)
                        .request();
            }
        });
        dismiss();
    }

    @PermissionFail(requestCode = 102)
    public void onFailWritePermission(){
        final MainActivity activity= (MainActivity) getActivity();
        ((MainActivity)getActivity()).snackbar(getString(R.string.storage_permission_required), getString(R.string.grant), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionGen.with(activity)
                        .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .addRequestCode(102)
                        .request();
            }
        });
        dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this,requestCode,permissions,grantResults);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_export_dialog,container);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog=getDialog();
        if (dialog!=null){
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

}
