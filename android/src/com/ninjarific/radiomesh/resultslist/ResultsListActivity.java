package com.ninjarific.radiomesh.resultslist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ninjarific.radiomesh.MainApplication;
import com.ninjarific.radiomesh.R;
import com.ninjarific.radiomesh.database.room.DatabaseHelper;
import com.ninjarific.radiomesh.database.room.entities.Graph;
import com.ninjarific.radiomesh.scanner.ScanController;
import com.ninjarific.radiomesh.utils.listutils.ListUtils;
import com.ninjarific.radiomesh.visualisation.GraphicsActivity;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class ResultsListActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION = 666;
    private RecyclerView recyclerView;
    private GraphsListAdapter adapter;
    private DatabaseHelper dbHelper;
    private Disposable disposable;
    private ScanController scanController = new ScanController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View scanButton = findViewById(R.id.fab);

        scanButton.setOnClickListener(view -> {
            MainApplication.getWifiScanner().triggerScan();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION);
            } else {
            }
        });

        dbHelper = MainApplication.getDatabaseHelper();

        adapter = new GraphsListAdapter(graphId -> {
            Intent intent = new Intent(this, GraphicsActivity.class);
            intent.putExtra(GraphicsActivity.BUNDLE_GRAPH_ID, graphId);
            this.startActivity(intent);
        });

        Flowable<List<Flowable<GraphNodes>>> graphNodesFlowable = dbHelper.observeGraphs()
                .map(graphs ->  {
                    Timber.i("graphNodesFlowable fired");
                    return ListUtils.map(graphs, this::getGraphNodesFlowable);
                });
        disposable = adapter.bind(graphNodesFlowable);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private Flowable<GraphNodes> getGraphNodesFlowable(Graph graph) {
        return dbHelper.getNodesForGraphObs(graph.getId()).map(nodeList -> new GraphNodes(graph.getId(), nodeList));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.i("onStart: registering scanner");
        MainApplication.getWifiScanner().register(this);
    }

    @Override
    protected void onStop() {
        MainApplication.getWifiScanner().unregister(this);
        Timber.i("onStop: unregistering scanner");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        recyclerView.setAdapter(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            MainApplication.getWifiScanner().triggerScan();
        }
    }
}
