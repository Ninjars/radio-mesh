package com.ninjarific.radiomesh;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.ninjarific.radiomesh.database.room.DatabaseHelper;
import com.ninjarific.radiomesh.utils.ScanSchedulerUtil;

import timber.log.Timber;

public class GraphicsActivity extends AndroidApplication implements IMessageHandler {
    public static final String PREF_BACKGROUND_SCAN = "background_scans";
    public static final String BUNDLE_GRAPH_ID = "graph_id";

    private static WifiScanner wifiScanner;
    private static DatabaseHelper roomDatabaseHelper;

    @Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		config.useGyroscope = false;
		config.useImmersiveMode = true;
		config.useRotationVectorSensor = false;
		initialize(new RadioMeshGame(), config);

        Timber.plant(new Timber.DebugTree());
        roomDatabaseHelper = DatabaseHelper.init(this);
        wifiScanner = new WifiScanner(this, roomDatabaseHelper, message
                -> Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show());
        boolean backgroundScan = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(PREF_BACKGROUND_SCAN, false);
        if (backgroundScan) {
            boolean scanJobStarted = ScanSchedulerUtil.scheduleScanJob(this);
            Timber.d("scan job started? " + scanJobStarted);
        }
    }

    @Override
    public void onMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public static WifiScanner getWifiScanner() {
        return wifiScanner;
    }

    public static DatabaseHelper getDatabaseHelper() {
        return roomDatabaseHelper;
    }
}
