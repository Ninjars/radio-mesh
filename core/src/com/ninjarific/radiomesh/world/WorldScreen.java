package com.ninjarific.radiomesh.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.ninjarific.radiomesh.RadioMeshGame;
import com.ninjarific.radiomesh.world.data.WorldModel;
import com.ninjarific.radiomesh.world.interaction.IWorldEventHandler;
import com.ninjarific.radiomesh.world.interaction.WorldEventHandler;
import com.ninjarific.radiomesh.world.scene.WorldStageManager;


public class WorldScreen implements Screen {

    private final RadioMeshGame game;
    private final IWorldEventHandler stageEventHandler;
    private final WorldStageManager stageManager;
    private WorldModel model;

    public WorldScreen(RadioMeshGame game) {
        this.game = game;
        stageEventHandler = new WorldEventHandler(this);
        stageManager = new WorldStageManager(stageEventHandler);
    }

    public void attachToInput(InputMultiplexer inputMultiplexer) {
        stageManager.attachToInput(inputMultiplexer);
    }

    public void removeFromInput(InputMultiplexer inputMultiplexer) {
        stageManager.removeFromInput(inputMultiplexer);
    }

    @Override
    public void show() {
        Gdx.gl.glClearColor(0.24f, 0.24f, 0.48f, 1);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (model != null) {
            stageManager.draw(model.getBounds());
        }
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
    public void hide() {

    }

    @Override
    public void dispose() {
        stageManager.dispose();
    }

    public void setModel(WorldModel model) {
        this.model = model;
        stageManager.setData(model.getMap());
    }
}
