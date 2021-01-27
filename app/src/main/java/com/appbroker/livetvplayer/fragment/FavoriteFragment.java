package com.appbroker.livetvplayer.fragment;

import android.os.Bundle;
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
import com.appbroker.livetvplayer.util.Constants;

public class FavoriteFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView=view.findViewById(R.id.fragment_channel_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ChannelListRecyclerViewAdapter(this, Constants.CATEGORY_ID_FAV));
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
            ExportDialogFragment exportDialogFragment=new ExportDialogFragment();
            exportDialogFragment.show(getChildFragmentManager(),Constants.TAG_EXPORT_DIALOG_FRAGMENT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
