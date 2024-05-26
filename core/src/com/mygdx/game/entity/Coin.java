package com.mygdx.game.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import com.mygdx.game.util.entity.EntityBase;
import com.mygdx.game.config.GameConfig;


public class Coin extends EntityBase implements Pool.Poolable {

    // == constants ==
    private static final float SCALE_MAX = 1.0f;

    // == attributes ==
    private float angleDeg;
    private boolean offset;
    private float scale;

    // == constructors ==
    public Coin() {
        setSize(GameConfig.COIN_SIZE, GameConfig.COIN_SIZE);
    }

    // == public methods ==
    public void update(float delta) {
        if (scale < SCALE_MAX) {
            scale += delta;
        }
    }

    public void setAngleDeg(float value) {
        angleDeg = value % 360;

        float radius = GameConfig.PLANET_HALF_SIZE;

        if (offset) {
            radius += GameConfig.COIN_SIZE;
        }

        float originX = GameConfig.WORLD_CENTER_X;
        float originY = GameConfig.WORLD_CENTER_Y;

        float newX = originX + MathUtils.cosDeg(-angleDeg) * radius;
        float newY = originY + MathUtils.sinDeg(-angleDeg) * radius;

        setPosition(newX, newY);
    }

    public void setOffset(boolean offset) {
        this.offset = offset;
    }

    public float getAngleDeg() {
        return angleDeg;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public void reset() {
        offset = false;
        scale = 0.0f;
    }
}
