package com.ninjarific.radiomesh;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ninjarific.radiomesh.nodes.ForceConnectedNode;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.scene.NodeActor;

import java.util.List;

public class RadioMeshGame extends ApplicationAdapter {
	private static final String TAG = RadioMeshGame.class.getSimpleName();
    private Stage stage;
    private GameEngine gameEngine = new GameEngine();
    private OrthographicCamera camera;

    @Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.gl.glClearColor(0.24f, 0.24f, 0.24f, 1);
        camera = new OrthographicCamera();
        camera.setToOrtho(true);
        Viewport viewport = new ScalingViewport(Scaling.fit, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        stage = new Stage(viewport);
        populateStage(gameEngine.getNodes());
	}

    private void populateStage(List<ForceConnectedNode> nodes) {
        if (stage != null) {
            stage.clear();
            for (ForceConnectedNode node : nodes) {
                stage.addActor(new NodeActor(node));
            }
        }
    }

    @Override
	public void render () {
        gameEngine.updateGameState(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        MutableBounds nodeBounds = gameEngine.getBounds();
        double zoom = Math.max(
                nodeBounds.getWidth() / camera.viewportWidth,
                nodeBounds.getHeight() / camera.viewportHeight);

        float camX = (float) (nodeBounds.left + nodeBounds.getWidth() / 2f);
        float camY = (float) (nodeBounds.top + nodeBounds.getHeight() / 2f);

        camera.zoom = (float) zoom;
        camera.position.set(camX, camY, 0);
//        Gdx.app.log(TAG, "bounds: " + nodeBounds
//                + "\nviewport " + camera.viewportWidth + "," + camera.viewportHeight
//                + "\nzoom " + zoom + " cam " + camX + "," + camY);
        stage.draw();
    }

    @Override
	public void dispose () {
        stage.dispose();
	}

	public void setData(List<ForceConnectedNode> data) {
		Gdx.app.log(TAG, "setData " + data);
        gameEngine.setNodes(data);
        populateStage(data);
	}
}
