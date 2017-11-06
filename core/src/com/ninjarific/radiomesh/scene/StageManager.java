package com.ninjarific.radiomesh.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ninjarific.radiomesh.nodes.ForceConnectedNode;
import com.ninjarific.radiomesh.nodes.MutableBounds;
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

    public void updateNodes(List<Change<ForceConnectedNode>> changes) {
        for (Change<ForceConnectedNode> change : changes) {
            ForceConnectedNode changeNode = change.getValue();
            switch (change.getType()) {
                case ADDED:
                    addNode(changeNode);
                    break;
                case REMOVED:
                    for (NodeActor actor : nodeActors) {
                        if (actor.getNodeIndex() == changeNode.getIndex()) {
                            actor.remove();
                            nodeActors.remove(actor);
                            break;
                        }
                    }
            }
        }
    }

    private void addNode(ForceConnectedNode node) {
        NodeActor newActor = new NodeActor(node);
        nodeActors.add(newActor);
        stage.addActor(newActor);
    }

    public void setData(List<ForceConnectedNode> nodes) {
        for (ForceConnectedNode node : nodes) {
            addNode(node);
        }
    }
}
