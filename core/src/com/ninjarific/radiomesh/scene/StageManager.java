package com.ninjarific.radiomesh.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ninjarific.radiomesh.interaction.IStageEventHandler;
import com.ninjarific.radiomesh.nodes.IPositionProvider;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.utils.listutils.Change;

import java.util.ArrayList;
import java.util.List;

public class StageManager<T extends IPositionProvider> {
    private final IStageEventHandler eventHandler;
    private Stage gameStage;
    private OrthographicCamera gameCamera;
    private List<NodeActor> nodeActors = new ArrayList<>();

    public StageManager(InputMultiplexer inputMultiplexer, IStageEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(true);
        Viewport viewport = new ScalingViewport(Scaling.fit, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), gameCamera);
        gameStage = new Stage(viewport);
        inputMultiplexer.addProcessor(gameStage);
    }

    public void draw(MutableBounds nodeBounds) {
        double zoom = Math.max(
                nodeBounds.getWidth() / gameCamera.viewportWidth,
                nodeBounds.getHeight() / gameCamera.viewportHeight);

        float camX = (float) (nodeBounds.left + nodeBounds.getWidth() / 2f);
        float camY = (float) (nodeBounds.top + nodeBounds.getHeight() / 2f);

        gameCamera.zoom = (float) zoom;
        gameCamera.position.set(camX, camY, 0);
        gameStage.draw();
    }

    public void dispose() {
        gameStage.dispose();
    }

    public void updateNodes(List<Change<T>> changes) {
        for (Change<T> change : changes) {
            T changeNode = change.getValue();
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

    private NodeActor getActorByNode(T node) {
        for (NodeActor actor : nodeActors) {
            if (actor.getDataProvider().equals(node)) {
                return actor;
            }
        }
        return null;
    }

    private void addNode(T node) {
        NodeActor newActor = new NodeActor<>(node);
        nodeActors.add(newActor);
        gameStage.addActor(newActor);
        newActor.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                eventHandler.onNodeTouched(newActor);
            }
        });
    }

    public void setData(List<T> nodes) {
        for (T node : nodes) {
            addNode(node);
        }
    }

    public void resize(int width, int height) {
        gameStage.getViewport().update(width, height);
    }
}
