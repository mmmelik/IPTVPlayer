package com.appbroker.livetvplayer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.listener.FileSelectListener;
import com.appbroker.livetvplayer.model.CustomFile;

import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilePickerDirListRecyclerAdapter extends RecyclerView.Adapter {
    private List<File> folders;
    private List<File> files;
    private List<CustomFile> customFiles;
    private FileSelectListener fileSelectListener;
    private Context context;

    public FilePickerDirListRecyclerAdapter(Context context,File parentFile, File[] fileList, FileSelectListener fileSelectListener) {
        this.context=context;
        this.customFiles = new ArrayList<>();
        CustomFile p=new CustomFile(parentFile,true);
        this.customFiles.add(p);
        this.files = new ArrayList<>();
        this.folders = new ArrayList<>();
        if (fileList==null){
            fileList=new File[]{};
        }
        Arrays.sort(fileList,NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
        for (File file:fileList){
            if (file.isHidden()){
                continue;
            }
            if (file.isDirectory()){
                folders.add(file);
            }else {
                files.add(file);
            }
        }
        customFiles.addAll(CustomFile.customFiles(folders));
        customFiles.addAll(CustomFile.customFiles(files));
        files=null;
        folders=null;
        this.fileSelectListener = fileSelectListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.file_picker_dir_list_item,parent,false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((CustomViewHolder)holder).bind(position);
    }

    @Override
    public int getItemCount() {
        return customFiles.size();
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView name;
        private TextView size;
        private TextView last_modified;
        private LinearLayout sizeContainer;
        private RelativeLayout relativeLayout;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            icon=itemView.findViewById(R.id.file_picker_list_item_file_icon);
            name=itemView.findViewById(R.id.file_picker_list_item_file_name);
            size=itemView.findViewById(R.id.file_picker_list_item_file_size);
            last_modified=itemView.findViewById(R.id.file_picker_list_item_file_last_modified);
            sizeContainer=itemView.findViewById(R.id.file_picker_list_item_size_container);
            relativeLayout=itemView.findViewById(R.id.file_picker_list_item_container);
        }
        public void bind(int position){
            CustomFile customFile=customFiles.get(position);
            if (customFile.isParentPlaceHolder()){
                name.setText("...");
            }else {
                name.setText(customFile.getName());
            }

            size.setText(customFile.getSize());
            String s=context.getResources().getString(R.string.last_edited)+customFile.getLastModified();
            last_modified.setText(s);
            if (customFile.isDirectory()){
                sizeContainer.setVisibility(View.GONE);
                icon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_folder_48,context.getTheme()));
            }else {
                sizeContainer.setVisibility(View.VISIBLE);
                icon.setImageDrawable(getDrawable(customFile));
            }
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileSelectListener.onFileSelect(customFile);
                }
            });
        }
        private Drawable getDrawable(CustomFile customFile){
            //TODO:Düzgün iconlar ekle.
            if ("m3u".equals(customFile.getExtension())){
                return ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_playlist_play_48,context.getTheme());
            }else if ("m3u8".equals(customFile.getExtension())){
                return ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_local_movies_48,context.getTheme());
            }else if ("pdf".equals(customFile.getExtension())){
                return ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_picture_as_pdf_48,context.getTheme());
            }else if ("zip".equals(customFile.getExtension())||"rar".equals(customFile.getExtension())){
                return ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_archive_48,context.getTheme());
            }else {
                if (customFile.getMime()!=null){
                    if (customFile.getMime().contains("video")){
                        return ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_local_movies_48,context.getTheme());
                    }else if (customFile.getMime().contains("audio")){
                        return ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_audiotrack_48,context.getTheme());
                    }else if (customFile.getMime().contains("image")){
                        return ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_image_48,context.getTheme());
                    }else {
                        return ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_insert_drive_file_48,context.getTheme());
                    }
                }else {
                    return ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_baseline_insert_drive_file_48,context.getTheme());
                }

            }
        }
    }
}
