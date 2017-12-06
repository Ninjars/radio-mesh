package com.ninjarific.radiomesh.scan.radialgraph;

import com.ninjarific.radiomesh.Constants;

public class RadialPositioningModel {
    private static final long ANIMATION_DURTAION = Constants.NODE_MOVE_ANIMATION_MS;
    private final float realX;
    private final float realY;
    private float x;
    private float y;
    private float animOriginX;
    private float animOriginY;
    private long remainingAnimationDuration;

    public RadialPositioningModel(int tier, int nodeCount, int tierIndex) {
        if (tier == 0) {
            realX = 0;
            realY = 0;
        } else {
            double angle = 2 * Math.PI / nodeCount * tierIndex - (Math.PI / 2.0);
            int h = tier * (Constants.NODE_WIDTH + Constants.TIER_RADIUS);
            realX = (float) (Math.cos(angle) * h);
            realY = (float) (Math.sin(angle) * h);
        }
        beginTransitionFrom(0, 0);
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
        if (remainingAnimationDuration <= 0) {
            return;
        }
        float animationProgress = getAnimationProgressFraction();
        if (remainingAnimationDuration <= timeDeltaMs) {
            x = realX;
            y = realY;
            remainingAnimationDuration = 0;
        } else {
            x = animOriginX + ((realX - animOriginX) * animationProgress);
            y = animOriginY + ((realY - animOriginY) * animationProgress);
            remainingAnimationDuration -= timeDeltaMs;
        }
    }

    private float getAnimationProgressFraction() {
        return Math.min(1, Math.max(0, 1 - (remainingAnimationDuration / (float) ANIMATION_DURTAION)));
    }

    /**
     * register a coordinate from which this position should animate
     */
    void beginTransitionFrom(float x, float y) {
        this.animOriginX = x;
        this.animOriginY = y;
        this.x = x;
        this.y = y;
        remainingAnimationDuration = ANIMATION_DURTAION;
    }
}
