package com.ninjarific.radiomesh.radialgraph;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.nodes.PositionedItem;

import java.util.ArrayList;
import java.util.List;

public class RadialNode implements PositionedItem<RadialNode> {

    private final List<RadialNode> neighbourNodes = new ArrayList<>();
    private NodeData nodeData;
    private float x;
    private float y;
    private float dx;
    private float dy;
    private QuadTree containingLeaf;

    public RadialNode(NodeData nodeData, float initialX, float initialY) {
        this.nodeData = nodeData;
        this.x = initialX;
        this.y = initialY;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setContainingLeaf(QuadTree quadTree) {
        this.containingLeaf = quadTree;
    }

    @Override
    public void updatePosition(int timeDeltaMs) {
        x += dx;
        y += dy;
        dx = 0;
        dy = 0;
    }

    @Override
    public void addForce(float fx, float fy) {
        dx += fx;
        dy += fy;
    }

    @Override
    public void updateNeighbours(List<RadialNode> nodes) {
        neighbourNodes.clear();
        neighbourNodes.addAll(nodes);
    }

    public NodeData getData() {
        return nodeData;
    }

    public void updateData(NodeData value) {
        nodeData = value;
    }

    public List<RadialNode> getNeighbours() {
        return neighbourNodes;
    }
}
