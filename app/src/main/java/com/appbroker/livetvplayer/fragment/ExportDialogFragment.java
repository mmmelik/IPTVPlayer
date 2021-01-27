package com.appbroker.livetvplayer.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.appbroker.livetvplayer.R;

public class ExportDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(requireContext());
        builder.setCancelable(true);
        builder.setView(R.layout.fragment_export_dialog);
        return builder.create();
    }
}
