package com.ninjarific.radiomesh.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.nodes.INode;
import com.ninjarific.radiomesh.utils.listutils.Change;

import java.util.ArrayList;
import java.util.List;

public class StageManager {
    private Stage stage;
    private OrthographicCamera camera;
    private List<NodeActor> nodeActors = new ArrayList<>();

    public StageManager() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true);
        Viewport viewport = new ScalingViewport(Scaling.fit, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        stage = new Stage(viewport);
    }

    public void draw(MutableBounds nodeBounds) {
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

    public void dispose() {
        stage.dispose();
    }

    public void updateNodes(List<Change<INode>> changes) {
        for (Change<INode> change : changes) {
            INode changeNode = change.getValue();
            switch (change.getType()) {
                case ADD:
                    addNode(changeNode);
                    break;
                case REMOVE:
                    NodeActor removeActor = getActorByNode(change.getValue());
                    if (removeActor != null) {
                        removeActor.remove();
                        nodeActors.remove(removeActor);
                    }
                    break;
            }
        }
    }

    private NodeActor getActorByNode(INode node) {
        for (NodeActor actor : nodeActors) {
            if (actor.getNode().equals(node)) {
                return actor;
            }
        }
        return null;
    }

    private void addNode(INode node) {
        NodeActor newActor = new NodeActor(node);
        nodeActors.add(newActor);
        stage.addActor(newActor);
    }

    public void setData(List<INode> nodes) {
        for (INode node : nodes) {
            addNode(node);
        }
    }
}
