package com.mygdx.game.screen.menu;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.assets.ButtonStyleNames;
import com.mygdx.game.assets.RegionNames;
import com.mygdx.game.common.GameManager;

public class GameOverOverlay extends Table {

    // == attributes ==
    private final OverlayCallback callback;

    private Label scoreLabel;
    private Label highScoreLabel;

    // == constructors ==
    public GameOverOverlay(Skin skin, OverlayCallback callback) {
        super(skin);
        this.callback = callback;
        init();
    }

    // == init ==
    private void init() {
        defaults().pad(20);

        Image gameOverImage = new Image(getSkin(), RegionNames.GAME_OVER);

        // score table
        Table scoreTable = new Table(getSkin());
        scoreTable.defaults().pad(10);
        scoreTable.setBackground(RegionNames.PANEL);

        scoreTable.add("SCORE: ").row();
        scoreLabel = new Label("", getSkin());
        scoreTable.add(scoreLabel).row();

        scoreTable.add("BEST: ").row();
        highScoreLabel = new Label("", getSkin());
        scoreTable.add(highScoreLabel);

        scoreTable.center();

        // button table
        Table buttonTable = new Table();

        ImageButton homeButton = new ImageButton(getSkin(), ButtonStyleNames.HOME);
        homeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                callback.home();
            }
        });

        ImageButton restartButton = new ImageButton(getSkin(), ButtonStyleNames.RESTART);
        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                callback.ready();
            }
        });

        buttonTable.add(homeButton).left().expandX();
        buttonTable.add(restartButton).right().expandX();

        add(gameOverImage).row();
        add(scoreTable).row();
        add(buttonTable).grow().center();

        center();
        setFillParent(true);
        pack();

        updateLabels();
    }

    public void updateLabels() {
        scoreLabel.setText("" + GameManager.INSTANCE.getScore());
        highScoreLabel.setText("" + GameManager.INSTANCE.getHighScore());
    }
}
