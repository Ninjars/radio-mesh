package com.ninjarific.radiomesh;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.ninjarific.radiomesh.interaction.IStageEventHandler;
import com.ninjarific.radiomesh.interaction.StageEventHandler;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.radialgraph.NodeData;
import com.ninjarific.radiomesh.radialgraph.RadialNode;
import com.ninjarific.radiomesh.scene.StageManager;
import com.ninjarific.radiomesh.utils.listutils.Change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanScreen implements Screen, NodeSelectionHandler<RadialNode> {
    private static final String TAG = ScanScreen.class.getSimpleName();

    private final RadioMeshGame game;
    private IStageEventHandler stageEventHandler;
    private GameEngine gameEngine = new GameEngine();
    private StageManager<RadialNode> stageManager;
    private List<NodeData> currentNodes = Collections.emptyList();

    public ScanScreen(RadioMeshGame game) {
        this.game = game;
        stageEventHandler = new StageEventHandler<>(this);
        stageManager = new StageManager<>(stageEventHandler);
        stageManager.setData(gameEngine.getNodes());
    }

    public void attachToInput(InputMultiplexer inputMultiplexer) {
        stageManager.attachToInput(inputMultiplexer);
    }

    public void removeFromInput(InputMultiplexer inputMultiplexer) {
        stageManager.removeFromInput(inputMultiplexer);
    }

    public void onScanStarted() {
        // TODO: ui effect
    }

    public void setData(List<NodeData> data) {
        List<Change<NodeData>> diff = getDiff(currentNodes, data);
        List<Change<RadialNode>> nodeChanges = gameEngine.updateNodes(diff);
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

    @Override
    public void onNodeSelected(RadialNode selectedNode) {
        Gdx.app.debug(TAG, "tapped on node " + selectedNode.getData().getSsid());
        stageManager.displayNodeData(selectedNode.getData());
    }

    @Override
    public void show() {
        Gdx.gl.glClearColor(0.24f, 0.24f, 0.24f, 1);
    }

    @Override
    public void hide() {

    }

    @Override
    public void render(float delta) {
        gameEngine.updateGameState(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        MutableBounds nodeBounds = gameEngine.getBounds();
        stageManager.draw(nodeBounds);
    }

    @Override
    public void resize(int width, int height) {
        stageManager.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        stageManager.dispose();
    }
}
