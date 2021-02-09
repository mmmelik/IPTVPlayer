package com.appbroker.livetvplayer.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.appbroker.livetvplayer.R;
import com.appbroker.livetvplayer.adapter.CategoryListSpinnerAdapter;
import com.appbroker.livetvplayer.util.PrefHelper;
import com.appbroker.livetvplayer.util.ThemeUtil;
import com.appbroker.livetvplayer.viewmodel.CategoryViewModel;
import com.appbroker.livetvplayer.viewmodel.ChannelViewModel;

import org.w3c.dom.Text;

public class ExportDialogFragment extends DialogFragment {
    private CategoryViewModel categoryViewModel;
    private ChannelViewModel channelViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView path=view.findViewById(R.id.fragment_export_dialog_path_text);
        AppCompatSpinner appCompatSpinner=view.findViewById(R.id.fragment_export_dialog_category_spinner);
        appCompatSpinner.setAdapter(new CategoryListSpinnerAdapter(getParentFragment()));

        String p = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        path.setText(p);
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
        dialog.setTitle(R.string.export_playlist);
        return dialog;
    }

    @Override
    public int getTheme() {
        return ThemeUtil.getPrefTheme(new PrefHelper(getContext()));
    }
}
