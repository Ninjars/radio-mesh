package com.ninjarific.radiomesh.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets;
import com.ninjarific.radiomesh.interaction.IStageEventHandler;
import com.ninjarific.radiomesh.nodes.IPositionProvider;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.utils.listutils.Change;

import java.util.ArrayList;
import java.util.List;

public class StageManager<T extends IPositionProvider> {
    private final IStageEventHandler eventHandler;
    private Stage gameStage;
    private Stage uiStage;
    private OrthographicCamera gameCamera;
    private List<NodeActor> nodeActors = new ArrayList<>();

    public StageManager(InputMultiplexer inputMultiplexer, IStageEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(true);
        Viewport viewport = new ScalingViewport(Scaling.fit, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), gameCamera);
        gameStage = new Stage(viewport);
        uiStage = setupUiStage();
        inputMultiplexer.addProcessor(gameStage);
        inputMultiplexer.addProcessor(uiStage);
    }

    private Stage setupUiStage() {
        Stage stage = new Stage(new ScreenViewport());
        Table table = new Table();
        table.setFillParent(true); // only valid for root widgets added to stage; normally parent sets size of child

        stage.addActor(table);

        table.setDebug(true); // add debug lines
        @GDXAssets(skinFiles = {"android/assets/uiskin.json"})
        Skin uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        @GDXAssets(atlasFiles = {"android/assets/uiskin.atlas"})
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));

        TextButton button1 = new TextButton("button 1", uiSkin);
        table.add(button1);
        TextButton button2 = new TextButton("button 2", uiSkin);
        table.add(button2);

        return stage;
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
        uiStage.draw();
    }

    public void dispose() {
        gameStage.dispose();
        uiStage.dispose();
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
        uiStage.getViewport().update(width, height, true);
    }
}
