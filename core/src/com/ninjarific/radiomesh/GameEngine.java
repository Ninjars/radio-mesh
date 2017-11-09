package com.ninjarific.radiomesh;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.ninjarific.radiomesh.forcedirectedgraph.NodeForceCalculator;
import com.ninjarific.radiomesh.forcedirectedgraph.QuadTree;
import com.ninjarific.radiomesh.nodes.Bounds;
import com.ninjarific.radiomesh.nodes.INode;
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

    private final NodeForceCalculator<INode> forceCalculator;
    private final MutableBounds nodeBounds = new MutableBounds();
    private QuadTree<INode> quadTree;
    private List<INode> datasetNodes = new ArrayList<>();
    private List<INode> datasetUpdate;

    public GameEngine() {
        forceCalculator = new NodeForceCalculator<>(NODE_FORCE_FACTOR, NODE_OPTIMAL_DISTANCE);
    }

    public List<INode> getNodes() {
        return datasetNodes;
    }

    /**
     * Note that getNodes won't return changes from this list until the time performStateUpdate has been called
     */
    public List<Change<INode>> updateNodes(List<Change<NodeData>> nodes) {
        List<Change<INode>> radialNodeChanges = new ArrayList<>(nodes.size());
        List<INode> update = new ArrayList<>(datasetNodes);
        for (Change<NodeData> change : nodes) {
            switch (change.getType()) {
                case ADD:
                    INode node = createNode(change.getValue());
                    update.add(node);
                    radialNodeChanges.add(new Change<>(Change.Type.ADD, node));
                    break;
                case REMOVE:
                    INode removeNode = findNode(change.getValue());
                    if (removeNode != null) {
                        update.remove(removeNode);
                        radialNodeChanges.add(new Change<>(Change.Type.REMOVE, removeNode));
                    }
                    break;
                case UPDATE:
                    INode updateNode = findNode(change.getValue());
                    if (updateNode != null) {
                        updateNode.updateData(change.getValue());
                    }
                    break;
            }
        }

        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for (INode node : update) {
            updateNodeBounds(node, nodeBounds);
        }
        double gutter = Math.max(nodeBounds.getWidth() * 0.1, nodeBounds.getHeight() * 0.1);
        nodeBounds.left -= gutter;
        nodeBounds.top -= gutter;
        nodeBounds.right += gutter;
        nodeBounds.bottom += gutter;
        datasetUpdate = update;
        return radialNodeChanges;
    }

    private INode createNode(NodeData value) {
        return new RadialNode(value, random.nextFloat() * NODE_OPTIMAL_DISTANCE, random.nextFloat() * NODE_OPTIMAL_DISTANCE);
    }

    private INode findNode(NodeData value) {
        for (INode node : datasetNodes) {
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

        if (datasetUpdate != null) {
            datasetNodes = datasetUpdate;
            datasetUpdate = null;
            for (INode node : datasetNodes) {
                // update node neighbours now that dataset has been updated
                List<INode> neighbours = Stream.of(datasetNodes).filter(value -> node != value).collect(Collectors.toList());
                node.updateNeighbours(neighbours);
            }
        }
        quadTree.insertAll(datasetNodes);

        for (INode node : datasetNodes) {
            forceCalculator.repelNode(node, quadTree);
            for (INode neighbour : node.getNeighbours()) {
                forceCalculator.attractNodes(node, neighbour);
            }
        }

        nodeBounds.set(Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        for (INode node : datasetNodes) {
            node.updatePosition(1);//FORCE_FACTOR / timeDelta);
            updateNodeBounds(node, nodeBounds);
        }
        double gutter = Math.max(nodeBounds.getWidth() * 0.1, nodeBounds.getHeight() * 0.1);
        nodeBounds.left -= gutter;
        nodeBounds.top -= gutter;
        nodeBounds.right += gutter;
        nodeBounds.bottom += gutter;
    }

    private static void updateNodeBounds(INode node, MutableBounds nodeBounds) {
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
