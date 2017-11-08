package com.ninjarific.radiomesh;

import android.app.Application;
import android.widget.Toast;

import com.ninjarific.radiomesh.database.room.DatabaseHelper;
import com.ninjarific.radiomesh.scanner.WifiScanner;

import timber.log.Timber;

public class MainApplication extends Application implements IMessageHandler {

    private static WifiScanner wifiScanner;
    private static DatabaseHelper roomDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        roomDatabaseHelper = DatabaseHelper.init(this);

        wifiScanner = new WifiScanner(this, message
                -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    public static WifiScanner getWifiScanner() {
        return wifiScanner;
    }

    @Override
    public void onMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static DatabaseHelper getDatabaseHelper() {
        return roomDatabaseHelper;
    }
}
