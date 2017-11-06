package com.ninjarific.radiomesh.scene;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ninjarific.radiomesh.nodes.ForceConnectedNode;

import java.util.Random;

public class NodeActor extends Actor {
    private static final Random colorRandom = new Random();
    private final ForceConnectedNode node;
    private final Texture texture;

    public NodeActor(ForceConnectedNode node) {
        this.node = node;
        this.texture = createTexture();
    }

    private Texture createTexture() {
        Pixmap pixmap = new Pixmap(3, 3, Pixmap.Format.RGBA8888);
        pixmap.setColor(colorRandom.nextFloat(), colorRandom.nextFloat(), colorRandom.nextFloat(), 1);
        pixmap.fillCircle(1, 1, 1);
        return new Texture(pixmap);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, node.getX(), node.getY());
    }
}
