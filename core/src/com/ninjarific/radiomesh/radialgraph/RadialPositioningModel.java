package com.ninjarific.radiomesh.radialgraph;

import com.ninjarific.radiomesh.Constants;

public class RadialPositioningModel {
    private float x;
    private float y;

    public RadialPositioningModel(int tier, int nodeCount, int tierIndex) {
        if (tier == 0) {
            x = 0;
            y = 0;
        } else {
            double angle = 2 * Math.PI / nodeCount * tierIndex - (Math.PI / 2.0);
            int h = tier * (Constants.NODE_WIDTH + Constants.TIER_RADIUS);
            x = (float) (Math.cos(angle) * h);
            y = (float) (Math.sin(angle) * h);
        }
    }

    float getX() {
        return x;
    }

    float getY() {
        return y;
    }

    /**
     * update current animations state based on frame time delta
     * @param timeDeltaMs elapsed ms since last frame
     */
    public void update(long timeDeltaMs) {
        // TODO: animate to target position if not already there
    }

    /**
     * register a coordinate from which this position should animate
     */
    void beginTransitionFrom(float x, float y) {
        // TODO: animate to target position if not already there
    }
}
