package com.ninjarific.radiomesh;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.ninjarific.radiomesh.nodes.INode;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.radialgraph.NodeData;
import com.ninjarific.radiomesh.scene.StageManager;
import com.ninjarific.radiomesh.utils.listutils.Change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RadioMeshGame extends ApplicationAdapter {
    private static final String TAG = RadioMeshGame.class.getSimpleName();
    private GameEngine gameEngine = new GameEngine();
    private StageManager stageManager;
    private List<NodeData> currentNodes = Collections.emptyList();

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.gl.glClearColor(0.24f, 0.24f, 0.24f, 1);
        stageManager = new StageManager();
        stageManager.setData(gameEngine.getNodes());
    }

    @Override
    public void render() {
        gameEngine.updateGameState(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        MutableBounds nodeBounds = gameEngine.getBounds();
        stageManager.draw(nodeBounds);
    }

    @Override
    public void dispose() {
        stageManager.dispose();
    }

    public void onScanStarted() {
        // TODO: ui effect
    }

    public void setData(List<NodeData> data) {
        List<Change<NodeData>> diff = getDiff(currentNodes, data);
        List<Change<INode>> nodeChanges = gameEngine.updateNodes(diff);
//        Gdx.app.log(TAG, "updateNodes " + data
//                + "\ndiff: " + diff
//                + "\nnodeChanges: " + nodeChanges);
        if (stageManager != null) {
            stageManager.updateNodes(nodeChanges);
        }
        currentNodes = data;
    }

    private static <T> List<Change<T>> getDiff(List<T> currentData, List<T> newData) {
        List<Change<T>> returnList = new ArrayList<>(newData.size());
        for (T node : newData) {
            if (!currentData.contains(node)) {
                returnList.add(new Change<>(Change.Type.ADD, node));
            }
        }

        for (T node : currentData) {
            if (!newData.contains(node)) {
                returnList.add(new Change<>(Change.Type.REMOVE, node));
            } else {
                returnList.add(new Change<>(Change.Type.UPDATE, node));
            }
        }
        return returnList;
    }
}
