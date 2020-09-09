/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter.
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SerializationException;
import edu.cornell.gdiac.assets.*;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.audio.MusicBuffer;
import edu.cornell.gdiac.audio.SoundBuffer;
import edu.cornell.gdiac.octoplasm.util.AudioController;
import edu.cornell.gdiac.octoplasm.util.LevelList;
import edu.cornell.gdiac.octoplasm.util.SaveGame;
import edu.cornell.gdiac.octoplasm.util.ScreenListener;


/**
 * Root class for a LibGDX.
 *
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However,
 * those classes are unique to each platform, while this class is the same across all
 * plaforms. In addition, this functions as the root class all intents and purposes,
 * and you would draw it as a root class in an architecture specification.
 */
public class GDXRoot extends Game implements ScreenListener {
	//=========================================================================
	//#region Fields
	/** The path to the save file for the game. */
	private static final String SAVE_FILE = "octoplasmSaves/SaveGame.json"; //TODO: decide on name for application preferences file
	/** Reference to the default cursor image for the mouse. */
	private static final String DEFAULT_CURSOR = "ui/cursors/cursorImage.png";
	/** Reference to the clicked cursor image for the mouse. */
	private static final String CLICKED_CURSOR = "ui/cursors/cursorImageClicked.png";
	/** Reference to the transparent cursor image for the mouse. */
	private final static String TRANSPARENT_CURSOR = "ui/cursors/transparentCursor.png";
	/** Reference to the wall cursor image for the mouse. */
	private final static String WALL_CURSOR = "ui/cursors/teleport_selector.png";
	/** Reference to the level list json file. */
	private static final String LEVEL_LIST = "levels/LevelList.json";

	/**  */
	private static final boolean DEBUGGING = false;

	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Player mode for the game menu screen (CONTROLLER CLASS) */
	private MenuMode menu;
	/** Player mode for the game level select screen (CONTROLLER CLASS) */
	private LevelSelectMode levelSelect;
	/** The Game Controller */
	private GameplayController gameController;
	/**  */
	private TransitionController transitionController;

	/** The default cursor image. */
	private Cursor defaultCursor;
	/** The clicked cursor image. */
	private Cursor clickedCursor;
	/** An image for making the cursor transparent. */
	private Cursor transparentCursor;
	/** The cursor for creating walls. */
	private Cursor wallCursor;

	/**  */
	private SaveGame saveGame;

	private Json json;
	//#endregion
	//=================================

	//=========================================================================
	//#region Constructors
	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() {
		// Start loading with the asset manager
		manager = new AssetManager();
		json = new Json();

		// Add font support to the asset manager
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
		manager.setLoader(SoundBuffer.class, new SoundBufferLoader());
		manager.setLoader(MusicBuffer.class, new MusicBufferLoader());
		manager.setLoader(AudioSource.class, new AudioSourceLoader());
	}
	//#endregion
	//=================================

	//=========================================================================
	//#region Application Methods
	/**
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas = new GameCanvas();

		//Create the mouse cursor
		Pixmap defaultCursorPixmap = new Pixmap(Gdx.files.internal(DEFAULT_CURSOR));
		Pixmap clickedCursorPixmap = new Pixmap(Gdx.files.internal(CLICKED_CURSOR));
		Pixmap transparentCursorPixmap = new Pixmap(Gdx.files.internal(TRANSPARENT_CURSOR));
		Pixmap wallCursorPixmap = new Pixmap(Gdx.files.internal(WALL_CURSOR));

		defaultCursor = Gdx.graphics.newCursor(defaultCursorPixmap, 0,defaultCursorPixmap.getHeight()/8);
		clickedCursor = Gdx.graphics.newCursor(clickedCursorPixmap, 0,clickedCursorPixmap.getHeight()/8);
		transparentCursor = Gdx.graphics.newCursor(transparentCursorPixmap, 0,transparentCursorPixmap.getHeight()/8);
		wallCursor = Gdx.graphics.newCursor(wallCursorPixmap, 0,wallCursorPixmap.getHeight()/8);

		defaultCursorPixmap.dispose();
		clickedCursorPixmap.dispose();
		transparentCursorPixmap.dispose();
		wallCursorPixmap.dispose();

		//Initialize the controllers and modes
		loading = new LoadingMode(canvas,manager,1);
		menu = new MenuMode(canvas, manager);
		levelSelect = new LevelSelectMode(canvas,manager);
		gameController = new GameplayController();
		transitionController = new TransitionController();
		gameController.setManager(manager);
		gameController.setCanvas(canvas);
		InputController.getInstance().setCanvas(canvas);

		//Start asynchronous preloading
		menu.preLoadContent(manager);
		loading.preLoadContent(manager);
		levelSelect.preLoadContent(manager);
		gameController.preLoadContent(manager);
		transitionController.preLoadContent(manager);
		AudioController.getInstance().setManager(manager);
		gameController.loadOctopusAssets();
		gameController.loadEnemyAssets();
		gameController.loadFont();
		gameController.loadFilmStrip();

		//Check game preferences
		checkPreferences();
		loadLevelList();

		//Start the loading screen
		loading.setScreenListener(this);
		setScreen(loading);
	}

	/**
	 * Checks the current save game file and applies quality of life preferences.
	 * If no Save Game exists, creates one in the specified folder.
	 */
	private void checkPreferences() {
		FileHandle fileHandle = Gdx.files.external(SAVE_FILE);
		if (fileHandle.exists()) {
			AudioController audio = AudioController.getInstance();
			//InputController input = InputController.getInstance();
			try {
				saveGame = json.fromJson(SaveGame.class, fileHandle);
				if (saveGame.isFullscreen) {
					DisplayMode monitorDisplayMode = Gdx.graphics.getDisplayMode(Gdx.graphics.getPrimaryMonitor());
					Gdx.graphics.setFullscreenMode(monitorDisplayMode);
				}

				audio.setMasterVolume(saveGame.masterVolume);
				audio.setSoundVolume(saveGame.sfxVolume);
				audio.setMusicVolume(saveGame.musicVolume);
				audio.setSoundActive(saveGame.soundActive);
			} catch (SerializationException e) {
				System.out.println("Save file was corrupted, making a new save file");
				saveGame = new SaveGame();
				json.setTypeName(null);
				json.setUsePrototypes(false);
				json.setIgnoreUnknownFields(true);
				json.setOutputType(JsonWriter.OutputType.json);
				json.toJson(saveGame, SaveGame.class, fileHandle);
			}
		} else {
			saveGame = new SaveGame();
			json.setTypeName(null);
			json.setUsePrototypes(false);
			json.setIgnoreUnknownFields(true);
			json.setOutputType(JsonWriter.OutputType.json);
			json.toJson(saveGame, SaveGame.class, fileHandle);
//			String s = json.toJson(saveGame, SaveGame.class);
//			System.out.println(s);
		}

		gameController.setSaveGame(saveGame);
		levelSelect.setSaveGame(saveGame);
		menu.setSaveGame(saveGame);
	}

	/**
	 * Load the level list
	 */
	private void loadLevelList() {
		Json json = new Json();
		LevelList levelList;
		try {
			levelList = json.fromJson(LevelList.class, Gdx.files.internal(LEVEL_LIST));
		} catch (SerializationException e) {
			e.printStackTrace();
			levelList = new LevelList();
		}

		if (DEBUGGING) {
			json.setTypeName(null);
			json.setUsePrototypes(false);
			json.setIgnoreUnknownFields(true);
			json.setOutputType(JsonWriter.OutputType.json);
			String s = json.toJson(levelList, LevelList.class);
//			System.out.println(s);
		}

		levelSelect.setLevelList(levelList);
		gameController.setLevelList(levelList);
	}

	/**
	 * Called when the Application is destroyed.
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Dispose mouse cursors
		defaultCursor.dispose();
		clickedCursor.dispose();
		transparentCursor.dispose();
		wallCursor.dispose();

		// Call dispose on our children
		setScreen(null);
		gameController.unloadContent(manager);
		gameController.dispose();
		menu.unloadContent(manager);
		menu.dispose();
		levelSelect.unloadContent(manager);
		levelSelect.dispose();
		transitionController.unloadContent(manager);
		transitionController.dispose();

		canvas.dispose();
		canvas = null;

		// Unload all of the resources
		manager.clear();
		manager.dispose();
		super.dispose();
	}

	/**
	 * Called when the Application is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to create().
	 *
	 * @param width The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.clear();
		canvas.resize(width, height);
		canvas.updateCamera();
		super.resize(width,height);
	}

	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			canvas.setStageAsInputProcessor();

			menu.loadContent(manager);
			menu.setScreenListener(this);
			menu.setCanvas(canvas);

			levelSelect.loadContent(manager);
			levelSelect.setScreenListener(this);
			levelSelect.setCanvas(canvas);

			gameController.loadContent(manager);
			gameController.setScreenListener(this);
			gameController.setCanvas(canvas);

			transitionController.loadContent(manager);
			transitionController.setScreenListener(this);
			transitionController.setCanvas(canvas);

			transitionController.screen1 = loading;
			transitionController.screen2 = menu;
			setScreen(transitionController);
		} else if (screen == menu) {
			switch (exitCode) {
			case (MenuMode.EXIT_QUIT):
				Gdx.app.exit();
				break;
			case (MenuMode.EXIT_SELECT):
				transitionController.screen1 = menu;
				transitionController.screen2 = levelSelect;
				setScreen(transitionController);
				break;
			case (MenuMode.EXIT_EDITOR):
				Gdx.input.setCursorCatched(true);
				gameController.setLevelEdit(true);
				gameController.reset();
				transitionController.screen1 = menu;
				transitionController.screen2 = gameController;
				setScreen(transitionController);
				break;
			}
		} else if (screen == levelSelect) {
			if (exitCode == LevelSelectMode.EXIT_BACK) {
				transitionController.rightToLeft = true;
				transitionController.screen1 = levelSelect;
				transitionController.screen2 = menu;
				setScreen(transitionController);
			} else {
				if (gameController.setCurrentLevel(exitCode)) {
					gameController.setLevelEdit(false);
					if (gameController.reset()) {
						transitionController.screen1 = levelSelect;
						transitionController.screen2 = gameController;
						canvas.resetCameraToWorldPos();
						setScreen(transitionController);
					}
				} else {
					levelSelect.setLoadFailedText();
				}
			}
		} else if (screen == gameController) {
			switch (exitCode) {
				case (GameplayController.EXIT_NEXT):
				case (GameplayController.EXIT_PREV):
					Gdx.input.setCursorCatched(false);
					setCursor(CursorState.DEFAULT);
					if (gameController.isLevelEdit()) {
						transitionController.rightToLeft = true;
						transitionController.screen1 = gameController;
						transitionController.screen2 = menu;
						setScreen(transitionController);
					} else {
						transitionController.rightToLeft = true;
						transitionController.screen1 = gameController;
						transitionController.screen2 = levelSelect;
						setScreen(transitionController);
					}
					break;
				case (GameplayController.EXIT_LEVEL):
					Gdx.input.setCursorCatched(false);
					transitionController.rightToLeft = true;
					transitionController.screen1 = gameController;
					transitionController.screen2 = levelSelect;
					setScreen(transitionController);
					break;
				case (GameplayController.EXIT_MENU):
					Gdx.input.setCursorCatched(false);
					transitionController.rightToLeft = true;
					transitionController.screen1 = gameController;
					transitionController.screen2 = menu;
					setScreen(transitionController);
					break;
				case (GameplayController.EXIT_POPULATE_FAILED):
					Gdx.input.setCursorCatched(false);
					levelSelect.setPopulateFailedText();
					transitionController.rightToLeft = true;
					transitionController.screen1 = gameController;
					transitionController.screen2 = levelSelect;
					setScreen(transitionController);
					break;
				case (GameplayController.EXIT_QUIT):
					Gdx.app.exit();
					break;
			}
		} else if (screen == transitionController) {
			//Dispose of loading screen
			if (transitionController.screen2 == loading) {
				loading.dispose();
				loading = null;
			}

			//Catch the cursor going to the game screen
			if (transitionController.screen2 == gameController)
				Gdx.input.setCursorCatched(true);

			// Update the selected level when going to levelSelect Screen
			if (transitionController.screen1 == gameController && transitionController.screen2 == levelSelect) {
				setScreen(transitionController.screen2);
				levelSelect.setSelectedLevel(gameController.getCurrentLevel());
			} else {
				setScreen(transitionController.screen2);
			}
		}
	}

	/**
	 * Changes the current cursor for this game.
	 *
	 * @param cursorState The cursor state to be changed to.
	 */
	public void setCursor(CursorState cursorState) {
		switch (cursorState) {
			case DEFAULT:
				Gdx.graphics.setCursor(defaultCursor);
				break;
			case CLICKED:
				Gdx.graphics.setCursor(clickedCursor);
				break;
			case WALL:
				Gdx.graphics.setCursor(wallCursor);
				break;
			case TRANSPARENT:
				Gdx.graphics.setCursor(transparentCursor);
				break;
		}
	}

	/**
	 * Updates the current level of the current save file. If the save game file is ahead of the parameter,
	 * nothing is changed.
	 *
	 * @param nextLevel The new level that the play is currently on.
	 */
	@Override
	public void updateSaveGameLevel(int nextLevel) {
		//If the save file should be updated,
		if (saveGame.currentLevel < nextLevel) {
			//Update the save file and the classes that need to use it
			saveGame.currentLevel = nextLevel;
			levelSelect.setSaveGame(saveGame);
			gameController.setSaveGame(saveGame);
			menu.setSaveGame(saveGame);

			//Enable or disable levels based on new save file
			levelSelect.updateDisabledLevels();

			//Update the save file json
			json.setTypeName(null);
			json.setUsePrototypes(false);
			json.setIgnoreUnknownFields(true);
			json.setOutputType(JsonWriter.OutputType.json);
			json.toJson(saveGame, SaveGame.class, Gdx.files.external(SAVE_FILE));
		}
	}

	@Override
	public void resetSaveGameLevel() {
		saveGame.currentLevel = 101;
		levelSelect.setSaveGame(saveGame);
		gameController.setSaveGame(saveGame);
		menu.setSaveGame(saveGame);

		//Enable or disable levels based on new save file
		levelSelect.updateDisabledLevels();

		//Update the save file json
		json.setTypeName(null);
		json.setUsePrototypes(false);
		json.setIgnoreUnknownFields(true);
		json.setOutputType(JsonWriter.OutputType.json);
		json.toJson(saveGame, SaveGame.class, Gdx.files.external(SAVE_FILE));
	}

	/**
	 *
	 */
	@Override
	public void updatePreferences() {
		AudioController audio = AudioController.getInstance();

		//Update sound settings
		saveGame.soundActive = audio.isSoundActive();
		saveGame.masterVolume = audio.getMasterVolume();
		saveGame.sfxVolume = audio.getSoundVolume();
		saveGame.musicVolume = audio.getMusicVolume();
		saveGame.isFullscreen = Gdx.graphics.isFullscreen();

		//Update save game in all classes that use it
		levelSelect.setSaveGame(saveGame);
		gameController.setSaveGame(saveGame);
		menu.setSaveGame(saveGame);

		//Update the save file json
		json.setTypeName(null);
		json.setUsePrototypes(false);
		json.setIgnoreUnknownFields(true);
		json.setOutputType(JsonWriter.OutputType.json);
		json.toJson(saveGame, SaveGame.class, Gdx.files.external(SAVE_FILE));
	}

	//#endregion
	//=================================
}
