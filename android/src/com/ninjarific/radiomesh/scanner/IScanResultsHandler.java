package com.ninjarific.radiomesh.scanner;

import android.net.wifi.ScanResult;

import java.util.List;

public interface IScanResultsHandler {
    void onScanCompleted(List<ScanResult> scanResults);

    void onScanStarted();
}
