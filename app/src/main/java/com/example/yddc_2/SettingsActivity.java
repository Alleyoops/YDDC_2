package com.example.yddc_2;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.yddc_2.utils.HideBar;
import com.example.yddc_2.utils.SecuritySP;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
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
        HideBar.hideBar(this);
        back();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
           Preference preference = (Preference) findPreference("exit");
            assert preference != null;
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
               @Override
               public boolean onPreferenceClick(Preference preference) {
                   //清楚某些本地SharedPreference的xml文件内容
                   try {
                       SecuritySP.Remove(getContext(),"token");
                       SecuritySP.Remove(getContext(),"ph");
                       SecuritySP.Remove(getContext(),"pwd");
                       SecuritySP.Remove(getContext(),"reciteWay");
                       SecuritySP.Remove(getContext(),"setting");
                   } catch (GeneralSecurityException | IOException e) {
                       e.printStackTrace();
                   }
                   Toast.makeText(getContext(), "已清除所有账号数据", Toast.LENGTH_SHORT).show();
                   return false;
               }
           });
        }
    }

    //使导航栏返回键可用
    private void back(){
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_result);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}