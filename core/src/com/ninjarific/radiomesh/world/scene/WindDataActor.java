package com.ninjarific.radiomesh.world.scene;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.ninjarific.radiomesh.Constants;
import com.ninjarific.radiomesh.world.data.WindData;

class WindDataActor extends Actor {
    private final WindData data;

    private TextureRegion textureRegion;

    public WindDataActor(WindData windData) {
        this.data = windData;
        setTouchable(Touchable.disabled);
        setWidth(1);
        setHeight(0.1f);
        setX(data.getX());
        setY(data.getY());
        setBounds(getX(), getY(), getWidth(), getHeight());
    }

    private static TextureRegion createTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 0.75f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        if (Constants.DEBUG_SHOW_WIND && textureRegion == null) {
            textureRegion = createTexture();
        }
        if (textureRegion != null) {
            batch.draw(textureRegion, getX() + getWidth() / 2f, getY() + getHeight() / 2f,
                    0, 0,
                    1, 0.1f,
                    1, 1, (float) Math.toDegrees(data.getDirection()));
        }
    }
}
