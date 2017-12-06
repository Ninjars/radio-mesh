package com.ninjarific.radiomesh.scan.scene;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ninjarific.radiomesh.Constants;
import com.ninjarific.radiomesh.scan.nodes.IPositionProvider;

import java.util.Random;

public class NodeActor<T extends IPositionProvider> extends Actor {
    private static final Random COLOR_RANDOM = new Random();
    private final int size = Constants.NODE_WIDTH;
    private final T dataProvider;
    private Texture texture;

    public NodeActor(T node) {
        this.dataProvider = node;
        setTouchable(Touchable.enabled);
        setWidth(size);
        setHeight(size);
    }

    public T getDataProvider() {
        return dataProvider;
    }

    private Texture createTexture() {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(COLOR_RANDOM.nextFloat(), COLOR_RANDOM.nextFloat(), COLOR_RANDOM.nextFloat(), 1);
        int radius = (int) Math.floor(this.size /2f);
        pixmap.fillCircle(radius, radius, radius);
        Texture newTexture = new Texture(pixmap);
        pixmap.dispose();
        return newTexture;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (texture == null) {
            texture = createTexture();
        }
        setX(dataProvider.getX()-size/2f);
        setY(dataProvider.getY()-size/2f);
        setBounds(getX(), getY(), getWidth(), getHeight());
        batch.draw(texture, getX(), getY());
    }
}
