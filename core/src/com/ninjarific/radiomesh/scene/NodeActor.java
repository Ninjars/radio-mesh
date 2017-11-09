package com.ninjarific.radiomesh.scene;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ninjarific.radiomesh.nodes.INode;

import java.util.Random;

public class NodeActor extends Actor {
    private static final Random COLOR_RANDOM = new Random();
    private static final float TEXTURE_SIZE = 3;
    private final INode node;
    private Texture texture;

    public NodeActor(INode node) {
        this.node = node;
    }

    public INode getNode() {
        return node;
    }

    private Texture createTexture() {
        Pixmap pixmap = new Pixmap((int) TEXTURE_SIZE, (int) TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(COLOR_RANDOM.nextFloat(), COLOR_RANDOM.nextFloat(), COLOR_RANDOM.nextFloat(), 1);
        int size = (int) Math.floor(TEXTURE_SIZE/2f);
        pixmap.fillCircle(size, size, size);
        Texture newTexture = new Texture(pixmap);
        pixmap.dispose();
        return newTexture;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (texture == null) {
            texture = createTexture();
        }
        batch.draw(texture, node.getX() - TEXTURE_SIZE/2f, node.getY() - TEXTURE_SIZE/2f);
    }
}
