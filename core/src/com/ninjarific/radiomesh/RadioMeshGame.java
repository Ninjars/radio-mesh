package com.ninjarific.radiomesh;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.ninjarific.radiomesh.nodes.ForceConnectedNode;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.scene.StageManager;
import com.ninjarific.radiomesh.utils.listutils.Change;

import java.util.ArrayList;
import java.util.List;

public class RadioMeshGame extends ApplicationAdapter {
	private static final String TAG = RadioMeshGame.class.getSimpleName();
    private GameEngine gameEngine = new GameEngine();
    private StageManager stageManager;

    @Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.gl.glClearColor(0.24f, 0.24f, 0.24f, 1);
		stageManager = new StageManager();
		stageManager.setData(gameEngine.getNodes());
	}

    @Override
	public void render () {
        gameEngine.updateGameState(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        MutableBounds nodeBounds = gameEngine.getBounds();
        stageManager.draw(nodeBounds);
    }

    @Override
	public void dispose () {
        stageManager.dispose();
	}

	public void onScanStarted() {
        // TODO: ui effect
    }

	public void setData(List<ForceConnectedNode> data) {
		Gdx.app.log(TAG, "setData " + data);
		List<Change<ForceConnectedNode>> diff = getDiff(gameEngine.getNodes(), data);
        gameEngine.setData(data);
        if (stageManager != null) {
            stageManager.updateNodes(diff);
        }
	}

    private List<Change<ForceConnectedNode>> getDiff(List<ForceConnectedNode> currentData,
                                                     List<ForceConnectedNode> newData) {
        List<Change<ForceConnectedNode>> returnList = new ArrayList<>(newData.size());
        for (ForceConnectedNode node : newData) {
            if (!currentData.contains(node)) {
                returnList.add(new Change<>(Change.Type.ADDED, node));
            }
        }

        for (ForceConnectedNode node : currentData) {
            if (!newData.contains(node)) {
                returnList.add(new Change<>(Change.Type.REMOVED, node));
            }
        }
        return returnList;
    }
}
