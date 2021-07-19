package com.appbroker.livetvplayer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.appbroker.livetvplayer.util.DialogUtils;
import com.appbroker.livetvplayer.util.PrefHelper;
import com.appbroker.livetvplayer.util.ThemeUtil;
import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {
    private MaterialToolbar materialToolbar;
    private PrefHelper prefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefHelper=new PrefHelper(SettingsActivity.this);
        setTheme(ThemeUtil.getPrefTheme(prefHelper));
        setContentView(R.layout.settings_activity);

        materialToolbar=findViewById(R.id.settings_activity_toolbar);
        setSupportActionBar(materialToolbar);


        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            switch (preference.getKey()){
                case "pref_store":
                    DialogUtils.showLeaveApplicationWarningDialog(getContext(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i1 = new Intent(Intent.ACTION_VIEW);
                            i1.setData(Uri.parse(
                                    "https://play.google.com/store/apps/details?id=com.appbroker.livetvplayer"));
                            i1.setPackage("com.android.vending");
                            startActivity(i1);
                        }
                    });
                    return true;
                case "pref_contact":
                    DialogUtils.showLeaveApplicationWarningDialog(getContext(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i2 = new Intent(Intent.ACTION_SEND);
                            i2.setType("*/*");
                            i2.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.dev_mail)});
                            i2.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                            if (i2.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(i2);
                            }
                        }
                    });
                    return true;
                case "pref_request_feature":
                    return true;
                    case "pref_report":
                    return true;
            }
            return false;
        }

    }
}