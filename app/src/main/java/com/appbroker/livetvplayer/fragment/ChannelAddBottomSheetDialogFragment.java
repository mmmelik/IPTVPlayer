package com.appbroker.livetvplayer.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appbroker.livetvplayer.MainActivity;
import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.adapter.CategoryListSpinnerAdapter;
import com.appbroker.livetvplayer.adapter.ChannelAddRecyclerViewAdapter;
import com.appbroker.livetvplayer.listener.ChannelListListener;
import com.appbroker.livetvplayer.model.Category;
import com.appbroker.livetvplayer.model.Channel;
import com.appbroker.livetvplayer.util.Constants;
import com.appbroker.livetvplayer.util.DialogUtils;
import com.appbroker.livetvplayer.util.ThemeUtil;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class ChannelAddBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private ChannelViewModel channelViewModel;
    private CategoryViewModel categoryViewModel;
    private Button selectAllButton;
    private ImageView addCategoryButton;
    private long selectedCategoryId;
    private int maxId;

    public ChannelAddBottomSheetDialogFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelViewModel= new ViewModelProvider(ChannelAddBottomSheetDialogFragment.this,new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(ChannelViewModel.class);
        this.categoryViewModel= new ViewModelProvider(ChannelAddBottomSheetDialogFragment.this,new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(CategoryViewModel.class);
        this.selectedCategoryId = getArguments().getLong(Constants.ARGS_CATEGORY_ID,-1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.channel_add_bottom_dialog,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        selectAllButton=view.findViewById(R.id.bottom_sheet_select_all_button);
        RecyclerView recyclerView=view.findViewById(R.id.bottom_sheet_recycler_view);
        Spinner spinner=view.findViewById(R.id.bottom_sheet_category_spinner);
        addCategoryButton=view.findViewById(R.id.bottom_sheet_add_category_image);
        Button addChannelsButton=view.findViewById(R.id.bottom_sheet_add_channels_button);

        CategoryListSpinnerAdapter categoryListSpinnerAdapter=new CategoryListSpinnerAdapter(ChannelAddBottomSheetDialogFragment.this);
        spinner.setAdapter(categoryListSpinnerAdapter);
        spinner.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                CategoryListSpinnerAdapter adapter = ((CategoryListSpinnerAdapter)spinner.getAdapter());
                int pos = adapter.getCategoryPosFromId((int)selectedCategoryId);
                if (pos >= 0)
                    spinner.setSelection(pos);

                for(int i = 0; i < adapter.getCount(); i++) {
                    Category c = (Category) adapter.getItem(i);
                    if (c.getId() >= maxId)
                        maxId = c.getId();
                }
            }
        });

        ChannelAddRecyclerViewAdapter channelAddRecyclerViewAdapter=new ChannelAddRecyclerViewAdapter(ChannelAddBottomSheetDialogFragment.this);
        channelAddRecyclerViewAdapter.setChannelListListener(new ChannelListListener() {
            @Override
            public void update(List<Channel> channels) {
                toggleSelectAllButton(isAllChecked(channels));
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(channelAddRecyclerViewAdapter);

        addChannelsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                channelViewModel.registerTempChannels((int) spinner.getSelectedItemId());
                ChannelAddBottomSheetDialogFragment.this.dismiss();
            }
        });
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog= DialogUtils.createAddCategoryDialog((MainActivity) getActivity(), new DialogUtils.CreateAddCategoryDialogInterface() {
                    @Override
                    public void onCreateCategory(Category category) {
                        selectedCategoryId = maxId + 1;
                    }
                });
                alertDialog.show();

            }
        });
    }
    private void toggleSelectAllButton(boolean allChecked){
        if (allChecked){
            selectAllButton.setText(R.string.unsellect_all);
            selectAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    channelViewModel.updateTempChannelsChecked(false);
                }
            });
        }else {
            selectAllButton.setText(R.string.select_all);
            selectAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    channelViewModel.updateTempChannelsChecked(true);
                }
            });
        }
    }
    private boolean isAllChecked(List<Channel> channels){
        for (Channel c:channels){
            if (!c.isChecked()){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        channelViewModel.dismissTempChannels();
        super.onCancel(dialog);
    }

    @Override
    public int getTheme() {
        return ThemeUtil.getStyleFromAttr(getContext(),R.attr.bottomSheetDialogTheme);
    }
}
