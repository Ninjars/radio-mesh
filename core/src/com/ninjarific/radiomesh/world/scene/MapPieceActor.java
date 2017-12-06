package com.ninjarific.radiomesh.world.scene;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.ShortArray;
import com.ninjarific.radiomesh.world.data.MapPiece;

import org.kynosarges.tektosyne.geometry.PointD;

import java.util.Random;

// TODO: currently this is expecting a separate actor for each map piece.  This may be very inefficient to render, so consider alternative strategies
// TODO: suggestion may be to have a single actor to handle all map pieces, so a single PolygonSpriteBatch is used in a single pass per frame
public class MapPieceActor extends Actor {
    private static final Random COLOR_RANDOM = new Random();
    private final MapPiece data;
    private final PolygonSpriteBatch spriteBatch;
    private PolygonSprite sprite;

    public MapPieceActor(MapPiece node, PolygonSpriteBatch spriteBatch) {
        this.data = node;
        this.spriteBatch = spriteBatch;
        setTouchable(Touchable.enabled);
        setWidth(data.width());
        setHeight(data.height());
        setX(data.getX());
        setY(data.getY());
        setBounds(getX(), getY(), getWidth(), getHeight());
    }

    public MapPiece getData() {
        return data;
    }

    private static PolygonSprite createSprite(MapPiece data) {
        Pixmap pixmap = new Pixmap((int)Math.ceil(data.width()), (int)Math.ceil(data.height()), Pixmap.Format.RGBA8888);
        pixmap.setColor(COLOR_RANDOM.nextFloat(), COLOR_RANDOM.nextFloat(), COLOR_RANDOM.nextFloat(), 1);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        PointD[] points = data.getVertexes();
        float[] vertices = new float[points.length * 2];
        for (int i = 0; i < points.length; i++) {
            vertices[i*2] = (float) points[i].x;
            vertices[i*2 + 1] = (float) points[i].y;
        }
        ShortArray triangles = new EarClippingTriangulator().computeTriangles(vertices);

        PolygonRegion region = new PolygonRegion(new TextureRegion(texture), vertices, triangles.items);
        PolygonSprite sprite = new PolygonSprite(region);
        sprite.setOrigin(data.getCenterX(), data.getCenterY());
        return sprite;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (sprite == null) {
            sprite = createSprite(data);
        }
        batch.end();
        spriteBatch.begin();
        sprite.draw(spriteBatch);
        spriteBatch.end();
        batch.begin();
    }
}
