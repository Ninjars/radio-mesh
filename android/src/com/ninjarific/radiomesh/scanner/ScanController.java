package com.ninjarific.radiomesh.scanner;

import android.Manifest;
import android.app.Activity;
import android.os.Handler;

import com.ninjarific.radiomesh.MainApplication;

import java.lang.ref.WeakReference;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class ScanController implements IScanController {
    private static final int PERMISSIONS_REQUEST_CODE_WIFI = 666;
    private WeakReference<Activity> activityReference;
    private Handler handler;
    private IScanResultsHandler scanResultsHandler;

    @Override
    public void beginScanning(Activity activity, int intervalMs, IScanResultsHandler scanResultsHandler) {
        Timber.v("beginScanning()");
        activityReference = new WeakReference<>(activity);
        this.scanResultsHandler = scanResultsHandler;
        MainApplication.getWifiScanner().register(activity);
        MainApplication.getWifiScanner().setResultsHandler(scanResultsHandler);
        if (handler == null) {
            handler = new Handler();
        } else {
            handler.removeCallbacksAndMessages(null);
        }
        Runnable scanLoopTask = new Runnable() {
            @Override
            public void run() {
                performScan();
                handler.postDelayed(this, intervalMs);
            }
        };
        handler.post(scanLoopTask);
    }

    @Override
    public void stopScanning() {
        Timber.v("stopScanning()");
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        Activity activity = activityReference.get();
        if (activity != null) {
            MainApplication.getWifiScanner().unregister(activity);
            return;
        }
        MainApplication.getWifiScanner().setResultsHandler(null);
    }

    private void performScan() {
        Timber.v("performing scan");
        Activity activity = activityReference.get();
        if (activity == null) {
            Timber.w("aborting scan as activity context has been lost");
            stopScanning();
            return;
        }
        if (EasyPermissions.hasPermissions(activity, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            boolean scanning = MainApplication.getWifiScanner().triggerScan();
            if (scanning) {
                scanResultsHandler.onScanStarted();
            }

        } else {
            Timber.v(">> requesting permission");
            EasyPermissions.requestPermissions(activity, "Required to read wifi hotspots",
                    PERMISSIONS_REQUEST_CODE_WIFI, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }
}
