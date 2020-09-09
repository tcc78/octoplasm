package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.octoplasm.util.AudioController;
import edu.cornell.gdiac.octoplasm.util.SaveGame;
import edu.cornell.gdiac.octoplasm.util.ScreenListener;

import java.util.Random;

/**
 * The class that handles the main menu screen in the game.
 *
 * @author Stephen Chin
 */
public class MenuMode extends SuperController implements Screen {
    //================================================================================
    //#region Fields
    /** The current State of the Menu Mode. Because Menu Mode will connect different menu
     *  screens, we use an enum to keep track of it's current status. */
    private enum CurrentState {
        /** The main menu. */
        MAIN_MENU,
        /** The settings menu. */
        SETTINGS,
        /** The credits section of the menu. */
        CREDITS
    }

        //===========================================================
    //#region Bubble Creation
    /**
     * Class related to a background bubble group. Because bubble groups have a large amount of variance in
     * their creation and management, this class was made for easier update and draw loops.
     */
    private class BubbleGroup {
        /** The bubble group texture region to draw. */
        public TextureRegion bubbleGroup;
        /** The x and y dimensions of this bubble group */
        public float[] dimension;
        /** The x and y positions of this bubble group */
        public float[] position;
        /** The size variance of this bubble group */
        public float dimensionVar;
        /** The color to draw this bubble group to the canvas with */
        public Color color;
        /** The selected index of the bubble group texture within our texture array. */
        public int bubbleIndex;
        /** The previous width of the canvas. Used when resizing positions */
        public int storedWidth;

        /**
         * Creates a new bubble group, placed within the bounds of the screen.
         * Alpha, size, position, and texture are all varied.
         */
        public BubbleGroup() {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int offset = (int) (width * BUBBLE_GROUP_OFFSET_PERCENT);

            this.bubbleIndex = r.nextInt(NUMBER_OF_BUBBLE_TEXTURES);
            this.bubbleGroup = bubbleGroupTextures[bubbleIndex];
            this.position = new float[2];
            this.dimension = new float[2];
            storedWidth = width;

            //Variance in alpha values
            this.color = new Color(Color.WHITE).sub(0,0,0,(float) Math.pow(9,r.nextFloat()-1));

            //Place either on screen or below screen
            int yPositionOffset = (int) (height/BUBBLE_ANIMATION_TIME * (r.nextFloat() * BUBBLE_GROUP_UNDER_OFFSET));
            position[0] = r.nextInt(width - offset*2) + offset;
            position[1] = r.nextInt(height + yPositionOffset) - yPositionOffset;

            resize();
        }

        /**
         * Updates the position of this bubble group. If we reached the top of the screen, brings the bubble group
         * down below the screen to rise up once again, changes the alpha value, and changes the dimensions
         * of the bubble group.
         *
         * @param delta Time in seconds since the last animation frame
         */
        public void updatePosition(float delta) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int offset = (int) (width* BUBBLE_GROUP_OFFSET_PERCENT);

            position[1] += height/BUBBLE_ANIMATION_TIME * delta;
            if (position[1] - (dimension[1] / 2) > height) {
                //Change alpha value
                color.set(Color.WHITE).sub(0,0,0,(float) Math.pow(9,r.nextFloat()-1));
                dimensionVar = -(r.nextFloat() * BUBBLE_GROUP_VARIANCE);
                resize();
                //Put below the screen in a random spot
                position[0] = r.nextInt(width - offset*2) + offset;
                position[1] = -(height/BUBBLE_ANIMATION_TIME * (r.nextFloat() * BUBBLE_GROUP_UNDER_OFFSET)) - dimension[1]/2f;
            }
        }

        /**
         * Resizes the bubble group to the current values in {@link #bubbleGroupDimensions}. If that array has
         * not been changed yet, this will not work as intended.
         */
        public void resize() {
            //Change dimension
            dimension[0] = bubbleGroupDimensions[2*bubbleIndex] + (bubbleGroupDimensions[2*bubbleIndex] * dimensionVar);
            dimension[1] = bubbleGroupDimensions[2*bubbleIndex + 1] + (bubbleGroupDimensions[2*bubbleIndex + 1] * dimensionVar);

            position[0] = position[0] * canvas.getWidth() / storedWidth;
            storedWidth = canvas.getWidth();
        }

        /**
         * Draws the bubble group to canvas.
         *
         * @param canvas Reference to the game canvas.
         */
        public void draw(GameCanvas canvas) {
            canvas.draw(bubbleGroup, color, bubbleGroup.getRegionWidth()/4f, bubbleGroup.getRegionHeight()/4f,
                    position[0], position[1], dimension[0], dimension[1]);
        }
    }

    /** Reference to the bubble group textures. Needs a number and a .png. */
    private static final String BUBBLE_GROUP_BASE_PATH = "ui/main_menu/bubble_group";

    /** A Random Object used for variance in bubble animations. */
    private static final Random r = new Random();

    /** The number of bubbles to be animated on screen. */
    public static final int NUMBER_OF_BUBBLES = 14;
    /** The number of bubble textures currently in the assets folder. */
    public static final int NUMBER_OF_BUBBLE_TEXTURES = 6;

    /** The amount of time in seconds that it takes for the bubbles to reach the top of the screen. */
    private static final float BUBBLE_ANIMATION_TIME = 16f;
    /** The offset amount (in percent of width) from the edge of the screen for the bubbles to be placed. */
    private static final float BUBBLE_GROUP_OFFSET_PERCENT = 0.05f;
    /** The offset amount (in seconds) from the bottom of the screen to replace the bubbles. */
    private static final float BUBBLE_GROUP_UNDER_OFFSET = 12f;
    /** The high bound of screen bubble size variance. */
    private static final float BUBBLE_GROUP_VARIANCE = 0.6f;

    /** The scaled dimensions of the bubble groups. */
    private float[] bubbleGroupDimensions = new float[NUMBER_OF_BUBBLE_TEXTURES * 2];
    /** The collection of textures for the bubble groups. */
    private TextureRegion[] bubbleGroupTextures = new TextureRegion[NUMBER_OF_BUBBLE_TEXTURES];
    /** All bubble groups currently being animated */
    private Array<BubbleGroup> screenBubbles = new Array<>();

    //#endregion
        //======================================================

    /** Exit code for quitting out of the whole game. */
    public static final int EXIT_QUIT = 0;
    /** Exit code for going to the level select screen. */
    public static final int EXIT_SELECT = 1;
    /** Exit code for going to the level editor. */
    public static final int EXIT_EDITOR = 2;

    /** The intended pixel width of the game screen, stored as a variable for resizing. */
    private static final float INTENDED_WIDTH = 1920f;
    /** The intended pixel height of the game screen, stored as a variable for resizing. */
    private static final float INTENDED_HEIGHT = 1080f;
    /** The intended pixel space between menu buttons. */
    private static final float INTENDED_BUTTON_SPACING = 52.53f / 2f;
    /** Modifier for the button sizes from the original texture images. Used alongside window scaling. */
    private static final float BUTTON_SIZE_MODIFIER = 0.8f;
    /** The intended pixel space from the title to the menu buttons. */
    private static final float INTENDED_TITLE_TO_BUTTONS_SPACING = 71f;
    /** The intended pixel space from the title to the top of the screen. */
    private static final float INTENDED_TITLE_TOP_SPACING = 123f;
    /** The amount of time in seconds that it takes for the octopus sprite movement to loop. */
    private static final float ANIMATION_PERIOD = 6f;

    /** The intended pixel amplitude of the octopus sprite movement. */
    private static final float INTENDED_ANIMATION_AMPLITUDE = 25f;
    /** The intended pixel position of the Fight octopus. */
    private static final float[] INTENDED_FIGHT_POS = new float[] {260f + 175f/2f, 359.88f + 309.64f/2f};
    /** The intended pixel position of the Flight octopus. */
    private static final float[] INTENDED_FLIGHT_POS = new float[] {1423.5f, 119f + 396f/2f};
    /** The intended pixel position of the Fold octopus. */
    private static final float[] INTENDED_FOLD_POS = new float[] {1701f, 659f};
    /** The intended pixel padding (bottom, left) of the group logo. */
    private static final float[] INTENDED_LOGO_PADDING = new float[] {90f, 91f};

    //Menu UI Paths
    /** Reference to the Main Menu background texture. */
    private static final String BACKGROUND = "backgrounds/main_menu.png";
    /** Reference to the game title texture. */
    private static final String TITLE = "ui/main_menu/title.png";
    /** Reference to the level select button texture. */
    private static final String LEVEL_SELECT_BUTTON = "ui/main_menu/level_select_button.png";
    /** Reference to the credits button texture. */
    private static final String CREDITS_BUTTON = "ui/main_menu/credits_button.png";
    /** Reference to the settings button texture. */
    private static final String SETTINGS_BUTTON = "ui/main_menu/settings_button.png";
    /** Reference to the exit button. */
    private static final String EXIT_BUTTON = "ui/main_menu/exit_button.png";
    /** Reference to the group logo texture. */
    private static final String GROUP_LOGO = "ui/main_menu/group_logo.png";
    /** Reference to the Fight octopus texture. */
    private static final String FIGHT = "ui/main_menu/fight.png";
    /** Reference to the Flight octopus texture. */
    private static final String FLIGHT = "ui/main_menu/flight.png";
    /** Reference to the Fold octopus texture. */
    private static final String FOLD = "ui/main_menu/fold.png";

    //Settings UI Paths
    /**  */
    private static final String SETTINGS_TITLE = "ui/main_menu/settings/title.png";
    /**  */
    private static final String SETTINGS = "ui/main_menu/settings/settings.png";
    /**  */
    private static final String SETTINGS_TOGGLE_ON = "ui/main_menu/settings/onbutton.png";
    /**  */
    private static final String SETTINGS_TOGGLE_OFF = "ui/main_menu/settings/offbutton.png";
    /**  */
    private static final String SLIDER_KNOB = "ui/main_menu/settings/knob.png";
    /**  */
    private static final String SLIDER_BACKGROUND = "ui/main_menu/settings/background.png";
    /**  */
    private static final String SLIDER_FILL = "ui/main_menu/settings/fill.png";
    /**  */
    private static final String CLEAR_SAVE = "ui/main_menu/settings/clear_save.png";
    /**  */
    private static final String DIVIDER = "ui/main_menu/settings/divider.png";
    /**  */
    private static final String SAVE_CLEARED = "ui/main_menu/settings/save_cleared.png";
    /**  */
    private static final String SOUND = "ui/main_menu/settings/sound.png";
    /**  */
    private static final String FULLSCREEN = "ui/main_menu/settings/fullscreen.png";
    /**  */
    private static final String MASTER_VOLUME = "ui/main_menu/settings/mastervol.png";
    /**  */
    private static final String SFX = "ui/main_menu/settings/sfx.png";
    /**  */
    private static final String MUSIC = "ui/main_menu/settings/music.png";
    /**  */
    private static final String X_ICON = "ui/main_menu/settings/xicon.png";
    /**  */
    private static final String CREDITS = "ui/main_menu/credits.png";

    //Audio Paths
    /** Reference to the menu click forward sound. */
    private static final String MENU_CLICK_FORWARD = "sounds/ui/menu_click_forward.wav";
    /** Reference to the menu click backward sound. */
    private static final String MENU_CLICK_BACKWARD = "sounds/ui/menu_click_backward.wav";
    /** Reference to the level select mouse over sound. */
    private static final String BUTTON_MOUSEOVER = "sounds/ui/button_mouseover.wav";
    /** Reference to the main menu music. */
    private static final String MENU_MUSIC = "music/main_menu.ogg";
    /**  */
    private static final String FOLEY = "music/foley/underwater_ambiance.ogg";

    /** AssetManager to be loading in the background. */
    private AssetManager manager;
    /** Reference to GameCanvas created by the root. */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done. */
    private ScreenListener listener;
    /** Track all loaded assets (for unloading purposes). */
    private Array<String> assets;

    /** The amplitude of the octopus animations, scaled to our current window. */
    private float animationAmplitude;
    /** The x variable for sin function used in the octopus animation. */
    private float animationVar = 0;

    /** The scaled position of the Fight octopus animation. */
    private float[] fightPos = new float[2];
    /** The scaled dimensions of the Fight octopus animation. */
    private float[] fightDimensions = new float[2];
    /** The scaled position of the Flight octopus animation. */
    private float[] flightPos = new float[2];
    /** The scaled dimensions of the Flight octopus animation. */
    private float[] flightDimensions = new float[2];
    /** The scaled position of the Fold octopus animation. */
    private float[] foldPos = new float[2];
    /** The scaled dimensions of the Fold octopus animation. */
    private float[] foldDimensions = new float[2];

    /** The background of the Main Menu screen. */
    private Texture background;
    /** The texture of the Fight octopus */
    private TextureRegion fight;
    /** The texture of the Flight octopus */
    private TextureRegion flight;
    /** The texture of the Fold octopus */
    private TextureRegion fold;

    /** Whether or not this is an active controller. */
    private boolean active;

    /** The main main table. */
    private Table mainTable;
    /** The settings table. */
    private Table settingsTable;
    /** The credits table. */
    private Table creditsTable;

    private ImageButton clearSave;

    /**  */
    private SaveGame saveGame;
    /**  */
    private CurrentState currentState = CurrentState.MAIN_MENU;

    private Slider masterVolSlider;
    private Slider sfxVolSlider;
    private Slider musicVolSlider;
    //#endregion
    //=================================

    //================================================================================
    //#region Constructors

    /**
     * Creates a new Menu Mode.
     *
     * @param canvas The game canvas to be drawn on
     * @param manager The AssetManager to load in the background
     */
    public MenuMode(GameCanvas canvas, AssetManager manager) {
        this.manager = manager;
        this.canvas = canvas;
        assets = new Array<>();
    }
    //#endregion
    //=================================

    //================================================================================
    //#region Getters and Setters
    /**
     * Returns true if this is the active screen
     *
     * @return true if this is the active screen
     */
    public boolean isActive( ) {
        return active;
    }

    /**
     * Returns the canvas associated with this controller
     *
     * The canvas is shared across all controllers
     *
     * @return the canvas associated with this controller
     */
    public GameCanvas getCanvas() {
        return canvas;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**  */
    public void setSaveGame(SaveGame saveGame) {
        this.saveGame = saveGame;
    }
    //#endregion
    //=================================

    //================================================================================
    //#region Asset Loading
    /**
     * Preloads the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void preLoadContent(AssetManager manager) {
        //Master Table Assets
        manager.load(TITLE, Texture.class);
        assets.add(TITLE);
        manager.load(BACKGROUND, Texture.class);
        assets.add(BACKGROUND);
        manager.load(LEVEL_SELECT_BUTTON, Texture.class);
        assets.add(LEVEL_SELECT_BUTTON);
        manager.load(SETTINGS_BUTTON, Texture.class);
        assets.add(SETTINGS_BUTTON);
        manager.load(CREDITS_BUTTON, Texture.class);
        assets.add(CREDITS_BUTTON);
        manager.load(EXIT_BUTTON, Texture.class);
        assets.add(EXIT_BUTTON);
        manager.load(GROUP_LOGO, Texture.class);
        assets.add(GROUP_LOGO);
        manager.load(FIGHT, Texture.class);
        assets.add(FIGHT);
        manager.load(FLIGHT, Texture.class);
        assets.add(FLIGHT);
        manager.load(FOLD, Texture.class);
        assets.add(FOLD);

        //Settings Table Assets
        manager.load(SETTINGS_TITLE, Texture.class);
        assets.add(SETTINGS_TITLE);
        manager.load(SETTINGS, Texture.class);
        assets.add(SETTINGS);
        manager.load(SETTINGS_TOGGLE_ON, Texture.class);
        assets.add(SETTINGS_TOGGLE_ON);
        manager.load(SETTINGS_TOGGLE_OFF, Texture.class);
        assets.add(SETTINGS_TOGGLE_OFF);
        manager.load(SLIDER_KNOB, Texture.class);
        assets.add(SLIDER_KNOB);
        manager.load(SLIDER_BACKGROUND, Texture.class);
        assets.add(SLIDER_BACKGROUND);
        manager.load(SLIDER_FILL, Texture.class);
        assets.add(SLIDER_FILL);
        manager.load(CLEAR_SAVE, Texture.class);
        assets.add(CLEAR_SAVE);
        manager.load(SAVE_CLEARED, Texture.class);
        assets.add(SAVE_CLEARED);
        manager.load(DIVIDER, Texture.class);
        assets.add(DIVIDER);
        manager.load(SOUND, Texture.class);
        assets.add(SOUND);
        manager.load(FULLSCREEN, Texture.class);
        assets.add(FULLSCREEN);
        manager.load(MASTER_VOLUME, Texture.class);
        assets.add(MASTER_VOLUME);
        manager.load(SFX, Texture.class);
        assets.add(SFX);
        manager.load(MUSIC, Texture.class);
        assets.add(MUSIC);
        manager.load(X_ICON, Texture.class);
        assets.add(X_ICON);

        manager.load(CREDITS, Texture.class);
        assets.add(CREDITS);

        //Bubbles
        for (int i = 1; i <= NUMBER_OF_BUBBLE_TEXTURES; i++) {
            manager.load(BUBBLE_GROUP_BASE_PATH + i + ".png", Texture.class);
            assets.add(BUBBLE_GROUP_BASE_PATH + i + ".png");
        }

        //Sounds
        manager.load(MENU_CLICK_FORWARD, Sound.class);
        assets.add(MENU_CLICK_FORWARD);

        manager.load(MENU_CLICK_BACKWARD, Sound.class);
        assets.add(MENU_CLICK_BACKWARD);

        manager.load(BUTTON_MOUSEOVER, Sound.class);
        assets.add(BUTTON_MOUSEOVER);

        //Music
        manager.load(MENU_MUSIC, AudioSource.class);
        assets.add(MENU_MUSIC);

        manager.load(FOLEY, AudioSource.class);
        assets.add(FOLEY);
    }

    /**
     * Loads the assets for this mode.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(AssetManager manager) {
        AudioController audio = AudioController.getInstance();

        background = manager.get(BACKGROUND, Texture.class);
        fight = new TextureRegion(manager.get(FIGHT, Texture.class));
        flight = new TextureRegion(manager.get(FLIGHT, Texture.class));
        fold = new TextureRegion(manager.get(FOLD, Texture.class));

        audio.allocateSound(manager, MENU_CLICK_FORWARD);
        audio.allocateSound(manager, MENU_CLICK_BACKWARD);
        audio.allocateSound(manager, BUTTON_MOUSEOVER);

        resizeAndRepositionOctopi();
        resizeAndRepositionBubbles();
        createMenuMasterTable();
        createSettingsMasterTable();
        createCreditsTable();
    }

    /**
     * Recalculates the positions and sizes of the octopus sprites on the screen.
     */
    private void resizeAndRepositionOctopi() {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        fightPos[0] = INTENDED_FIGHT_POS[0]/INTENDED_WIDTH * width;
        fightPos[1] = INTENDED_FIGHT_POS[1]/INTENDED_HEIGHT * height;
        fightDimensions[0] = Math.min(fight.getRegionWidth()/INTENDED_WIDTH * width, fight.getRegionWidth());
        fightDimensions[1] = Math.min(fight.getRegionHeight()/INTENDED_HEIGHT * height, fight.getRegionHeight());

        flightPos[0] = INTENDED_FLIGHT_POS[0]/INTENDED_WIDTH * width;
        flightPos[1] = INTENDED_FLIGHT_POS[1]/INTENDED_HEIGHT * height;
        flightDimensions[0] = Math.min(flight.getRegionWidth()/INTENDED_WIDTH * width, flight.getRegionWidth());
        flightDimensions[1] = Math.min(flight.getRegionHeight()/INTENDED_HEIGHT * height, flight.getRegionHeight());

        foldPos[0] = INTENDED_FOLD_POS[0]/INTENDED_WIDTH * width;
        foldPos[1] = INTENDED_FOLD_POS[1]/INTENDED_HEIGHT * height;
        foldDimensions[0] = Math.min(fold.getRegionWidth()/INTENDED_WIDTH * width, fold.getRegionWidth());
        foldDimensions[1] = Math.min(fold.getRegionHeight()/INTENDED_HEIGHT * height, fold.getRegionHeight());

        animationAmplitude = INTENDED_ANIMATION_AMPLITUDE/INTENDED_HEIGHT * height;
    }

    /**
     * Recalculates the positions of the bubble group textures and either creates new bubble groups, or
     * resizes the bubble groups.
     */
    private void resizeAndRepositionBubbles() {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        //Calculate base bubble group dimensions
        for (int i = 0; i < NUMBER_OF_BUBBLE_TEXTURES; i++) {
            String path = BUBBLE_GROUP_BASE_PATH + (i+1) + ".png";
            bubbleGroupTextures[i] = new TextureRegion(manager.get(path, Texture.class));
            bubbleGroupDimensions[(2*i)] = Math.min(bubbleGroupTextures[i].getRegionWidth()/INTENDED_WIDTH * width,
                    bubbleGroupTextures[i].getRegionWidth());
            bubbleGroupDimensions[(2*i) + 1] = Math.min(bubbleGroupTextures[i].getRegionHeight()/INTENDED_HEIGHT * height,
                    bubbleGroupTextures[i].getRegionHeight());
        }

        //Create or Resize Bubble Groups
        if (screenBubbles.isEmpty()) {
            for (int i = 0; i < NUMBER_OF_BUBBLES; i++)
                screenBubbles.add(new BubbleGroup());
        } else {
            for (BubbleGroup bubbleGroup : screenBubbles)
                bubbleGroup.resize();
        }
    }

        //===========================================================
    //#region Table Creation
    /**
     * Creates and populates the master main menu table with the proper values.
     */
    private void createMenuMasterTable() {
        final MenuMode menu = this;
        final AudioController audio = AudioController.getInstance();
        ImageButton.ImageButtonStyle buttonStyle;
        Texture buttonTexture;

        //Create Table
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.center();

        //Title
        TextureRegionDrawable titleImage = new TextureRegionDrawable(manager.get(TITLE, Texture.class));
        titleImage.setMinWidth(titleImage.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        titleImage.setMinHeight(titleImage.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image title = new Image(titleImage, Scaling.fit);

        //Menu Buttons
        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonTexture = manager.get(LEVEL_SELECT_BUTTON, Texture.class);
        buttonStyle.imageUp = new TextureRegionDrawable(buttonTexture);
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth() * BUTTON_SIZE_MODIFIER/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight() * BUTTON_SIZE_MODIFIER/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver = new TextureRegionDrawable(buttonTexture).tint(Color.LIGHT_GRAY);
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageOver.getMinWidth() * BUTTON_SIZE_MODIFIER/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageOver.getMinHeight() * BUTTON_SIZE_MODIFIER/INTENDED_HEIGHT * canvas.getHeight());
        final ImageButton playButton = new ImageButton(buttonStyle);

        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonTexture = manager.get(SETTINGS_BUTTON, Texture.class);
        buttonStyle.imageUp = new TextureRegionDrawable(buttonTexture);
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth() * BUTTON_SIZE_MODIFIER/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight() * BUTTON_SIZE_MODIFIER/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver = new TextureRegionDrawable(buttonTexture).tint(Color.LIGHT_GRAY);
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageOver.getMinWidth() * BUTTON_SIZE_MODIFIER/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageOver.getMinHeight() * BUTTON_SIZE_MODIFIER/INTENDED_HEIGHT * canvas.getHeight());
        final ImageButton settingsButton = new ImageButton(buttonStyle);

        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonTexture = manager.get(CREDITS_BUTTON, Texture.class);
        buttonStyle.imageUp = new TextureRegionDrawable(buttonTexture);
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth() * BUTTON_SIZE_MODIFIER/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight() * BUTTON_SIZE_MODIFIER/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver = new TextureRegionDrawable(buttonTexture).tint(Color.LIGHT_GRAY);
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageOver.getMinWidth() * BUTTON_SIZE_MODIFIER/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageOver.getMinHeight() * BUTTON_SIZE_MODIFIER/INTENDED_HEIGHT * canvas.getHeight());
        final ImageButton creditsButton = new ImageButton(buttonStyle);

        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonTexture = manager.get(EXIT_BUTTON, Texture.class);
        buttonStyle.imageUp = new TextureRegionDrawable(buttonTexture);
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth() * BUTTON_SIZE_MODIFIER/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight() * BUTTON_SIZE_MODIFIER/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver = new TextureRegionDrawable(buttonTexture).tint(Color.LIGHT_GRAY);
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageOver.getMinWidth() * BUTTON_SIZE_MODIFIER/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageOver.getMinHeight() * BUTTON_SIZE_MODIFIER/INTENDED_HEIGHT * canvas.getHeight());
        final ImageButton exitButton = new ImageButton(buttonStyle);

        //Button Listeners
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("playButtonClick", MENU_CLICK_FORWARD, false);
                listener.exitScreen(menu, EXIT_SELECT);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!playButton.isChecked() && !playButton.isDisabled() && pointer == -1) {
                    audio.playSound("playButtonOver", BUTTON_MOUSEOVER, false);
                }
            }
        });
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("settingsButtonClick", MENU_CLICK_FORWARD, false);
                setCurrentState(CurrentState.SETTINGS);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!settingsButton.isChecked() && !settingsButton.isDisabled() && pointer == -1) {
                    audio.playSound("settingsButtonOver", BUTTON_MOUSEOVER, false);
                }
            }
        });
        creditsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //TODO: Credits Screen
                audio.playSound("creditsButtonClick", MENU_CLICK_FORWARD, false);
                setCurrentState(CurrentState.CREDITS);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!creditsButton.isChecked() && !creditsButton.isDisabled() && pointer == -1) {
                    audio.playSound("creditsButtonOver", BUTTON_MOUSEOVER, false);
                }
            }
        });
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("exitButtonClick", MENU_CLICK_FORWARD, false);
                listener.exitScreen(menu, EXIT_QUIT);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!creditsButton.isChecked() && !creditsButton.isDisabled() && pointer == -1) {
                    audio.playSound("exitButtonOver", BUTTON_MOUSEOVER, false);
                }
            }
        });

        //Fill Table with Buttons
        Table buttonsTable = new Table();
        buttonsTable.top();
        buttonsTable.defaults().padBottom(Value.percentHeight(INTENDED_BUTTON_SPACING/INTENDED_HEIGHT, mainTable));

        buttonsTable.add(playButton);
        buttonsTable.row();
        buttonsTable.add(settingsButton);
        buttonsTable.row();
        buttonsTable.add(creditsButton);
        buttonsTable.row();
        buttonsTable.add(exitButton).padBottom(0);

        //Add Group Logo
        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(GROUP_LOGO, Texture.class));
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        ImageButton groupLogo = new ImageButton(buttonStyle);

//        groupLogo.addListener(new ClickListener() {
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                audio.playSound("logoButtonClick", MENU_CLICK_FORWARD, false);
//                listener.exitScreen(menu, EXIT_EDITOR); //Currently set to exit to the level editor
//            }
//        });

        //Fill main Table
        mainTable.add(title).expandX()
                .padBottom(Value.percentHeight(INTENDED_TITLE_TO_BUTTONS_SPACING/INTENDED_HEIGHT, mainTable))
                .padTop(Value.percentHeight(INTENDED_TITLE_TOP_SPACING/INTENDED_HEIGHT, mainTable));
        mainTable.row();
        mainTable.add(buttonsTable).expand();
        mainTable.row();
        mainTable.add(groupLogo).left().bottom()
                .padBottom(Value.percentHeight(INTENDED_LOGO_PADDING[0]/INTENDED_HEIGHT, mainTable))
                .padLeft(Value.percentHeight(INTENDED_LOGO_PADDING[1]/INTENDED_WIDTH, mainTable));

//        mainTable.setDebug(true, true);
    }

    /**
     * Creates and populates the master settings table with the proper values.
     */
    private void createSettingsMasterTable() {
        //Reusable variables
        final AudioController audio = AudioController.getInstance();
        ImageButton.ImageButtonStyle buttonStyle;
        Texture buttonTexture;

        settingsTable = new Table();
        settingsTable.setFillParent(true);
        settingsTable.center()
                .padLeft(Value.percentWidth(400/INTENDED_WIDTH, settingsTable));
        //4 columns, 3 main rows
        //2 subtables (left and right)
        //leftsubtable:
        // 3 rows, 2 columns
        //rightsubtable:
        // 6 rows, 1 column

        //Title
        TextureRegionDrawable titleImage = new TextureRegionDrawable(manager.get(SETTINGS_TITLE, Texture.class));
        titleImage.setMinWidth(titleImage.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        titleImage.setMinHeight(titleImage.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image title = new Image(titleImage, Scaling.fit);

        //X Icon
        TextureRegionDrawable xIconTexture = new TextureRegionDrawable(manager.get(X_ICON, Texture.class));
        xIconTexture.setMinWidth(xIconTexture.getMinWidth()/INTENDED_WIDTH/2f * canvas.getWidth());
        xIconTexture.setMinHeight(xIconTexture.getMinHeight()/INTENDED_HEIGHT/2f * canvas.getHeight());
        Image xIcon = new Image(xIconTexture, Scaling.fit);
        xIcon.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clearSave.setDisabled(false);
                listener.updatePreferences();
                setCurrentState(CurrentState.MAIN_MENU);
                audio.playSound("settingsBack", MENU_CLICK_BACKWARD, false);
            }
        });

        //Settings
        TextureRegionDrawable settingsImage = new TextureRegionDrawable(manager.get(SETTINGS, Texture.class));
        settingsImage.setMinWidth(settingsImage.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        settingsImage.setMinHeight(settingsImage.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image settings = new Image(settingsImage, Scaling.fit);

        //Create Left Sub Table
        Table leftSubTable = createLeftSettingsSubTable();

        //Divider Image
        TextureRegionDrawable dividerImage = new TextureRegionDrawable(manager.get(DIVIDER, Texture.class));
        dividerImage.setMinWidth(dividerImage.getMinWidth()/INTENDED_WIDTH/2f * canvas.getWidth());
        dividerImage.setMinHeight(dividerImage.getMinHeight()/INTENDED_HEIGHT/2f * canvas.getHeight());
        Image divider = new Image(dividerImage);

        //Create Right Sub Table
        Table rightSubTable = createRightSettingsSubTable();

        //Add to settings table
        settingsTable.row().top()
                .padTop(Value.percentHeight(135/INTENDED_HEIGHT, settingsTable))
                .expand();
        settingsTable.add(title).colspan(3);
        settingsTable.add(xIcon).right()
                .padLeft(Value.percentWidth(250.28f/INTENDED_WIDTH, settingsTable))
                .padTop(Value.percentHeight(80f/INTENDED_HEIGHT, settingsTable))
                .padRight(Value.percentWidth(90f/INTENDED_WIDTH, settingsTable));

        settingsTable.row();
        settingsTable.add(settings).colspan(3).expand();
        settingsTable.add();

        settingsTable.row()
                .padBottom(Value.percentHeight(80/INTENDED_HEIGHT, settingsTable));
        settingsTable.add(leftSubTable)
                .padBottom(Value.percentHeight(160/INTENDED_HEIGHT, settingsTable));
        settingsTable.add(divider)
                .padLeft(Value.percentWidth(92.29f/INTENDED_WIDTH, settingsTable))
                .padRight(Value.percentWidth(127.48f/INTENDED_WIDTH, settingsTable));
        settingsTable.add(rightSubTable)
                .padBottom(Value.percentHeight(160/INTENDED_HEIGHT, settingsTable));
        settingsTable.add();
    }

    /**
     *
     * @return
     */
    private Table createLeftSettingsSubTable() {
        //Reusable variables
        final AudioController audio = AudioController.getInstance();
        ImageButton.ImageButtonStyle buttonStyle;
        Texture buttonTexture;

        //Create Left Sub Table
        Table leftSubTable = new Table();
        leftSubTable.center();

        //Sound text
        TextureRegionDrawable soundTextTexture = new TextureRegionDrawable(manager.get(SOUND, Texture.class));
        soundTextTexture.setMinWidth(soundTextTexture.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        soundTextTexture.setMinHeight(soundTextTexture.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image soundText = new Image(soundTextTexture, Scaling.fit);

        //Sound toggle
        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(SETTINGS_TOGGLE_ON, Texture.class));
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH/2f * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT/2f * canvas.getHeight());
        buttonStyle.imageChecked = new TextureRegionDrawable(manager.get(SETTINGS_TOGGLE_OFF, Texture.class));
        buttonStyle.imageChecked.setMinWidth(buttonStyle.imageChecked.getMinWidth()/INTENDED_WIDTH/2f * canvas.getWidth());
        buttonStyle.imageChecked.setMinHeight(buttonStyle.imageChecked.getMinHeight()/INTENDED_HEIGHT/2f * canvas.getHeight());
        final ImageButton soundToggle = new ImageButton(buttonStyle);

        //Fullscreen text
        TextureRegionDrawable fullscreenTextTexture = new TextureRegionDrawable(manager.get(FULLSCREEN, Texture.class));
        fullscreenTextTexture.setMinWidth(fullscreenTextTexture.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        fullscreenTextTexture.setMinHeight(fullscreenTextTexture.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image fullscreenText = new Image(fullscreenTextTexture, Scaling.fit);

        //Fullscreen toggle
        buttonStyle = new ImageButton.ImageButtonStyle(buttonStyle);
        final ImageButton fullscreenToggle = new ImageButton(buttonStyle);

        //Checked == Off
        soundToggle.setChecked(!saveGame.soundActive);
        fullscreenToggle.setChecked(!saveGame.isFullscreen || !Gdx.graphics.isFullscreen());

        soundToggle.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.setSoundActive(!soundToggle.isChecked());
                masterVolSlider.setDisabled(soundToggle.isChecked());
                sfxVolSlider.setDisabled(soundToggle.isChecked());
                musicVolSlider.setDisabled(soundToggle.isChecked());
                listener.updatePreferences();
                audio.playSound("fullscreen toggle", MENU_CLICK_FORWARD, false);
            }
        });
        fullscreenToggle.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!Gdx.graphics.isFullscreen()) {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                } else {
                    Gdx.graphics.setWindowedMode(1280, 720);
                }
                listener.updatePreferences();
                audio.playSound("fullscreen toggle", MENU_CLICK_FORWARD, false);
            }
        });

        //Clear Save
        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(CLEAR_SAVE, Texture.class));
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver = new TextureRegionDrawable(manager.get(CLEAR_SAVE, Texture.class)).tint(Color.LIGHT_GRAY);
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageOver.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageOver.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageDisabled = new TextureRegionDrawable(manager.get(SAVE_CLEARED, Texture.class));
        buttonStyle.imageDisabled.setMinWidth(buttonStyle.imageDisabled.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageDisabled.setMinHeight(buttonStyle.imageDisabled.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        clearSave = new ImageButton(buttonStyle);

        clearSave.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!clearSave.isChecked() && !clearSave.isDisabled() && pointer == -1) {
                    audio.playSound("clearSaveOver", BUTTON_MOUSEOVER, false);
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!clearSave.isDisabled()) {
                    clearSave.setDisabled(true);
                    listener.resetSaveGameLevel();
                    audio.playSound("clearSaveClick", MENU_CLICK_FORWARD, false);
                }
            }
        });

        leftSubTable.row()
                .padBottom(Value.percentHeight(51/INTENDED_HEIGHT, settingsTable));
        leftSubTable.add(soundText).expandX().left();
        leftSubTable.add(soundToggle);

        leftSubTable.row()
                .padBottom(Value.percentHeight(51/INTENDED_HEIGHT, settingsTable));
        leftSubTable.add(fullscreenText).expandX().left();
        leftSubTable.add(fullscreenToggle);

        leftSubTable.row();
        leftSubTable.add(clearSave).colspan(2);

        return leftSubTable;
    }

    /**
     *
     * @return
     */
    private Table createRightSettingsSubTable() {
        //Reusable variables
        final AudioController audio = AudioController.getInstance();

        //Create Right Sub Table
        Table rightSubTable = new Table();

        //Master Text Image
        TextureRegionDrawable masterVolTexture = new TextureRegionDrawable(manager.get(MASTER_VOLUME, Texture.class));
        masterVolTexture.setMinWidth(masterVolTexture.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        masterVolTexture.setMinHeight(masterVolTexture.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image masterVol = new Image(masterVolTexture);

        //Master Slider
        Slider.SliderStyle style = new Slider.SliderStyle();
        style.background = new TextureRegionDrawable(manager.get(SLIDER_BACKGROUND, Texture.class));
        style.background.setMinWidth(style.background.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        style.background.setMinHeight(style.background.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        style.disabledKnob = new TextureRegionDrawable(manager.get(SLIDER_KNOB, Texture.class)).tint(Color.DARK_GRAY);
        style.disabledKnob.setMinWidth(style.disabledKnob.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        style.disabledKnob.setMinHeight(style.disabledKnob.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        style.knob = new TextureRegionDrawable(manager.get(SLIDER_KNOB, Texture.class));
        style.knob.setMinWidth(style.knob.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        style.knob.setMinHeight(style.knob.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        style.knobBefore = new TextureRegionDrawable(manager.get(SLIDER_FILL, Texture.class));
        style.knobBefore.setMinWidth(style.knobBefore.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        style.knobBefore.setMinHeight(style.knobBefore.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        style.disabledKnobBefore = new TextureRegionDrawable(manager.get(SLIDER_FILL, Texture.class)).tint(Color.DARK_GRAY);
        style.disabledKnobBefore.setMinWidth(style.disabledKnobBefore.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        style.disabledKnobBefore.setMinHeight(style.disabledKnobBefore.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        masterVolSlider = new Slider(0f, 1f, 0.1f, false, style);

        //Master Text Image
        TextureRegionDrawable sfxVolTexture = new TextureRegionDrawable(manager.get(SFX, Texture.class));
        sfxVolTexture.setMinWidth(sfxVolTexture.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        sfxVolTexture.setMinHeight(sfxVolTexture.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image sfxVol = new Image(sfxVolTexture);

        //Sfx slider
        style = new Slider.SliderStyle(style);
        sfxVolSlider = new Slider(0f, 1f, 0.1f, false, style);

        //Music Text Image
        TextureRegionDrawable musicVolTexture = new TextureRegionDrawable(manager.get(MUSIC, Texture.class));
        musicVolTexture.setMinWidth(musicVolTexture.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        musicVolTexture.setMinHeight(musicVolTexture.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image musicVol = new Image(musicVolTexture);

        //Music slider
        style = new Slider.SliderStyle(style);
        musicVolSlider = new Slider(0f, 1f, 0.1f, false, style);

        masterVolSlider.setValue(saveGame.masterVolume);
        masterVolSlider.setDisabled(!saveGame.soundActive);
        sfxVolSlider.setValue(saveGame.sfxVolume);
        sfxVolSlider.setDisabled(!saveGame.soundActive);
        musicVolSlider.setValue(saveGame.musicVolume);
        musicVolSlider.setDisabled(!saveGame.soundActive);

        masterVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (audio.getMasterVolume() != masterVolSlider.getValue()) {
                    audio.setMasterVolume(masterVolSlider.getValue());
                    audio.playSound("masterSliderChange", BUTTON_MOUSEOVER, false);
                    listener.updatePreferences();
                }
            }
        });
        sfxVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (audio.getSoundVolume() != sfxVolSlider.getValue()) {
                    audio.setSoundVolume(sfxVolSlider.getValue());
                    audio.playSound("sfxSliderChange", BUTTON_MOUSEOVER, false);
                    listener.updatePreferences();
                }
            }
        });
        musicVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (audio.getMusicVolume() != musicVolSlider.getValue()) {
                    audio.setMusicVolume(musicVolSlider.getValue());
                    audio.playSound("musicSliderChange", BUTTON_MOUSEOVER, false);
                    listener.updatePreferences();
                }
            }
        });

        //Add to right sub table
        rightSubTable.row();
        rightSubTable.add(masterVol).left()
                .padBottom(Value.percentHeight(20.87f/INTENDED_HEIGHT, settingsTable));
        rightSubTable.row();
        rightSubTable.add(masterVolSlider).minWidth(Value.percentWidth(426.82f/INTENDED_WIDTH, settingsTable))
                .padBottom(Value.percentHeight(40.79f/INTENDED_HEIGHT, settingsTable));
        rightSubTable.row();
        rightSubTable.add(sfxVol).left()
                .padBottom(Value.percentHeight(20.87f/INTENDED_HEIGHT, settingsTable));
        rightSubTable.row();
        rightSubTable.add(sfxVolSlider).minWidth(Value.percentWidth(426.82f/INTENDED_WIDTH, settingsTable))
                .padBottom(Value.percentHeight(40.79f/INTENDED_HEIGHT, settingsTable));
        rightSubTable.row();
        rightSubTable.add(musicVol).left()
                .padBottom(Value.percentHeight(20.87f/INTENDED_HEIGHT, settingsTable));
        rightSubTable.row();
        rightSubTable.add(musicVolSlider).minWidth(Value.percentWidth(426.82f/INTENDED_WIDTH, settingsTable));

        return rightSubTable;
    }

    /**
     *
     */
    public void createCreditsTable() {
        final AudioController audio = AudioController.getInstance();
        creditsTable = new Table();
        creditsTable.setFillParent(true);
        creditsTable.center();

        //Title
        TextureRegionDrawable titleImage = new TextureRegionDrawable(manager.get(SETTINGS_TITLE, Texture.class));
        titleImage.setMinWidth(titleImage.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        titleImage.setMinHeight(titleImage.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image title = new Image(titleImage, Scaling.fit);

        //X Icon
        TextureRegionDrawable xIconTexture = new TextureRegionDrawable(manager.get(X_ICON, Texture.class));
        xIconTexture.setMinWidth(xIconTexture.getMinWidth()/INTENDED_WIDTH/2f * canvas.getWidth());
        xIconTexture.setMinHeight(xIconTexture.getMinHeight()/INTENDED_HEIGHT/2f * canvas.getHeight());
        Image xIcon = new Image(xIconTexture, Scaling.fit);
        xIcon.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
//                clearSave.setDisabled(false);
//                listener.updatePreferences();
                setCurrentState(CurrentState.MAIN_MENU);
                audio.playSound("settingsBack", MENU_CLICK_BACKWARD, false);
            }
        });

        //Credits
        TextureRegionDrawable settingsImage = new TextureRegionDrawable(manager.get(CREDITS, Texture.class));
        settingsImage.setMinWidth(settingsImage.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        settingsImage.setMinHeight(settingsImage.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image credits = new Image(settingsImage, Scaling.fit);

        creditsTable.row().top().padTop(40);
        creditsTable.add(title).colspan(3);
        creditsTable.add(xIcon).right();

        creditsTable.row().padTop(10);
        creditsTable.add(credits).colspan(3).padLeft(37);
//        creditsTable.setDebug(true, true);
    }
    //#endregion
        //======================================================

    /**
     * Unloads the assets for this game.
     *
     * This method erases the static variables.  It also deletes the associated textures
     * from the asset manager. If no assets are loaded, this method does nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void unloadContent(AssetManager manager) {
        for(String s : assets) {
            if (manager.isLoaded(s)) {
                manager.unload(s);
            }
        }
    }
    //#endregion
    //=================================

    //================================================================================
    //#region Gameplay Loop
    /**
     * The update code for the cursor.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        InputController input = InputController.getInstance();
        input.readInput(null, null);
        if (input.didPressLeft()) {
            listener.setCursor(ScreenListener.CursorState.CLICKED);
        } else if (input.didReleaseLeft()) {
            listener.setCursor(ScreenListener.CursorState.DEFAULT);
        }

        AudioController.getInstance().update();
    }

    /**
     * Draw the on screen octopi, background bubbles, and menu buttons.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void draw(float delta) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        canvas.begin();

        //Background
        canvas.draw(background, Color.WHITE, 0, 0, width, height);

        //Bubbles - update animation postion and draw
        for (BubbleGroup bubbleGroup : screenBubbles) {
            bubbleGroup.updatePosition(delta);
            bubbleGroup.draw(canvas);
        }

        //Octopus - update animation position
        if (currentState == CurrentState.MAIN_MENU) {
            animationVar += delta;
            float ANIMATION_ANGVEL = (2f * (float) Math.PI) / ANIMATION_PERIOD;
            float heightOff = animationAmplitude * (float) Math.sin(ANIMATION_ANGVEL * animationVar);
            //not sure why these are voer 4 instead of 2, but it works
            canvas.draw(fight, Color.WHITE, fight.getRegionWidth() / 4f, fight.getRegionHeight() / 4f,
                    fightPos[0], fightPos[1] + heightOff, fightDimensions[0], fightDimensions[1]);
            canvas.draw(flight, Color.WHITE, flight.getRegionWidth() / 4f, flight.getRegionHeight() / 4f,
                    flightPos[0], flightPos[1] + heightOff, flightDimensions[0], flightDimensions[1]);
            canvas.draw(fold, Color.WHITE, fold.getRegionWidth() / 4f, fold.getRegionHeight() / 4f,
                    foldPos[0], foldPos[1] + heightOff, foldDimensions[0], foldDimensions[1]);
        }
        canvas.end();

        canvas.drawStage(delta);
    }

    /**
     *
     * @param delta Number of seconds since last update
     */
    @Override
    public void transitionDraw(float delta) {
        draw(delta);
    }

    /**
     *
     */
    @Override
    public void showTransition() {
        canvas.clearStage();
        canvas.addTable(mainTable);
    }

    /**
     *
     * @param currentState
     */
    private void setCurrentState(CurrentState currentState) {
        if (this.currentState != currentState) {
            canvas.clearStage();
            if (currentState == CurrentState.MAIN_MENU)
                canvas.addTable(mainTable);
            else if (currentState == CurrentState.SETTINGS)
                canvas.addTable(settingsTable);
            else if (currentState == CurrentState.CREDITS)
                canvas.addTable(creditsTable); // TODO: Credits screen
            this.currentState = currentState;
        }
    }
    //#endregion
    //=================================

    //================================================================================
    //#region Screen Interface Methods
    /**
     * Called when this screen becomes the current screen for a Game.
     */
    @Override
    public void show() {
        if (!active) {
            active = true;
            canvas.clearStage();
            canvas.addTable(mainTable);

            AudioController audio = AudioController.getInstance();
            String musicFile = audio.getCurrentlyPlayingTrack();
            if (musicFile == null || !musicFile.equals(MENU_MUSIC)) {
                AudioController.getInstance().crossfade(MENU_MUSIC);
            }
            audio.playFoley(FOLEY);
        }
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    @Override
    public void render(float delta) {
        if (active) {
            update(delta); // This is the one that must be defined.
            draw(delta);
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    @Override
    public void resize(int width, int height) {
//        createMenuMasterTable();
//        createSettingsMasterTable();
        resizeAndRepositionOctopi();
        resizeAndRepositionBubbles();
        canvas.clearStage();
        if (currentState == CurrentState.MAIN_MENU)
            canvas.addTable(mainTable);
        else if (currentState == CurrentState.SETTINGS)
            canvas.addTable(settingsTable);
        else if (currentState == CurrentState.CREDITS)
            canvas.addTable(mainTable); // TODO: Credits screen
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    @Override
    public void pause() {
        //TODO: Implement
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    @Override
    public void resume() {
        //TODO: Implement
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    @Override
    public void hide() {
        active = false;
    }

    /**
     * Called when this screen should release all resources.
     */
    @Override
    public void dispose() {
        if (background != null) {
            background.dispose();
            background = null;
        }

        //Octopi
        if (fight != null) {
            fight.getTexture().dispose();
            fight = null;
        }
        if (flight != null) {
            flight.getTexture().dispose();
            flight = null;
        }
        if (fold != null) {
            fold.getTexture().dispose();
            fold = null;
        }

        //Bubbles
        for (int i = 0; i < NUMBER_OF_BUBBLE_TEXTURES; i++) {
            if (bubbleGroupTextures[i] != null) {
                bubbleGroupTextures[i].getTexture().dispose();
                bubbleGroupTextures[i] = null;
            }
        }

        mainTable = null;
        settingsTable = null;
        canvas = null;
    }
    //#endregion
    //=================================
}
