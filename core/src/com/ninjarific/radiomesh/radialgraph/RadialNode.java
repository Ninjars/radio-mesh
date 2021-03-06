package com.ninjarific.radiomesh.radialgraph;

import com.ninjarific.radiomesh.nodes.IPositionProvider;

import java.util.List;

public class RadialNode implements IPositionProvider {

    private final List<RadialNode> childNodes;
    private RadialNode parentNode; // null for root node
    private NodeData nodeData;
    private RadialPositioningModel position;

    /**
     * Radial graph node
     *
     * @param nodeData node metadata
     * @param parentNode node one step up the tree - null for root node
     * @param childNodes list of nodes one step down the tree from this node
     * @param position positional information
     */
    public RadialNode(NodeData nodeData, RadialNode parentNode,
                      List<RadialNode> childNodes, RadialPositioningModel position) {
        this.nodeData = nodeData;
        this.parentNode = parentNode;
        this.childNodes = childNodes;
        this.position = position;
    }

    @Override
    public float getX() {
        return position.getX();
    }

    @Override
    public float getY() {
        return position.getY();
    }

    public void update(long timeDeltaMs) {
        position.update(timeDeltaMs);
    }

    public void setPosition(RadialPositioningModel newPosition) {
        newPosition.beginTransitionFrom(position.getX(), position.getY());
        position = newPosition;
    }

    public void setChildNodes(List<RadialNode> nodes) {
        childNodes.clear();
        childNodes.addAll(nodes);
    }

    public NodeData getData() {
        return nodeData;
    }

    public void updateData(NodeData value) {
        nodeData = value;
    }

    public RadialNode getParentNode() {
        return parentNode;
    }
}
