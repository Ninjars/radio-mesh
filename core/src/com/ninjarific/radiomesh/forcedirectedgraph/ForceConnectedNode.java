package com.ninjarific.radiomesh.forcedirectedgraph;

import com.ninjarific.radiomesh.nodes.INode;
import com.ninjarific.radiomesh.radialgraph.NodeData;

import java.util.List;

public class ForceConnectedNode implements INode {
    private final int index;
    private List<Integer> neighbours;
    private float x;
    private float y;
    private double dx;
    private double dy;
    private QuadTree<ForceConnectedNode> containingLeaf;

    public ForceConnectedNode(int i, List<Integer> neighbours, float x, float y) {
        this.index = i;
        this.neighbours = neighbours;
        this.x = x;
        this.y = y;
    }

    public List<Integer> getNeighbourIds() {
        return neighbours;
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

    }

    @Override
    public void updateNeighbours(List nodes) {

    }


    public void clearForce() {
        dx = 0;
        dy = 0;
    }

    @Override
    public void addForce(float fx, float fy) {
        dx += fx;
        dy += fy;
    }

    @Override
    public List<INode> getNeighbours() {
        return null;
    }

    @Override
    public void updateData(NodeData value) {

    }

    @Override
    public NodeData getData() {
        return null;
    }

    public void applyForce(double fx, double fy) {
        x += fx;
        y += fy;
    }

    public void updatePosition(double forceFactor) {
        x = (float) (x + dx * forceFactor);
        y = (float) (y + dy * forceFactor);
    }

    public int getIndex() {
        return index;
    }

    public QuadTree<ForceConnectedNode> getContainingLeaf() {
        return containingLeaf;
    }

    @Override
    public String toString() {
        return "<ForceConnectedNode " + index + " " + x + ", " + y + ">";
    }
}
