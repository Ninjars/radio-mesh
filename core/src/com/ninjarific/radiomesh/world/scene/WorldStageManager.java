package com.ninjarific.radiomesh.world.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gmail.blueboxware.libgdxplugin.annotations.GDXAssets;
import com.ninjarific.radiomesh.coordinates.Bounds;
import com.ninjarific.radiomesh.world.data.MapPiece;
import com.ninjarific.radiomesh.world.interaction.IWorldEventHandler;

import java.util.List;

public class WorldStageManager {
    private final IWorldEventHandler eventHandler;
    private final PolygonSpriteBatch spriteBatch;
    private final Stage gameDebugStage;

    private final Stage gameStage;
    private final OrthographicCamera gameCamera;
    private final Stage uiStage;
    private Table uiRootTable;

    @GDXAssets(skinFiles = {"android/assets/uiskin.json"})
    private Skin uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
    @GDXAssets(atlasFiles = {"android/assets/uiskin.atlas"})
    private TextureAtlas uiAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));

    public WorldStageManager(IWorldEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        spriteBatch = new PolygonSpriteBatch();
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(true);
        Viewport viewport = new ScalingViewport(Scaling.fit, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), gameCamera);
        gameStage = new Stage(viewport);

        gameDebugStage = new Stage(viewport);

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

    public void draw(Bounds bounds) {
        double zoom = Math.max(
                bounds.getWidth() / gameCamera.viewportWidth,
                bounds.getHeight() / gameCamera.viewportHeight);
        float camX = (float) (bounds.left + bounds.getWidth() / 2f);
        float camY = (float) (bounds.top + bounds.getHeight() / 2f);

        gameCamera.zoom = (float) zoom;
        gameCamera.position.set(camX, camY, 0);

        spriteBatch.setProjectionMatrix(gameCamera.combined);

        gameStage.draw();
        gameDebugStage.draw();
        uiStage.draw();
    }

    public void dispose() {
        gameStage.dispose();
        gameDebugStage.dispose();
        uiStage.dispose();
    }

    public void resize(int width, int height) {
        gameStage.getViewport().update(width, height);
        uiStage.getViewport().update(width, height, true);
    }

    public void setData(List<MapPiece> mapPieces) {
        gameStage.clear();
        gameDebugStage.clear();
        for (MapPiece piece : mapPieces) {
            MapPieceActor actor = new MapPieceActor(piece, spriteBatch);
            gameStage.addActor(actor);
        }
    }
}
