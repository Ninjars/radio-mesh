package com.ninjarific.radiomesh;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ninjarific.radiomesh.nodes.ForceConnectedNode;
import com.ninjarific.radiomesh.scene.NodeActor;

import java.util.List;

public class RadioMeshGame extends ApplicationAdapter {
	private static final String TAG = RadioMeshGame.class.getSimpleName();
	private List<ForceConnectedNode> data;
    private Stage stage;

    @Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.gl.glClearColor(0.24f, 0.24f, 0.24f, 1);
        Viewport viewport = new ScalingViewport(Scaling.fit, 100f, 100f);
        stage = new Stage(viewport);
        if (data != null) {
            populateStage();
        }
	}

    private void populateStage() {
        if (stage == null || data == null) {
            Gdx.app.error(TAG, "attempted to populate stage prematurely. Stage? "
                    + (stage != null) + " data? " + (data != null));
        }
        for (ForceConnectedNode node : data) {
            stage.addActor(new NodeActor(node));
        }
    }

    @Override
	public void render () {
        updateGameState(Gdx.graphics.getDeltaTime());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
	}

    private void updateGameState(float deltaTimeSeconds) {
        // TODO
    }

    @Override
	public void dispose () {
        stage.dispose();
	}

	public void setData(List<ForceConnectedNode> data) {
		Gdx.app.log(TAG, "setData " + data);
		this.data = data;

        if (stage != null) {
            stage.clear();
            populateStage();
        }
	}
}
