package com.ninjarific.radiomesh;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.ninjarific.radiomesh.scan.ScanScreen;
import com.ninjarific.radiomesh.scan.radialgraph.NodeData;
import com.ninjarific.radiomesh.world.WorldScreen;
import com.ninjarific.radiomesh.world.data.WorldModel;

import java.util.List;

public class RadioMeshGame extends Game {
    private static final String TAG = RadioMeshGame.class.getSimpleName();
    private ScanScreen scanScreen;
    private WorldScreen worldScreen;
    private InputMultiplexer inputMultiplexer = new InputMultiplexer();

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.gl.glClearColor(0.24f, 0.24f, 0.24f, 1);

        scanScreen = new ScanScreen(this);
        worldScreen = new WorldScreen(this);
//        showScanScreen();
        showWorldScreen(new WorldModel());
    }

    private void showScanScreen() {
        worldScreen.removeFromInput(inputMultiplexer);

        setScreen(scanScreen);
        scanScreen.attachToInput(inputMultiplexer);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void showWorldScreen(WorldModel model) {
        scanScreen.removeFromInput(inputMultiplexer);

        worldScreen.setModel(model);
        setScreen(worldScreen);
        worldScreen.attachToInput(inputMultiplexer);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        scanScreen.dispose();
        super.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.dispose();
    }

    public void onScanResults(List<NodeData> nodes) {
        if (scanScreen != null) {
            scanScreen.setData(nodes);
        }
    }

    public void onScanStarted() {
        if (scanScreen != null) {
            scanScreen.onScanStarted();
        }
    }
}
