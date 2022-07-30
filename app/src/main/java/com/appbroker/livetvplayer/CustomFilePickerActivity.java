package com.appbroker.livetvplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.appbroker.livetvplayer.adapter.FilePickerDirListRecyclerAdapter;
import com.appbroker.livetvplayer.listener.FileSelectListener;
import com.appbroker.livetvplayer.model.CustomFile;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.PrefHelper;
import com.appbroker.livetvplayer.util.ThemeUtil;
import com.google.android.gms.common.api.Result;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class CustomFilePickerActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView dirRecyclerView;
    private TextView pathView;
    private ImageButton backImage;

    private File downloadDir;
    private File videoDir;
    private File audioDir;
    private File sdDir;
    private File internalDir;
    private File currentFolder;
    private File currentRoot;

    private HashMap<Integer,ResolveInfo> externalAppsIdMap;
    private PrefHelper prefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefHelper=new PrefHelper(CustomFilePickerActivity.this);
        setTheme(ThemeUtil.getPrefTheme(prefHelper));

        setContentView(R.layout.activity_custom_file_picker);
        Toolbar toolbar = findViewById(R.id.file_picker_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        backImage=findViewById(R.id.file_picker_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goUp();
            }
        });
        pathView=findViewById(R.id.file_picker_file_path);
        dirRecyclerView=findViewById(R.id.file_picker_recyclerview);
        drawerLayout=findViewById(R.id.file_picker_drawer_layout);
        navigationView=findViewById(R.id.file_picker_navigation_view);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                if (id==R.id.file_picker_navigation_internal_storage){
                    currentRoot=internalDir;
                    goTo(internalDir);
                }else if (id==R.id.file_picker_navigation_sd_storage){
                    if (item.isEnabled()){
                        currentRoot=sdDir;
                        goTo(sdDir);
                    }else {
                        Toast.makeText(CustomFilePickerActivity.this,R.string.sd_card_not_available,Toast.LENGTH_SHORT).show();
                    }
                }else if (id==R.id.file_picker_navigation_downloads){
                    currentRoot=downloadDir;
                    goTo(downloadDir);
                }else if (id==R.id.file_picker_navigation_video){
                    currentRoot=videoDir;
                    goTo(videoDir);
                }else if (id==R.id.file_picker_navigation_audio){
                    currentRoot=audioDir;
                    goTo(audioDir);
                }else {
                    if (externalAppsIdMap.containsKey(id)){
                        ResolveInfo resolveInfo=externalAppsIdMap.get(id);
                        Intent intent=getFilePickerIntent();
                        intent.setClassName(resolveInfo.activityInfo.packageName,resolveInfo.activityInfo.name);
                        startActivityForResult(intent,Constants.REQUEST_CODE_PICK_FILE);

                    }
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
        getDirs();
        drawerLayout.openDrawer(Gravity.LEFT);
        dirRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationView.getMenu().findItem(R.id.file_picker_navigation_downloads).setChecked(true);
        goTo(downloadDir);
        currentRoot=downloadDir;
        addExternalApps();
    }

    public void getDirs() {
        downloadDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        audioDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        videoDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        internalDir=Environment.getExternalStorageDirectory();
        try {
            sdDir=new File(Objects.requireNonNull(System.getenv("SECONDARY_STORAGE")));
        }catch (NullPointerException e){
            e.printStackTrace();
            try {
                sdDir=new File(Objects.requireNonNull(System.getenv("EXTERNAL_STORAGE")));
            }catch (NullPointerException ne){
                ne.printStackTrace();
            }
        }
        navigationView.getMenu().findItem(R.id.file_picker_navigation_sd_storage).setEnabled(sdDir!=null);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)){
            drawerLayout.closeDrawer(Gravity.LEFT);
        }else {
            if (isRoot()){
                super.onBackPressed();
            }else {
                goUp();
            }
        }
    }

    private void goTo(File file){
        FileFilter filenameFilter = pathname -> {
            if (pathname.isDirectory())
                return true;

            String lowerName = pathname.getName().toLowerCase(Locale.US);
            return lowerName.endsWith(".m3u8") || lowerName.endsWith(".m3u");
        };

        dirRecyclerView.setAdapter(new FilePickerDirListRecyclerAdapter(this,file.getParentFile(),file.listFiles(filenameFilter), new FileSelectListener() {
            @Override
            public void onFileSelect(CustomFile customFile) {
                if(customFile.isParentPlaceHolder() && customFile.getFile().getParent()==null){
                    Toast.makeText(getApplicationContext(),R.string.root_directory,Toast.LENGTH_SHORT).show();

                    return;
                }
                if (customFile.isDirectory()){
                    goTo(customFile.getFile());
                }else {
                    Intent intent=new Intent();
                    intent.setData(Uri.fromFile(customFile.getFile()));
                    setResult(RESULT_OK,intent);
                    CustomFilePickerActivity.this.finish();
                }

            }
        }));
        pathView.setText(file.getAbsolutePath());
        currentFolder=file;
        try {
            getSupportActionBar().setTitle(file.getName());
        }catch (NullPointerException e){

        }

    }

    private void goUp(){
        if (!isRoot()){
            Log.d("test",currentFolder.getAbsolutePath()+"\n"+currentRoot.getAbsolutePath());
            File parent=currentFolder.getParentFile();
            assert parent != null;
            goTo(parent);
        }
    }

    private boolean isRoot(){
        return currentFolder.getParentFile() == null || currentFolder.getAbsolutePath().equals(currentRoot.getAbsolutePath());
    }

    private void addExternalApps(){
        PackageManager packageManager=getPackageManager();
        List<ResolveInfo> resolveInfoList=packageManager.queryIntentActivities(getFilePickerIntent(), 0);
        Menu menu=navigationView.getMenu().findItem(R.id.file_picker_navigation_item_external_apps).getSubMenu();
        externalAppsIdMap=new HashMap<>();
        for (ResolveInfo resolveInfo:resolveInfoList){
            Log.d("activity",resolveInfo.toString());
            int id=new Random().nextInt(Integer.MAX_VALUE);
            menu.add(R.id.file_picker_navigation_group_external_apps,id,Menu.NONE,resolveInfo.loadLabel(packageManager));
            menu.findItem(id).setIcon(resolveInfo.loadIcon(packageManager));
            externalAppsIdMap.put(id,resolveInfo);
        }
        navigationView.setItemIconTintList(null);
    }

    private Intent getFilePickerIntent(){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/vnd.apple.mpegurl", "application/mpegurl", "application/x-mpegurl", "audio/mpegurl", "audio/x-mpegurl"};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        }else {
            intent.setType("application/vnd.apple.mpegurl|application/mpegurl|application/x-mpegurl|audio/mpegurl|audio/x-mpegurl");
        }
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==Constants.REQUEST_CODE_PICK_FILE){
            if (resultCode==RESULT_OK){
                CustomFilePickerActivity.this.setResult(RESULT_OK,data);
                CustomFilePickerActivity.this.finish();
            }else {
                Toast.makeText(CustomFilePickerActivity.this,R.string.unknown_error,Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}