package com.ninjarific.radiomesh.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;

import com.ninjarific.radiomesh.IMessageHandler;

import java.util.List;

import timber.log.Timber;

public class WifiScanner {

    private final WifiManager wifiManager;
    private final BroadcastReceiver broadCastReceiver;
    private final IMessageHandler messageReceiver;
    private IScanResultsHandler resultsHandler;

    private ScanState scanState = ScanState.IDLE;

    private enum ScanState {
        IDLE,
        PENDING,
        SCANNING
    }

    public WifiScanner(Context context, IMessageHandler messageReceiver) {
        this.messageReceiver = messageReceiver;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        broadCastReceiver = new WifiReceiver(this);
    }

    public void onScanResultsAvailable() {
        onScanResults(wifiManager.getScanResults());
    }

    public void register(Context context) {
        context.registerReceiver(broadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        context.registerReceiver(broadCastReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    }

    public void unregister(Context context) {
        try {
            context.unregisterReceiver(broadCastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * receives callbacks on successful scans
     * @param resultsHandler
     */
    public void setResultsHandler(@Nullable IScanResultsHandler resultsHandler) {
        this.resultsHandler = resultsHandler;
    }

    /**
     * Requires that wifi state permission has already been granted
     */
    public boolean triggerScan() {
        Timber.i("startScan: results handler " + resultsHandler);
        if (!wifiManager.isWifiEnabled()) {
            Timber.w(">> aborting scan; wifi not enabled");
            return false;
        }
        switch (scanState) {
            case SCANNING:
                sendMessage("Scan already running");
                return false;

            case PENDING:
                sendMessage("Waiting for wifi adapter");
                return false;

            case IDLE:
                if (ableToScan()) {
                    scanState = ScanState.PENDING;
                    startScan();
                    return true;
                }
                return false;
        }
        return false;
    }

    private boolean ableToScan() {
        if (!wifiManager.isWifiEnabled()) {
            return false;
        } else {
            Timber.d("Wifi enabled already");
            return true;
        }
    }

    private void startScan() {
        if (scanState == ScanState.PENDING) {
            Timber.d("\t start scan");
            sendMessage("Scanning");
            wifiManager.startScan();
            scanState = ScanState.SCANNING;
        }
    }

    public void onReceiveStateChange(int intExtra) {
        Timber.i("onReceiveStateChange() " + intExtra);
        switch (intExtra) {
            case WifiManager.WIFI_STATE_ENABLED:
                Timber.d("\t WIFI_STATE_ENABLED");
                startScan();
                break;
        }
    }

    private void onScanResults(List<ScanResult> scanResults) {
        if (scanState == ScanState.SCANNING) {
            Timber.i("onScanCompleted() count " + scanResults.size());
            scanState = ScanState.IDLE;
            // TODO: instead of persisting here, send results to the ui
            if (resultsHandler != null) {
                resultsHandler.onScanCompleted(scanResults);
            }

        } else {
            Timber.i("ignoring system scan");
        }
    }

    private void sendMessage(String string) {
        messageReceiver.onMessage(string);
    }

}
