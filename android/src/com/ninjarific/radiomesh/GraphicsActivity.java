package com.ninjarific.radiomesh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.ninjarific.radiomesh.database.room.DatabaseHelper;
import com.ninjarific.radiomesh.database.room.entities.Connection;
import com.ninjarific.radiomesh.database.room.entities.Node;
import com.ninjarific.radiomesh.nodes.ForceConnectedNode;
import com.ninjarific.radiomesh.utils.listutils.ListUtils;
import com.ninjarific.radiomesh.visualisation.DebugDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GraphicsActivity extends AndroidApplication implements IMessageHandler {
    public static final String BUNDLE_GRAPH_ID = "graph_id";
    private Disposable disposable;
    private RadioMeshGame game;
    private long graphId;

    private static List<Long> getConnectedNodes(DatabaseHelper dbHelper, long nodeId) {
        // TODO: avoid iterative database lookups with a single query to get connected nodes directly via Connection object
        List<Connection> connections = dbHelper.getConnectedNodes(nodeId);
        return ListUtils.map(connections, connection -> dbHelper.getNode(connection.getToNodeId()).getId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useImmersiveMode = true;
        config.useRotationVectorSensor = false;
        game = new RadioMeshGame();
        initialize(game, config);

        Intent intent = getIntent();
        graphId = intent.getLongExtra(BUNDLE_GRAPH_ID, -1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        disposable = getObservableData(graphId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                //              .doOnNext(nodes -> loadingSpinner.setVisibility(View.GONE))
                .subscribe(game::setData, Throwable::printStackTrace);
    }

    @Override
    protected void onStop() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onStop();
    }

    private Flowable<List<ForceConnectedNode>> getObservableData(long graphId) {
        if (graphId < 0) {
            return Flowable.just(DebugDataProvider.getDebugData((int) graphId));
        }

        Random random = new Random(0);
        DatabaseHelper dbHelper = MainApplication.getDatabaseHelper();
        return dbHelper.getNodesForGraphObs(graphId)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .map(dataNodes -> {
                    List<Long> nodeIds = ListUtils.map(dataNodes, Node::getId);
                    List<ForceConnectedNode> connectedNodes = new ArrayList<>();
                    for (int i = 0; i < dataNodes.size(); i++) {
                        Node node = dataNodes.get(i);
                        List<Long> neighbourNodeIds = getConnectedNodes(dbHelper, node.getId());
                        List<Integer> neighbourIndexes = ListUtils.map(neighbourNodeIds, nodeIds::indexOf);
                        ForceConnectedNode connectedNode = new ForceConnectedNode(i, neighbourIndexes, random.nextFloat() * 100, random.nextFloat() * 100);
                        connectedNodes.add(connectedNode);
                    }
                    return connectedNodes;
                });
    }

    @Override
    public void onMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
