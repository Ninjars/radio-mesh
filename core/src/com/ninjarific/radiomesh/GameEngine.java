package com.ninjarific.radiomesh;

import com.ninjarific.radiomesh.forcedirectedgraph.NodeForceCalculator;
import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.nodes.Bounds;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.radialgraph.NodeData;
import com.ninjarific.radiomesh.radialgraph.RadialNode;
import com.ninjarific.radiomesh.utils.listutils.Change;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private static final String TAG = GameEngine.class.getSimpleName();

    private static final double FORCE_FACTOR = 1000;
    private static final float NODE_FORCE_FACTOR = 0.1f;
    private static final float NODE_OPTIMAL_DISTANCE = 10;

    private final Random random = new Random();

    private final NodeForceCalculator<RadialNode> forceCalculator;
    private final MutableBounds nodeBounds = new MutableBounds();
    private QuadTree<RadialNode> quadTree;
    private List<RadialNode> datasetNodes = new ArrayList<>();

    public GameEngine() {
        forceCalculator = new NodeForceCalculator<>(NODE_FORCE_FACTOR, NODE_OPTIMAL_DISTANCE);
    }

    public List<RadialNode> getNodes() {
        return datasetNodes;
    }

    public List<Change<RadialNode>> updateNodes(List<Change<NodeData>> nodes) {
        List<Change<RadialNode>> radialNodeChanges = new ArrayList<>(nodes.size());
        for (Change<NodeData> change : nodes) {
            switch (change.getType()) {
                case ADD:
                    RadialNode node = createNode(change.getValue());
                    datasetNodes.add(node);
                    radialNodeChanges.add(new Change<>(Change.Type.ADD, node));
                    break;
                case REMOVE:
                    RadialNode removeNode = findNode(change.getValue());
                    if (removeNode != null) {
                        datasetNodes.remove(removeNode);
                        radialNodeChanges.add(new Change<>(Change.Type.REMOVE, removeNode));
                    }
                    break;
                case UPDATE:
                    RadialNode updateNode = findNode(change.getValue());
                    if (updateNode != null) {
                        updateNode.updateData(change.getValue());
                    }
                    break;
            }
        }

        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for (RadialNode node : datasetNodes) {
            updateNodeBounds(node, nodeBounds);
        }
        double gutter = Math.max(nodeBounds.getWidth() * 0.1, nodeBounds.getHeight() * 0.1);
        nodeBounds.left -= gutter;
        nodeBounds.top -= gutter;
        nodeBounds.right += gutter;
        nodeBounds.bottom += gutter;

        return radialNodeChanges;
    }

    private RadialNode createNode(NodeData value) {
        return new RadialNode(value, random.nextFloat() * NODE_OPTIMAL_DISTANCE, random.nextFloat() * NODE_OPTIMAL_DISTANCE);
    }

    private RadialNode findNode(NodeData value) {
        for (RadialNode node : datasetNodes) {
            if (node.getData().equals(value)) {
                return node;
            }
        }
        return null;
    }

    public void updateGameState(float deltaTimeSeconds) {
        performStateUpdate(deltaTimeSeconds * 1000.0);
    }

    private void performStateUpdate(double timeDelta) {
        double maxDim = Math.max(nodeBounds.getWidth(), nodeBounds.getHeight());
        Bounds squareBounds = new Bounds(nodeBounds.left, nodeBounds.top, nodeBounds.left + maxDim, nodeBounds.top + maxDim);
        quadTree = new QuadTree<>(0, squareBounds);
        quadTree.insertAll(datasetNodes);

        for (RadialNode node : datasetNodes) {
            forceCalculator.repelNode(node, quadTree);
            for (RadialNode neighbour : node.getNeighbours()) {
                forceCalculator.attractNodes(node, neighbour);
            }
        }

        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for (RadialNode node : datasetNodes) {
            node.updatePosition(1);//FORCE_FACTOR / timeDelta);
            updateNodeBounds(node, nodeBounds);
        }
        double gutter = Math.max(nodeBounds.getWidth() * 0.1, nodeBounds.getHeight() * 0.1);
        nodeBounds.left -= gutter;
        nodeBounds.top -= gutter;
        nodeBounds.right += gutter;
        nodeBounds.bottom += gutter;
    }

    private static void updateNodeBounds(RadialNode node, MutableBounds nodeBounds) {
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
