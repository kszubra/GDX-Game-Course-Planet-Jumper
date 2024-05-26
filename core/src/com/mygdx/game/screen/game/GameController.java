package com.mygdx.game.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.mygdx.game.common.FloatingScore;
import com.mygdx.game.common.GameManager;
import com.mygdx.game.common.GameState;
import com.mygdx.game.common.SoundListener;
import com.mygdx.game.config.GameConfig;
import com.mygdx.game.entity.Coin;
import com.mygdx.game.entity.Monster;
import com.mygdx.game.entity.Obstacle;
import com.mygdx.game.entity.Planet;
import com.mygdx.game.screen.menu.OverlayCallback;


public class GameController {

    // == constants ==
    private static final Logger log = new Logger(GameController.class.getName(), Logger.DEBUG);

    // == attributes ==
    private final SoundListener listener;

    private Planet planet;
    private Monster monster;

    private float monsterStartX;
    private float monsterStartY;

    private final Array<Coin> coins = new Array<Coin>();
    private final Pool<Coin> coinPool = Pools.get(Coin.class, 10);
    private float coinTimer;

    private final Array<Obstacle> obstacles = new Array<Obstacle>();
    private final Pool<Obstacle> obstaclePool = Pools.get(Obstacle.class, 10);
    private float obstacleTimer;

    private float startWaitTimer = GameConfig.START_WAIT_TIME;
    private float animationTime;

    private GameState gameState = GameState.MENU;
    private OverlayCallback callback;

    private final Array<FloatingScore> floatingScores = new Array<FloatingScore>();
    private Pool<FloatingScore> floatingScorePool = Pools.get(FloatingScore.class);

    // == constructors ==
    public GameController(SoundListener listener) {
        this.listener = listener;
        init();
    }

    // == init ==
    private void init() {
        planet = new Planet();
        planet.setPosition(
                GameConfig.WORLD_CENTER_X - GameConfig.PLANET_HALF_SIZE,
                GameConfig.WORLD_CENTER_Y - GameConfig.PLANET_HALF_SIZE
        );

        monsterStartX = GameConfig.WORLD_CENTER_X - GameConfig.MONSTER_HALF_SIZE;
        monsterStartY = GameConfig.WORLD_CENTER_Y + GameConfig.PLANET_HALF_SIZE;


        monster = new Monster();
        monster.setPosition(monsterStartX, monsterStartY);

        callback = new OverlayCallback() {
            @Override
            public void home() {
                gameState = GameState.MENU;
            }

            @Override
            public void ready() {
                restart();
                gameState = GameState.READY;
            }
        };
    }

    // == public methods ==
    public void update(float delta) {

        if (gameState.isReady() && startWaitTimer > 0) {
            startWaitTimer -= delta;

            if (startWaitTimer <= 0) {
                gameState = GameState.PLAYING;
            }
        }

        if (!gameState.isPlaying()) {
            return;
        }

        animationTime += delta;

        GameManager.INSTANCE.updateDisplayScore(delta);

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && monster.isWalking()) {
            listener.jump();
            monster.jump();
        }

        monster.update(delta);

        for (Obstacle obstacle : obstacles) {
            obstacle.update(delta);
        }

        for (Coin coin : coins) {
            coin.update(delta);
        }

        for (int i = 0; i < floatingScores.size; i++) {
            FloatingScore floatingScore = floatingScores.get(i);
            floatingScore.update(delta);

            if (floatingScore.isFinished()) {
                floatingScorePool.free(floatingScore);
                floatingScores.removeIndex(i);
            }
        }

        spawnObstacles(delta);
        spawnCoins(delta);

        checkCollision();
    }

    public Planet getPlanet() {
        return planet;
    }

    public Monster getMonster() {
        return monster;
    }

    public Array<Coin> getCoins() {
        return coins;
    }

    public Array<Obstacle> getObstacles() {
        return obstacles;
    }

    public Array<FloatingScore> getFloatingScores() {
        return floatingScores;
    }

    public float getStartWaitTimer() {
        return startWaitTimer;
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public GameState getGameState() {
        return gameState;
    }

    public OverlayCallback getCallback() {
        return callback;
    }

    public void restart() {
        coinPool.freeAll(coins);
        coins.clear();

        obstaclePool.freeAll(obstacles);
        obstacles.clear();

        floatingScorePool.freeAll(floatingScores);
        floatingScores.clear();

        monster.reset();
        monster.setPosition(monsterStartX, monsterStartY);

        GameManager.INSTANCE.updateHighScore();
        GameManager.INSTANCE.reset();
        startWaitTimer = GameConfig.START_WAIT_TIME;
        animationTime = 0f;
        gameState = GameState.READY;
    }

    // == private methods ==
    private void spawnCoins(float delta) {
        coinTimer += delta;

        if (coinTimer < GameConfig.COIN_SPAWN_TIME) {
            return;
        }

        coinTimer = 0;

        if (coins.size == 0) {
            addCoins();
        }
    }

    private void addCoins() {
        int count = MathUtils.random(GameConfig.MAX_COINS);

        for (int i = 0; i < count; i++) {
            float randomAngle = MathUtils.random(360f);

            boolean canSpawn = !isCoinNearBy(randomAngle)
                    && !isMonsterNearBy(randomAngle);

            if (canSpawn) {
                Coin coin = coinPool.obtain();

                if (isObstacleNearBy(randomAngle)) {
                    coin.setOffset(true);
                }

                coin.setAngleDeg(randomAngle);
                coins.add(coin);
            }
        }
    }

    private boolean isCoinNearBy(float angle) {
        // check that there are no coins nearby min dist
        for (Coin coin : coins) {
            float angleDeg = coin.getAngleDeg();

            float diff = Math.abs(Math.abs(angleDeg) - Math.abs(angle));

            if (diff < GameConfig.MIN_ANG_DIST) {
                return true;
            }
        }

        return false;
    }

    private boolean isMonsterNearBy(float angle) {
        float playerDiff = Math.abs(Math.abs(monster.getAngleDeg()) - Math.abs(angle));

        if (playerDiff < GameConfig.MIN_ANG_DIST) {
            return true;
        }

        return false;
    }

    private boolean isObstacleNearBy(float angle) {
        angle = Math.abs(angle);

        for (Obstacle obstacle : obstacles) {
            float angleDeg = Math.abs(obstacle.getAngleDeg());

            float diff = Math.abs(angleDeg - angle);

            if (diff < GameConfig.MIN_ANG_DIST) {
                return true;
            }
        }

        return false;
    }

    private void spawnObstacles(float delta) {
        obstacleTimer += delta;

        if (obstacleTimer < GameConfig.OBSTACLE_SPAWN_TIME) {
            return;
        }

        obstacleTimer = 0;

        if (obstacles.size == 0) {
            addObstacles();
        }
    }

    private void addObstacles() {
        int count = MathUtils.random(2, GameConfig.MAX_OBSTACLES);

        for (int i = 0; i < count; i++) {
            float randomAngle = monster.getAngleDeg() - i * GameConfig.MIN_ANG_DIST - MathUtils.random(60, 80);

            boolean canSpawn = !isObstacleNearBy(randomAngle)
                    && !isCoinNearBy(randomAngle)
                    && !isMonsterNearBy(randomAngle);

            if (canSpawn) {
                Obstacle obstacle = obstaclePool.obtain();
                obstacle.setAngleDeg(randomAngle);
                obstacles.add(obstacle);
            }
        }
    }

    private void checkCollision() {
        // player <-> coins
        for (int i = 0; i < coins.size; i++) {
            Coin coin = coins.get(i);

            if (Intersector.overlaps(monster.getBounds(), coin.getBounds())) {
                GameManager.INSTANCE.addScore(GameConfig.COIN_SCORE);
                addFloatingScore(GameConfig.COIN_SCORE);
                coinPool.free(coin);
                coins.removeIndex(i);
                listener.hitCoin();
            }
        }

        // player <-> obstacle
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle obstacle = obstacles.get(i);

            if (Intersector.overlaps(monster.getBounds(), obstacle.getSensor())) {
                GameManager.INSTANCE.addScore(GameConfig.OBSTACLE_SCORE);
                addFloatingScore(GameConfig.OBSTACLE_SCORE);
                obstaclePool.free(obstacle);
                obstacles.removeIndex(i);
            } else if (Intersector.overlaps(monster.getBounds(), obstacle.getBounds())) {
                listener.lose();
                gameState = GameState.GAME_OVER;
            }
        }
    }

    private void addFloatingScore(int score) {
        FloatingScore floatingScore = floatingScorePool.obtain();
        floatingScore.setStartPosition(GameConfig.HUD_WIDTH / 2f, GameConfig.HUD_HEIGHT / 2f);
        floatingScore.setScore(score);
        floatingScores.add(floatingScore);
    }
}
