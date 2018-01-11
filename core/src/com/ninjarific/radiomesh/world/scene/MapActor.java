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
import com.ninjarific.radiomesh.coordinates.Coordinate;
import com.ninjarific.radiomesh.world.WorldColors;
import com.ninjarific.radiomesh.world.data.Center;
import com.ninjarific.radiomesh.world.data.Corner;
import com.ninjarific.radiomesh.world.data.Edge;
import com.ninjarific.radiomesh.world.data.MapProperties;

import org.kynosarges.tektosyne.geometry.PointD;

import java.util.ArrayList;
import java.util.Arrays;
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
        for (Edge edge : edges) {
            Color color = WorldColors.getEdgeColor(edge.getRiverValue());
            outList.add(createEdgeSprite(edge.v0, edge.v1, color));
        }
    }

    private static PolygonSprite createEdgeSprite(Corner a, Corner b, Color color) {
        final double riverFactor = 0.5;
        List<Coordinate> cornerCoordinates = Arrays.asList(
                getPerpendicularPoint(a.position, b.position, a.getMapProperties().getRiverValue() * riverFactor),
                getPerpendicularPoint(b.position, a.position, a.getMapProperties().getRiverValue() * riverFactor),
                getPerpendicularPoint(a.position, b.position, -a.getMapProperties().getRiverValue() * riverFactor),
                getPerpendicularPoint(b.position, a.position, -a.getMapProperties().getRiverValue() * riverFactor));
        double minX, minY, maxX, maxY, dx, dy;
        minX = minY = Float.MAX_VALUE;
        maxX = maxY = Float.MIN_VALUE;
        for (Coordinate coord : cornerCoordinates) {
            if (minX > coord.x) {
                minX = coord.x;
            }
            if (maxX < coord.x) {
                maxX = coord.x;
            }
            if (minY > coord.y) {
                minY = coord.y;
            }
            if (maxY < coord.y) {
                maxY = coord.y;
            }
        }
        dx = maxX - minX;
        dy = maxY - minY;

        Pixmap pixmap = new Pixmap((int) Math.ceil(dx), (int) Math.ceil(dy), Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        float[] vertices = new float[] {
                (float)cornerCoordinates.get(0).x, (float)cornerCoordinates.get(0).y,
                (float)cornerCoordinates.get(1).x, (float)cornerCoordinates.get(1).y,
                (float)cornerCoordinates.get(2).x, (float)cornerCoordinates.get(2).y,
                (float)cornerCoordinates.get(3).x, (float)cornerCoordinates.get(3).y,
        };
        short[] triangles = new short[] {
                0, 1, 2,
                1, 2, 3
        };

        PolygonRegion region = new PolygonRegion(new TextureRegion(texture), vertices, triangles);
        PolygonSprite sprite = new PolygonSprite(region);
        sprite.setOrigin((float) minX, (float) minY);

        return sprite;
    }

    private static Coordinate getPerpendicularPoint(Coordinate start, Coordinate stop, double distance) {
        double x = -start.y - stop.y;
        double y = start.x - stop.x;
        double length = Math.sqrt((x * x) + (y * y));
        x /= length;
        y /= length;
        return new Coordinate(start.x + (distance * x), start.y + (distance * y));
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
