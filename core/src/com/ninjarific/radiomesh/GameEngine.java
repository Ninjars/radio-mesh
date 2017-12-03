package com.ninjarific.radiomesh;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.ninjarific.radiomesh.nodes.MutableBounds;
import com.ninjarific.radiomesh.radialgraph.NodeData;
import com.ninjarific.radiomesh.radialgraph.RadialNode;
import com.ninjarific.radiomesh.radialgraph.RadialPositioningModel;
import com.ninjarific.radiomesh.utils.listutils.Change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private static final String TAG = GameEngine.class.getSimpleName();

    private final Random random = new Random();

    private final MutableBounds nodeBounds = new MutableBounds();
    private final RadialNode rootNode; // is also included in datasetNodes
    private List<RadialNode> datasetNodes = new ArrayList<>();
    private List<RadialNode> datasetUpdate;

    GameEngine() {
        rootNode = new RadialNode(NodeData.ROOT_NODE, null,
                new ArrayList<>(), new RadialPositioningModel(0, 1, 0));
        datasetNodes.add(rootNode);
    }

    public List<RadialNode> getNodes() {
        return datasetNodes;
    }

    /**
     * TODO: support child tiers
     * Note that getNodes won't return changes from this list until the time performStateUpdate has been called
     */
    List<Change<RadialNode>> updateNodes(List<Change<NodeData>> nodes) {
        // sort node changes to have updates first to help maintain node
        // ordering of updated nodes and reduce visual changes when adding and removing nodes
        Collections.sort(nodes, (left, right) -> {
            if (left.getType() == right.getType()) {
                return left.getValue().getBssid().compareTo(right.getValue().getBssid());
            } else if (left.getType() == Change.Type.UPDATE) {
                return -1;
            } else {
                return 0;
            }
        });

        List<Change<RadialNode>> radialNodeChanges = new ArrayList<>(nodes.size());
        List<RadialNode> update = new ArrayList<>(datasetNodes);

        int magnitudeOfChange = 0;
        for (Change<NodeData> change : nodes) {
            switch (change.getType()) {
                case ADD:
                    magnitudeOfChange++;
                    break;
                case REMOVE:
                    magnitudeOfChange--;
                    break;
                default:
                    break;
            }
        }

        int sizeOfTier = datasetNodes.size() - 1 + magnitudeOfChange;
        int tierIndex = 0;
        for (Change<NodeData> change : nodes) {
            switch (change.getType()) {
                case ADD:
                    RadialPositioningModel position = new RadialPositioningModel(1, sizeOfTier, tierIndex);
                    RadialNode node = createNode(change.getValue(), position);
                    update.add(node);
                    radialNodeChanges.add(new Change<>(Change.Type.ADD, node));
                    tierIndex++;
                    break;
                case REMOVE:
                    RadialNode removeNode = findNode(change.getValue());
                    if (removeNode != null) {
                        update.remove(removeNode);
                        radialNodeChanges.add(new Change<>(Change.Type.REMOVE, removeNode));
                    }
                    break;
                case UPDATE:
                    RadialNode updateNode = findNode(change.getValue());
                    if (updateNode != null) {
                        RadialPositioningModel updatePosition = new RadialPositioningModel(1, sizeOfTier, tierIndex);
                        updateNode.setPosition(updatePosition);
                        updateNode.updateData(change.getValue());
                        tierIndex++;
                    }
                    break;
            }
        }
        datasetUpdate = update;
        updateBounds(1);
        return radialNodeChanges;
    }

    private void updateBounds(int numberOfTiers) {
        int size = (1 + numberOfTiers * 2) * Constants.NODE_WIDTH
                + (2 * numberOfTiers) * Constants.TIER_RADIUS
                + 2 * Constants.SCREEN_MARGIN;
        int halfSize = size / 2;
        nodeBounds.set(-halfSize, -halfSize, halfSize, halfSize);
    }

    private RadialNode createNode(NodeData value, RadialPositioningModel position) {
        // TODO: support child tiers
        return new RadialNode(value, rootNode, Collections.emptyList(), position);
    }

    private RadialNode findNode(NodeData value) {
        for (RadialNode node : datasetNodes) {
            if (node.getData().equals(value)) {
                return node;
            }
        }
        return null;
    }

    void updateGameState(float deltaTimeSeconds) {
        performStateUpdate((long) (deltaTimeSeconds * 1000));
    }

    private void performStateUpdate(long timeDelta) {
        if (datasetUpdate != null) {
            datasetNodes = datasetUpdate;
            datasetUpdate = null;
            for (RadialNode node : datasetNodes) {
                // update node neighbours now that dataset has been updated
                List<RadialNode> childNodes = Stream.of(datasetNodes)
                        .filter(otherNode -> node.equals(otherNode.getParentNode()))
                        .collect(Collectors.toList());
                node.setChildNodes(childNodes);
            }
        }

//        for (IPositionProvider node : datasetNodes) {datasetNodes
//            forceCalculator.repelNode(node, quadTree);
//            for (IPositionProvider neighbour : node.getNeighbours()) {
//                forceCalculator.attractNodes(node, neighbour);
//            }
//        }

        for (RadialNode node : datasetNodes) {
            node.update(timeDelta);
        }
    }

    MutableBounds getBounds() {
        return nodeBounds;
    }
}
