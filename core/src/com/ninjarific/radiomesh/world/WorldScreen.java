package com.ninjarific.radiomesh.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.ninjarific.radiomesh.RadioMeshGame;
import com.ninjarific.radiomesh.scan.nodes.MutableBounds;
import com.ninjarific.radiomesh.world.data.MapPiece;
import com.ninjarific.radiomesh.world.data.WorldModel;
import com.ninjarific.radiomesh.world.interaction.IWorldEventHandler;
import com.ninjarific.radiomesh.world.interaction.WorldEventHandler;
import com.ninjarific.radiomesh.world.scene.WorldStageManager;

import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.RectD;
import org.kynosarges.tektosyne.geometry.Voronoi;
import org.kynosarges.tektosyne.geometry.VoronoiResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class WorldScreen implements Screen {
    private static final int WORLD_WIDTH = 1000;
    private static final int WORLD_HEIGHT = 1000;

    private final RadioMeshGame game;
    private final IWorldEventHandler stageEventHandler;
    private final WorldStageManager stageManager;
    private final MutableBounds bounds = new MutableBounds(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    private List<MapPiece> mapPieces = Collections.emptyList();

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
        stageManager.draw(bounds);
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
        Random random = new Random(model.getSeed());

        final int count = 500;
        PointD[] points = new PointD[count];
        for (int i = 0; i < count; i++) {
            PointD point = new PointD(random.nextInt(WORLD_WIDTH), random.nextInt(WORLD_HEIGHT));
            points[i] = point;
        }
        RectD clippingRect = new RectD(bounds.left, bounds.top, bounds.right, bounds.bottom);
        VoronoiResults voroniGraph = Voronoi.findAll(points, clippingRect);

        PointD[][] regions = voroniGraph.voronoiRegions();

        this.mapPieces = new ArrayList<>(regions.length);
        for (PointD[] region : regions) {
            mapPieces.add(new MapPiece(region));
        }

        stageManager.setData(mapPieces);
    }
}
