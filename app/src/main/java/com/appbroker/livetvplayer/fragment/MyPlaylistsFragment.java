package com.appbroker.livetvplayer.fragment;

import android.Manifest;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.appbroker.livetvplayer.CustomFilePickerActivity;
import com.appbroker.livetvplayer.MainActivity;
import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.adapter.CustomViewPagerAdapter;
import com.appbroker.livetvplayer.listener.DataBaseJobListener;
import com.appbroker.livetvplayer.listener.ParserListener;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.DialogUtils;
import com.appbroker.livetvplayer.util.Enums;
import com.appbroker.livetvplayer.util.M3UParser;
import com.appbroker.livetvplayer.util.ThemeUtil;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class MyPlaylistsFragment extends Fragment{
    private CategoryViewModel categoryViewModel;
    private ChannelViewModel channelViewModel;
    private SpeedDialView speedDialView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageView addCategory;
    private CustomViewPagerAdapter customViewPagerAdapter;
    private SearchView searchView;

    public MyPlaylistsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryViewModel=new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(CategoryViewModel.class);
        channelViewModel=new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(ChannelViewModel.class);
        setHasOptionsMenu(true);

    }

    private void checkIntent() {
        Intent intent=getActivity().getIntent();
        if (intent!=null&&intent.getAction().equals(Intent.ACTION_VIEW)){
            String type=intent.getType();
            Log.i("intent_type",type);
            Uri data=intent.getData();
            Log.i("path",data.toString());
            handleFileSelect(data.toString(),!URLUtil.isNetworkUrl(data.toString()));
        }
        getActivity().setIntent(null);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_playlists,container,false);
    }

    public boolean isFABOpen(){
        return speedDialView.isOpen();
    }
    public void closeFAB(){
        if (speedDialView.isOpen()){
            speedDialView.close(true);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tabLayout=view.findViewById(R.id.fragment_playlist_tab_layout);
        viewPager=view.findViewById(R.id.fragment_playlist_view_pager);
        addCategory=view.findViewById(R.id.fragment_playlist_add_category);

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.createAddCategoryDialog((MainActivity)getActivity()).show();
            }
        });
        customViewPagerAdapter=new CustomViewPagerAdapter(this,getActivity().getApplication());
        viewPager.setAdapter(customViewPagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, true, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(customViewPagerAdapter.getCategoryAt(position).getName());
            }
        }).attach();


        speedDialView=view.findViewById(R.id.fragment_playlist_speedDialView);
        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_add_playlist_local,R.drawable.ic_baseline_create_new_folder_white_24)
                .setLabel(R.string.load_local_playlist)
                .setFabBackgroundColor(ThemeUtil.getColorFromAttr(getContext(), R.attr.cardBackgroundColor))
                .setFabImageTintColor(ThemeUtil.getColorFromAttr(getContext(),android.R.attr.textColor))
                .create());

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_add_playlist_url,R.drawable.ic_baseline_link_white_24)
                .setLabel(R.string.load_remote_playlist)
                .setFabBackgroundColor(ThemeUtil.getColorFromAttr(getContext(), R.attr.cardBackgroundColor))
                .setFabImageTintColor(ThemeUtil.getColorFromAttr(getContext(),android.R.attr.textColor))
                .create());

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_export_playlist,R.drawable.ic_baseline_export_24)
                .setLabel(R.string.export_playlist)
                .setFabBackgroundColor(ThemeUtil.getColorFromAttr(getContext(), R.attr.cardBackgroundColor))
                .setFabImageTintColor(ThemeUtil.getColorFromAttr(getContext(),android.R.attr.textColor))
                .create());
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                if(actionItem.getId()==R.id.fab_add_playlist_url){
                    launchURLDialog();
                }else if (actionItem.getId()==R.id.fab_add_playlist_local){
                    launchFileChooserFlow();
                }else if(actionItem.getId()==R.id.fab_export_playlist){
                    exportPlaylistFlow();
                }
                speedDialView.close();
                return true;
            }
        });
        //todo:intercept ontouch and close fab.
        super.onViewCreated(view, savedInstanceState);
        checkIntent();
    }

    private void exportPlaylistFlow() {
        ExportDialogFragment exportDialogFragment=new ExportDialogFragment();
        exportDialogFragment.show(getChildFragmentManager(),Constants.TAG_EXPORT_DIALOG_FRAGMENT);
    }

    private void launchURLDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        View view=View.inflate(getContext(),R.layout.dialog_load_url,null);
        EditText editText=view.findViewById(R.id.dialog_load_url_edit);
        ImageView imageView=view.findViewById(R.id.dialog_load_url_clipboard);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=getClipboard();
                try {
                    new URL(s);
                    editText.setText(s);
                }catch (MalformedURLException e){
                    //do nothing.
                }
            }
        });
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleFileSelect(editText.getText().toString(),false);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private String getClipboard(){
        ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip;
        if ((clip=clipboard.getPrimaryClip()) != null) {
            return clip.getItemAt(0).getText().toString();
        }
        return "";
    }
    private void launchFileChooserFlow() {
        PermissionGen
                .with(this)
                .addRequestCode(100)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request();
    }


    @Keep
    @PermissionSuccess(requestCode = 100)
    private void onStorageRequestSuccess(){

        //Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        //String[] mimeTypes={"audio/*","video/*","application/vnd.apple.mpegurl"};
        //intent.setType("*/*");
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        //    intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        //}else {
        //    intent.setType("video/*|audio/*|application/*");
        //}
        //startActivityForResult(intent,Constants.REQUEST_CODE_PICK_FILE);

        /*Intent intent=new Intent(getContext(),FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
        intent.putExtra(FilePickerActivity.EXTRA_START_PATH, getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath());
        startActivityForResult(intent,Constants.REQUEST_CODE_PICK_FILE);*/

        Intent intent=new Intent(getContext(), CustomFilePickerActivity.class);
        startActivityForResult(intent,Constants.REQUEST_CODE_PICK_FILE);
    }
    @Keep
    @PermissionFail(requestCode = 100)
    private void onStorageRequestFail(){
        ((MainActivity)getActivity()).snackbar(getResources().getString(R.string.storage_permission_required), getString(R.string.grant), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionGen.with(getActivity())
                        .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .request();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this,requestCode,permissions,grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==Constants.REQUEST_CODE_PICK_FILE){
            if (resultCode==RESULT_OK){
                Uri uri=data.getData();
                if (uri!=null){
                    Log.d("pick",uri.toString());
                    handleFileSelect(uri.toString(),true);
                }else {
                    ((MainActivity)getActivity()).snackbar(getResources().getString(R.string.unknown_error),null,null);
                }
            }else if (requestCode==RESULT_CANCELED){
                ((MainActivity)getActivity()).snackbar(getResources().getString(R.string.file_pick_canceled),null,null);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void handleFileSelect(String uri,boolean isLocal){
        Log.d("isLocal", String.valueOf(isLocal));
        M3UParser m3UParser=new M3UParser(getContext(), new ParserListener() {
            @Override
            public void onFinish(Enums.ParseResult parseResult, List<Channel> channelList, String message) {
                if (parseResult == Enums.ParseResult.SUCCESS){
                    callAddChannelDialog(channelList);

                }else if (parseResult== Enums.ParseResult.REQUIRE_NAME){
                    //TODO:ask channel name
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    View view=View.inflate(getContext(),R.layout.dialog_add_channel_name,null);
                    EditText editText=view.findViewById(R.id.dialog_add_channel_name_edit);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            channelList.get(0).setName(editText.getText().toString());
                            callAddChannelDialog(channelList);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setCancelable(false);
                    builder.setView(view);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.create().show();
                        }
                    });
                }else {
                    Log.d("error",parseResult.name());
                }
            }

            @Override
            public void onCreateFile(File f) {
                //empty
            }

            @Override
            public void onError(Exception e) {
                Log.d("error",e.getMessage());
            }
        });
        if (isLocal){
            m3UParser.parseLocal(uri);
        }else {
            m3UParser.parseFromURL(uri);
        }

    }
    private void callAddChannelDialog(List<Channel> channelList){
        channelViewModel.addMultipleChannels(channelList, new DataBaseJobListener() {
            @Override
            public void onStart() {
                ((MainActivity)getActivity()).setLoading(true);
            }

            @Override
            public void onFinish(@Nullable Channel channel) {
                ((MainActivity)getActivity()).setLoading(false);
                ChannelAddBottomSheetDialogFragment channelAddBottomSheetDialogFragment=new ChannelAddBottomSheetDialogFragment();
                Bundle args=new Bundle();
                try {
                    args.putLong(Constants.ARGS_CATEGORY_ID,customViewPagerAdapter.getCategoryAt(viewPager.getCurrentItem()).getId());
                }catch (Exception e){
                    e.printStackTrace();
                }

                channelAddBottomSheetDialogFragment.setArguments(args);
                channelAddBottomSheetDialogFragment.show(getFragmentManager(),"bottom_sheet_fragment");
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.fragment_myplaylist_action_menu,menu);
        MenuItem searchItem=menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).drawerLayout.closeDrawers();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("onQueryTextSubmit", query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    Log.i("onQueryTextChange", newText);
                    if (getActivity()!=null){
                        if ("".equals(newText)){
                            ((MainActivity)getActivity()).searchFragmentChangeState(false,null);
                        }else {
                            ((MainActivity)getActivity()).searchFragmentChangeState(true, newText);
                        }
                    }
                    return true;
                }catch (Exception e){
                    e.printStackTrace();
                    return true;
                }
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d("search","expand");
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
    }

    @Override
    public void onPause() {
        Log.d("fragment","pause");
        ((MainActivity)getActivity()).detachSearchFragment();
        super.onPause();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int channelId=item.getGroupId();
        Log.d("channel", String.valueOf(channelId));
        if (item.getItemId()==R.id.context_menu_edit){
            Log.d("channel","edit");
            DialogUtils.showEditChannelDialog((MainActivity)getActivity(),channelId);
        }else if(item.getItemId()==R.id.context_menu_delete){
            Log.d("channel","delete");
            DialogUtils.deleteChannelDialog((MainActivity)getActivity(),channelId).show();
        }
        return super.onContextItemSelected(item);
    }
}
