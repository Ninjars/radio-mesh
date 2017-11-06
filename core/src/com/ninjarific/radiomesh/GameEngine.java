package com.ninjarific.radiomesh;

import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.nodes.Bounds;
import com.ninjarific.radiomesh.nodes.ForceConnectedNode;
import com.ninjarific.radiomesh.nodes.ForceConnection;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.nodes.NodeForceCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class GameEngine {
    private static final String TAG = GameEngine.class.getSimpleName();

    private static final double FORCE_FACTOR = 1000;
    private static final double NODE_FORCE_FACTOR = 0.1;
    private static final double NODE_OPTIMAL_DISTANCE = 10;

    private final NodeForceCalculator forceCalculator;
    private final MutableBounds nodeBounds = new MutableBounds();
    private QuadTree<ForceConnectedNode> quadTree;
    private List<ForceConnectedNode> datasetNodes = Collections.emptyList();
    private List<ForceConnection> uniqueConnections = Collections.emptyList();

    public GameEngine() {
        forceCalculator = new NodeForceCalculator(NODE_FORCE_FACTOR, NODE_OPTIMAL_DISTANCE);
    }

    public List<ForceConnectedNode> getNodes() {
        return datasetNodes;
    }

    public void setNodes(List<ForceConnectedNode> nodes) {
        this.datasetNodes = nodes;
        final HashSet<ForceConnection> connections = new HashSet<>(nodes.size());
        for (int i = 0; i < datasetNodes.size(); i++) {
            ForceConnectedNode node = datasetNodes.get(i);
            for (int j = 0; j < datasetNodes.size(); j++) {
                if (i != j && node.getNeighbours().contains(j)) {
                    connections.add(new ForceConnection(i, j));
                }
            }
        }
        uniqueConnections = new ArrayList<>(connections);
        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for (ForceConnectedNode node : datasetNodes) {
            updateNodeBounds(node, nodeBounds);
        }
        double gutter = Math.max(nodeBounds.getWidth() * 0.1, nodeBounds.getHeight() * 0.1);
        nodeBounds.left -= gutter;
        nodeBounds.top -= gutter;
        nodeBounds.right += gutter;
        nodeBounds.bottom += gutter;
    }

    public void updateGameState(float deltaTimeSeconds) {
        performStateUpdate(deltaTimeSeconds * 1000.0);
    }

    private void performStateUpdate(double timeDelta) {
        double maxDim = Math.max(nodeBounds.getWidth(), nodeBounds.getHeight());
        Bounds squareBounds = new Bounds(nodeBounds.left, nodeBounds.top, nodeBounds.left + maxDim, nodeBounds.top + maxDim);
        quadTree = new QuadTree<>(0, squareBounds);
        quadTree.insertAll(datasetNodes);

        for (ForceConnectedNode node : datasetNodes) {
            forceCalculator.repelNode(node, quadTree);
        }

        for (ForceConnection connection : uniqueConnections) {
            ForceConnectedNode nodeA = datasetNodes.get(connection.from);
            ForceConnectedNode nodeB = datasetNodes.get(connection.to);
            forceCalculator.attractNodes(nodeA, nodeB);
        }
        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for (ForceConnectedNode node : datasetNodes) {
            node.updatePosition(1);//FORCE_FACTOR / timeDelta);
            node.clearForce();
            updateNodeBounds(node, nodeBounds);
        }
        double gutter = Math.max(nodeBounds.getWidth() * 0.1, nodeBounds.getHeight() * 0.1);
        nodeBounds.left -= gutter;
        nodeBounds.top -= gutter;
        nodeBounds.right += gutter;
        nodeBounds.bottom += gutter;
    }

    private static void updateNodeBounds(ForceConnectedNode node, MutableBounds nodeBounds) {
        if (node.getX() < nodeBounds.left) {
            nodeBounds.left = node.getX();
        }
        if (node.getY() < nodeBounds.top) {
            nodeBounds.top = node.getY();
        }
        if (node.getX() > nodeBounds.right) {
            nodeBounds.right = node.getX();
        }
        if (node.getY() > nodeBounds.bottom) {
            nodeBounds.bottom = node.getY();
        }
    }

    public MutableBounds getBounds() {
        return nodeBounds;
    }
}
