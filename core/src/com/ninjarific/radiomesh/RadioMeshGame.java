package com.ninjarific.radiomesh;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ninjarific.radiomesh.nodes.ForceConnectedNode;

import java.util.List;

public class RadioMeshGame extends ApplicationAdapter {
	private static final String TAG = RadioMeshGame.class.getSimpleName();
	private SpriteBatch batch;
	private Texture img;
	private List<ForceConnectedNode> data;

    @Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

	public void setData(List<ForceConnectedNode> data) {
		Gdx.app.log(TAG, "setData " + data);
		this.data = data;
	}
}
