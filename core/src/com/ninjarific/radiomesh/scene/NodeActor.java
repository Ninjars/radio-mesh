package com.ninjarific.radiomesh.scene;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ninjarific.radiomesh.Constants;
import com.ninjarific.radiomesh.nodes.IPositionProvider;

import java.util.Random;

public class NodeActor extends Actor {
    private static final Random COLOR_RANDOM = new Random();
    private final int size = Constants.NODE_WIDTH;
    private final IPositionProvider positionProvider;
    private Texture texture;

    public NodeActor(IPositionProvider node) {
        this.positionProvider = node;
    }

    public IPositionProvider getPositionProvider() {
        return positionProvider;
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
        batch.draw(texture, positionProvider.getX() - size /2f, positionProvider.getY() - size /2f);
    }
}
