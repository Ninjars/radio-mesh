package com.ninjarific.radiomesh.scanner;

import android.app.Activity;

public interface IScanController {
    void beginScanning(Activity activity, int intervalMs, IScanResultsHandler scanResultsHandler);
    void stopScanning();
}
