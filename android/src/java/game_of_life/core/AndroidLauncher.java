package game_of_life.core;

import clojure.lang.RT;
import clojure.lang.Symbol;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.Game;

public class AndroidLauncher extends AndroidApplication {
	public void onCreate (android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
          RT.var("clojure.core", "require").invoke(Symbol.intern("game-of-life.core"));
		try {
			Game game = (Game) RT.var("game-of-life.core", "game-of-life").deref();
			initialize(game);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
