package com.ninjarific.radiomesh.radialgraph;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.nodes.INode;

import java.util.ArrayList;
import java.util.List;

public class RadialNode implements INode {

    private final List<INode> neighbourNodes = new ArrayList<>();
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
    public void updateNeighbours(List<INode> nodes) {
        neighbourNodes.clear();
        neighbourNodes.addAll(nodes);
    }

    @Override
    public NodeData getData() {
        return nodeData;
    }

    @Override
    public void updateData(NodeData value) {
        nodeData = value;
    }

    @Override
    public List<INode> getNeighbours() {
        return neighbourNodes;
    }
}
