package com.appbroker.livetvplayer.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.adapter.ChannelListRecyclerViewAdapter;
import com.appbroker.livetvplayer.listener.ParserListener;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.DialogUtils;
import com.appbroker.livetvplayer.util.Enums;
import com.appbroker.livetvplayer.util.M3UParser;

import java.io.File;
import java.util.List;
import java.util.Random;

public class FavoriteFragment extends Fragment {
    ChannelListRecyclerViewAdapter channelListRecyclerViewAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView=view.findViewById(R.id.fragment_channel_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        channelListRecyclerViewAdapter=new ChannelListRecyclerViewAdapter(this, Constants.CATEGORY_ID_FAV);
        recyclerView.setAdapter(channelListRecyclerViewAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channel_list,container,false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_favorite_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.fragment_favorite_export_action){
            M3UParser m3UParser=new M3UParser(getContext());
            File dir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+File.separator+"MicroPlaylists");
            if (!dir.exists()){
                dir.mkdir();
            }
            m3UParser.generateM3UPlaylist("favorites_" + new Random().nextInt(100), channelListRecyclerViewAdapter.getChannels(), dir, new ParserListener() {
                @Override
                public void onFinish(Enums.ParseResult parseResult, List<Channel> channelList, String message) {
                    //empty
                }

                @Override
                public void onCreateFile(File f) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.showShareDialog(getActivity(),f);
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    //todo:show
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
