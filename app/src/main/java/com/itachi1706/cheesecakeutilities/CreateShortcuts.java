package com.itachi1706.cheesecakeutilities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.itachi1706.cheesecakeutilities.recyclerAdapters.CreateShortcutsAdapter;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateShortcuts extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shortcuts);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        CreateShortcutsAdapter adapter = new CreateShortcutsAdapter(generateList(), new CreateShortcutHandler(this));
        recyclerView.setAdapter(adapter);
    }

    private List<String> generateList() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        List<String> menuitems = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.mainmenu)));
        List<String> gameitems = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.gamesmenu)));
        menuitems.addAll(gameitems);
        List<String> hiddenitems = new ArrayList<>(Arrays.asList(mFirebaseRemoteConfig.getString("serverHide").split("\\|\\|\\|")));

        menuitems.removeAll(hiddenitems);
        return menuitems;
    }

    private void createShortcut(String className, String link) {
        LogHelper.i("CreateShortcuts", "Attempting to create shortcut for " + className);
        try {
            Class classObj = Class.forName(className);
            Intent intent = new Intent(this, classObj);
            intent.setAction(Intent.ACTION_MAIN);
            intent.putExtra("menuitem", link);
            intent.putExtra("globalcheck", true);

            // Using API 26 new method of creating shortcuts
            ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(this, link.replace(" ", "-"))
                    .setShortLabel(link).setLongLabel(link + " Utility")
                    .setIntent(intent).setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher_round))
                    .build();
            Intent shortcutIntent = ShortcutManagerCompat.createShortcutResultIntent(this, shortcutInfo);

            // Firebase Analytics Event Logging
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, link);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "launcher_shortcut_created");
            analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            LogHelper.i("Firebase", "Logged Event Utility Shortcut Created: " + link);

            LogHelper.i("CreateShortcuts", "Shortcut Creation success");

            setResult(RESULT_OK, shortcutIntent);
            finish();
            return;
        } catch (ClassNotFoundException e) {
            LogHelper.e("CreateShortcutAdapter", "Class Not Found: " + className);
            e.printStackTrace();
        }
        LogHelper.e("CreateShortcuts", "Shortcut Creation failed error");
        setResult(RESULT_CANCELED);
        finish();
    }

    public static final int CREATE_SHORTCUT_ADAPTER_DONE = 1111;
    public static final int CREATE_SHORTCUT_ADAPTER_FAIL = 1112;

    private static class CreateShortcutHandler extends Handler {
        WeakReference<CreateShortcuts> mActivity;

        CreateShortcutHandler(CreateShortcuts activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CreateShortcuts act = mActivity.get();
            super.handleMessage(msg);

            switch (msg.what) {
                case CREATE_SHORTCUT_ADAPTER_DONE:
                    String cn = msg.getData().getString("class");
                    String title = msg.getData().getString("title");
                    act.createShortcut(cn, title);
                    break;
                case CREATE_SHORTCUT_ADAPTER_FAIL:
                    LogHelper.e("CreateShortcuts", "Shortcut Creation failed");
                    act.setResult(RESULT_CANCELED);
                    act.finish();
                    break;
            }
        }
    }
}
