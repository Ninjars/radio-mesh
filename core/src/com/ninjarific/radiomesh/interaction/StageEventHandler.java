package com.ninjarific.radiomesh.interaction;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ninjarific.radiomesh.NodeSelectionHandler;
import com.ninjarific.radiomesh.nodes.IPositionProvider;
import com.ninjarific.radiomesh.scene.NodeActor;

public class StageEventHandler<T extends IPositionProvider> implements IStageEventHandler {

    private final NodeSelectionHandler<T> nodeSelectionHandler;

    public StageEventHandler(NodeSelectionHandler<T> nodeSelectionHandler) {
        this.nodeSelectionHandler = nodeSelectionHandler;
    }

    @Override
    public void onNodeTouched(Actor actor) {
        if (actor instanceof NodeActor) {
            @SuppressWarnings("unchecked")
            NodeActor<T> nodeActor = (NodeActor) actor;
            nodeSelectionHandler.onNodeSelected(nodeActor.getDataProvider());
        }
    }
}
