package com.ninjarific.radiomesh.forcedirectedgraph;

import com.ninjarific.radiomesh.nodes.*;

public class NodeForceCalculator<T extends PositionedItem> {
    private static final double TREE_INEQUALITY = 1.2;
    private static final double MIN_FORCE_THRESHOLD = 0.00001;
    private static final String LOGTAG = NodeForceCalculator.class.getSimpleName();

    private final double optimalDistance;
    private final double maxForce;
    private final double forceMagnitude;

    public NodeForceCalculator(double forceFactor, double optimalDistance) {
        this.optimalDistance = optimalDistance;
        forceMagnitude = forceFactor * optimalDistance * optimalDistance;
        maxForce = 10;
    }

    public void repelNode(T node, QuadTree<T> quadTree) {
        applyTreeForce(node, quadTree);
    }

    private void applyRepulsionForce(T node, double dx, double dy) {
        double mag = Math.sqrt(dx * dx + dy * dy);
        if (mag < MIN_FORCE_THRESHOLD) {
            return;
        }
        applyRepulsionForce(node, dx, dy, 1, mag);
    }

    private void applyRepulsionForce(T node, double dx, double dy, double multiplier, double magnitude) {
        double vx = dx / magnitude;
        double vy = dy / magnitude;

        double force = -Math.min(maxForce, Math.max(0, multiplier * forceMagnitude / magnitude));
        double fx = vx * force;
        double fy = vy * force;

        node.addForce((float) fx, (float) fy);
//        Timber.i("applyRepulsionForce " + node.getIndex() + " force: " + force + " multiplied by: " + multiplier + "   dx,dy: " + dx + ", " + dy + "   vx,vy: " + vx + ", " + vy + "   fx,fy: " + fx + ", " + fy);
//        Gdx.app.debug(LOGTAG, "repel," + node.getIndex() + "," + -force + "," + dx + "," + dy + "," + fx + "," + fy + "," + node.getX() + "," + node.getY());
    }

    private void applyTreeForce(T node, QuadTree<T> tree) {
        if (tree.isEmpty()) return;
        if (tree.isLeaf()) {
            for (T other : tree.getContainedItems()) {
                if (other != node) {
                    double dx = other.getX() - node.getX();
                    double dy = other.getY() - node.getY();
                    applyRepulsionForce(node, dx, dy);
                }
            }
        } else {
            double distance = quadTreeDistance(node, tree);
            if (leafIsFar(distance, tree)) {
                applyRepulsionForce(node, getDx(node, tree), getDy(node, tree), tree.getTotalContainedItemCount(), distance);
            } else {
                for (QuadTree<T> subtree : tree.getSubTrees()) {
                    applyTreeForce(node, subtree);
                }
            }
        }
    }

    private static <T extends PositionedItem> double getDx(T node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        return treeCenterOfGravity.x - node.getX();
    }

    private static <T extends PositionedItem> double getDy(T node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        return treeCenterOfGravity.y - node.getY();
    }

    private static <T extends PositionedItem> double quadTreeDistance(T node, QuadTree tree) {
        Coordinate treeCenterOfGravity = tree.getCenterOfGravity();
        if (treeCenterOfGravity == null) {
            return 0;
        }
        double dx = treeCenterOfGravity.x - node.getX();
        double dy = treeCenterOfGravity.y - node.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static boolean leafIsFar(double distance, QuadTree quadTree) {
        return quadTree.getBounds().getWidth() / distance <= TREE_INEQUALITY;
    }

    public void attractNodes(T nodeA, T nodeB) {
        double dx = nodeB.getX() - nodeA.getX();
        double dy = nodeB.getY() - nodeA.getY();

        double mag = Math.sqrt(dx * dx + dy * dy);
        if (mag <= MIN_FORCE_THRESHOLD) {
            return;
        }
        double vx = dx / mag;
        double vy = dy / mag;

        double force = Math.min(maxForce, Math.max(0, Math.log(mag) / optimalDistance));

        double fx = vx * force;
        double fy = vy * force;

        nodeA.addForce((float) fx, (float) fy);
        nodeB.addForce((float) -fx, (float) -fy);
//        Gdx.app.debug(LOGTAG, "attract," + nodeA.getIndex() + "," + force + "," + dx + "," + dy + "," + fx + "," + fy);
//        Gdx.app.debug(LOGTAG, "attract," + nodeB.getIndex() + "," + -force + "," + dx + "," + dy + "," + fx + "," + fy + ",nodeA," + nodeA.getX() + "," + nodeA.getY() + ",nodeB," + nodeB.getX() + "," + nodeB.getY());
    }
}
