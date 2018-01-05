package com.ninjarific.radiomesh.world.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.ShortArray;
import com.ninjarific.radiomesh.Constants;
import com.ninjarific.radiomesh.coordinates.Bounds;
import com.ninjarific.radiomesh.world.WorldColors;
import com.ninjarific.radiomesh.world.data.Center;
import com.ninjarific.radiomesh.world.data.Edge;
import com.ninjarific.radiomesh.world.data.MapProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapActor extends Actor {

    private static final String TAG = MapActor.class.getSimpleName();
    private final long seed;
    private final PolygonSpriteBatch spriteBatch;
    private final List<Center> centers;
    private final List<Edge> edges;
    private final List<PolygonSprite> sprites;

    public MapActor(PolygonSpriteBatch spriteBatch, long seed, Bounds bounds, List<Center> centers, List<Edge> edges) {
        this.seed = seed;
        this.spriteBatch = spriteBatch;
        this.centers = centers;
        this.edges = edges;
        sprites = new ArrayList<>(centers.size());
        setTouchable(Touchable.enabled);
        setWidth((float) bounds.getWidth());
        setHeight((float) bounds.getHeight());
        setX((float) bounds.left);
        setY((float) bounds.top);
        setBounds(getX(), getY(), getWidth(), getHeight());

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (sprites.isEmpty() || sprites.get(0) == null) {
            populateSprites(seed, centers, edges, sprites);
        }
        batch.end();
        spriteBatch.begin();
        for (PolygonSprite sprite : sprites) {
            sprite.draw(spriteBatch);
        }
        spriteBatch.end();
        batch.begin();
    }

    private static void populateSprites(long seed, List<Center> centers, List<Edge> edges, List<PolygonSprite> outList) {
        Random colorRandom = new Random(seed);
        outList.clear();
        for (Center center : centers) {
            Color color = getColorForMapProperties(colorRandom, center.getMapProperties());
            outList.add(createSprite(center.getBounds(), center.getPolygon(), color));
        }
    }

    private static PolygonSprite createSprite(Rectangle bounds, Polygon polygon, Color color) {
        Pixmap pixmap = new Pixmap((int) Math.ceil(bounds.getWidth()), (int) Math.ceil(bounds.getHeight()), Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        float[] vertices = polygon.getVertices();
        ShortArray triangles = new EarClippingTriangulator().computeTriangles(vertices);

        PolygonRegion region = new PolygonRegion(new TextureRegion(texture), vertices, triangles.items);
        PolygonSprite sprite = new PolygonSprite(region);
        sprite.setOrigin(bounds.getX(), bounds.getY() - bounds.getHeight());
        return sprite;
    }

    private static Color getColorForMapProperties(Random colorRandom, MapProperties properties) {
        switch (Constants.WORLD_RENDER_MODE) {
            default:
                Gdx.app.error(TAG, "unhandled render mode " + Constants.WORLD_RENDER_MODE);
            case NORMAL:
                return WorldColors.getBiomeColor(colorRandom, properties.getBiome());

            case HEIGHT:
                double elevation = properties.getElevation();
                return WorldColors.getHeightMapColor(colorRandom, elevation);

            case MOISTURE:
                double moisture = properties.getMoisture();
                return WorldColors.getGreyscale((float) moisture);
        }
    }
}
