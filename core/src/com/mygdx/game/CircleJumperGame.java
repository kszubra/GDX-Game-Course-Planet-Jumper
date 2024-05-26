package com.mygdx.game;


import com.mygdx.game.util.game.GameBase;
import com.mygdx.game.screen.loading.LoadingScreen;

public class CircleJumperGame extends GameBase {

	@Override
	public void postCreate() {
		setScreen(new LoadingScreen(this));
	}
}
