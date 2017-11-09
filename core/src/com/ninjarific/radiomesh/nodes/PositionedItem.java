package com.ninjarific.radiomesh.nodes;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;

import java.util.List;

public interface PositionedItem<T> {
    float getX();
    float getY();
    void setContainingLeaf(QuadTree quadTree);
    void updatePosition(int timeDeltaMs);
    void updateNeighbours(List<T> nodes);
    void addForce(float fx, float fy);
}
