package com.ninjarific.radiomesh.nodes;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.radialgraph.NodeData;

import java.util.List;

public interface INode {
    float getX();
    float getY();
    void setContainingLeaf(QuadTree quadTree);
    void updatePosition(int timeDeltaMs);
    void updateNeighbours(List<INode> nodes);
    void addForce(float fx, float fy);
    List<INode> getNeighbours();
    void updateData(NodeData value);
    NodeData getData();
}
