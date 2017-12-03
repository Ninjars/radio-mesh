package com.ninjarific.radiomesh.interaction;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ninjarific.radiomesh.RadioMeshGame;
import com.ninjarific.radiomesh.nodes.IPositionProvider;
import com.ninjarific.radiomesh.scene.NodeActor;

public class StageEventHandler<T extends IPositionProvider> implements IStageEventHandler {

    private final RadioMeshGame game;

    public StageEventHandler(RadioMeshGame game) {
        this.game = game;
    }

    @Override
    public boolean onNodeTouched(Actor actor) {
        if (actor instanceof NodeActor) {
            NodeActor nodeActor = (NodeActor) actor;
            T data = (T) nodeActor.getDataProvider();
            return game.onNodeSelected(data);
        }
        return false;
    }
}
