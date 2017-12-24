package com.ninjarific.radiomesh.visualisation;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.ninjarific.radiomesh.IMessageHandler;
import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.RadioMeshGame;
import com.ninjarific.radiomesh.SystemDelegate;
import com.ninjarific.radiomesh.database.room.DatabaseHelper;
import com.ninjarific.radiomesh.database.room.entities.Connection;
import com.ninjarific.radiomesh.scan.radialgraph.NodeData;
import com.ninjarific.radiomesh.scanner.IScanResultsHandler;
import com.ninjarific.radiomesh.scanner.ScanController;
import com.ninjarific.radiomesh.utils.listutils.ListUtils;

import java.util.List;

import io.reactivex.disposables.Disposable;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class GraphicsActivity extends AndroidApplication implements IMessageHandler, IScanResultsHandler, EasyPermissions.PermissionCallbacks, SystemDelegate {
    public static final String BUNDLE_GRAPH_ID = "graph_id";
    private static final int SCAN_INTERVAL_MS = 1000 * 10;
    private Disposable disposable;
    private RadioMeshGame game;
    private long graphId;
    private ScanController scanController;

    private static List<Long> getConnectedNodes(DatabaseHelper dbHelper, long nodeId) {
        // TODO: avoid iterative database lookups with a single query to get connected nodes directly via Connection object
        List<Connection> connections = dbHelper.getConnectedNodes(nodeId);
        return ListUtils.map(connections, connection -> dbHelper.getNode(connection.getToNodeId()).getId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graphics);

        scanController = new ScanController();

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useImmersiveMode = false;
        config.useRotationVectorSensor = false;
        game = new RadioMeshGame(this);
        View gameView = initializeForView(game, config);
        ViewGroup container = findViewById(R.id.game_frame);
        container.addView(gameView);

        Intent intent = getIntent();
        graphId = intent.getLongExtra(BUNDLE_GRAPH_ID, -1);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        disposable = getObservableData(graphId)
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                //              .doOnNext(nodes -> loadingSpinner.setVisibility(View.GONE))
//                .subscribe(game::updateNodes, Throwable::printStackTrace);
        scanController.beginScanning(this, SCAN_INTERVAL_MS, this);
    }

    @Override
    protected void onStop() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        scanController.stopScanning();
        super.onStop();
    }

    @Override
    public void onMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScanCompleted(List<ScanResult> scanResults) {
        Timber.d(scanResults.toString());
        List<NodeData> nodes = ListUtils.map(scanResults, scan -> new NodeData(scan.BSSID, scan.SSID, WifiManager.calculateSignalLevel(scan.level, 5), scan.frequency));
        game.onScanResults(nodes);
    }

    @Override
    public void onScanStarted() {
        game.onScanStarted();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        scanController.beginScanning(this, SCAN_INTERVAL_MS, this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Timber.w("permissionsDenied");
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            scanController.beginScanning(this, SCAN_INTERVAL_MS, this);
        }
    }

    @Override
    public void stopScanning() {
        scanController.stopScanning();
    }

    @Override
    public void startScanning() {
        scanController.beginScanning(this, SCAN_INTERVAL_MS, this);
    }
}
