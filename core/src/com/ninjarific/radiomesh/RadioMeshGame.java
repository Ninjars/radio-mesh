package com.ninjarific.radiomesh;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.ninjarific.radiomesh.radialgraph.NodeData;

import java.util.List;

public class RadioMeshGame extends Game {
    private static final String TAG = RadioMeshGame.class.getSimpleName();
    private ScanScreen scanScreen;
    private InputMultiplexer inputMultiplexer = new InputMultiplexer();

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.gl.glClearColor(0.24f, 0.24f, 0.24f, 1);

        scanScreen = new ScanScreen(this);
        showScanScreen();
    }

    private void showScanScreen() {
        setScreen(scanScreen);
        scanScreen.attachToInput(inputMultiplexer);
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
