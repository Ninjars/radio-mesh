package com.ninjarific.radiomesh.scan.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets;
import com.ninjarific.radiomesh.scan.interaction.IStageEventHandler;
import com.ninjarific.radiomesh.scan.nodes.IPositionProvider;
import com.ninjarific.radiomesh.scan.nodes.MutableBounds;
import com.ninjarific.radiomesh.scan.radialgraph.NodeData;
import com.ninjarific.radiomesh.utils.listutils.Change;

import java.util.ArrayList;
import java.util.List;

public class StageManager<T extends IPositionProvider> {
    private final IStageEventHandler eventHandler;

    private Stage uiStage;
    private Table uiRootTable;
    @GDXAssets(skinFiles = {"android/assets/uiskin.json"})
    private Skin uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
    @GDXAssets(atlasFiles = {"android/assets/uiskin.atlas"})
    private TextureAtlas uiAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));

    private Stage gameStage;
    private OrthographicCamera gameCamera;
    private List<NodeActor> nodeActors = new ArrayList<>();

    public StageManager(IStageEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(true);
        Viewport viewport = new ScalingViewport(Scaling.fit, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), gameCamera);
        gameStage = new Stage(viewport);

        uiStage = new Stage(new ScreenViewport());

        uiRootTable = new Table();
        uiRootTable.setFillParent(true); // only valid for root widgets added to stage; normally parent sets size of child
        uiStage.addActor(uiRootTable);
    }

    public void attachToInput(InputMultiplexer inputMultiplexer) {
        inputMultiplexer.addProcessor(gameStage);
        inputMultiplexer.addProcessor(uiStage);
    }

    public void removeFromInput(InputMultiplexer inputMultiplexer) {
        inputMultiplexer.removeProcessor(gameStage);
        inputMultiplexer.removeProcessor(uiStage);
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

    public void displayNodeData(NodeData nodeData) {
        uiRootTable.reset();
        uiRootTable.setDebug(true); // add debug lines
        uiRootTable.bottom().pad(20);

        Label bssid = new Label(nodeData.getBssid(), uiSkin);
        bssid.setFontScale(2f);
        Label ssid = new Label(nodeData.getSsid(), uiSkin);
        ssid.setFontScale(3f);
        Label frequency = new Label("frequnecy: " + nodeData.getFrequency(), uiSkin);
        uiRootTable.add(bssid);
        uiRootTable.row();
        uiRootTable.add(ssid);
        uiRootTable.row();
        uiRootTable.add(frequency);
        uiRootTable.row();
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
