package com.ninjarific.radiomesh;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.ninjarific.radiomesh.scan.ScanScreen;
import com.ninjarific.radiomesh.scan.radialgraph.NodeData;
import com.ninjarific.radiomesh.world.WorldScreen;
import com.ninjarific.radiomesh.world.logger.LoadingLogger;
import com.ninjarific.radiomesh.world.worldgenerator.WorldGenerator;
import com.ninjarific.radiomesh.world.worldgenerator.WorldModel;

import java.util.List;

public class RadioMeshGame extends Game implements InputProcessor {
    private static final String TAG = RadioMeshGame.class.getSimpleName();
    private final SystemDelegate systemDelegate;
    private ScanScreen scanScreen;
    private WorldScreen worldScreen;
    private InputMultiplexer inputMultiplexer = new InputMultiplexer();

    public RadioMeshGame(SystemDelegate systemDelegate) {

        this.systemDelegate = systemDelegate;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        Gdx.gl.glClearColor(0.24f, 0.24f, 0.24f, 1);
        inputMultiplexer.addProcessor(this);
        scanScreen = new ScanScreen(this);
        worldScreen = new WorldScreen(this);
        showScanScreen();
    }

    private void showScanScreen() {
        worldScreen.removeFromInput(inputMultiplexer);

        setScreen(scanScreen);
        systemDelegate.startScanning();
        scanScreen.attachToInput(inputMultiplexer);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchBackKey(false);
    }

    private void showWorldScreen(WorldModel model) {
        scanScreen.removeFromInput(inputMultiplexer);

        worldScreen.setModel(model);
        setScreen(worldScreen);
        systemDelegate.stopScanning();
        worldScreen.attachToInput(inputMultiplexer);
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchBackKey(true);
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

    public void onNodeSelected(NodeData data) {
        LoadingLogger loadingLogger = new LoadingLogger(s -> Gdx.app.debug("WorldGenerator", s));
        showWorldScreen(WorldGenerator.generateWorld(data, loadingLogger));
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.BACK){
            // Do back button handling (show pause menu?)
            if (getScreen() == scanScreen) {
                Gdx.app.exit();
            } else {
                showScanScreen();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
