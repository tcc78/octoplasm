package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.octoplasm.animationView.EnemyView;
import edu.cornell.gdiac.octoplasm.animationView.OctopusView;
import edu.cornell.gdiac.octoplasm.entity.*;
import edu.cornell.gdiac.octoplasm.util.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
//This was taken from WorldController from PhysicsLab (Lab 4)

/**
 * TODO: Documentation
 *
 * @author Jarrett Coleman
 */
public class GameplayController extends SuperController implements Screen {
    //=========================================================================
    //#region Fields
    /** Tracks the current world of the gameplay controller. Mainly used for doing world
     * specific actions, such as changing music files. */
    public enum GameWorld {
        /** The Cave World */
        CAVE,
        /** The Ship World */
        SHIP,
        /** The Ocean World */
        OCEAN
    }
    /** Tracks the asset state. Otherwise subclasses will try to load assets */
    protected enum AssetState {
        /** No assets loaded */
        EMPTY,
        /** Still loading assets */
        LOADING,
        /** Assets are complete */
        COMPLETE
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
            this.color = new Color(Color.WHITE).sub(0,0,0,(float) Math.pow(12,r.nextFloat()-1));

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
            dimensionVar = -(r.nextFloat() * BUBBLE_GROUP_VARIANCE);
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
        public void draw(GameCanvas canvas, float xOffset, float yOffset) {
            canvas.draw(bubbleGroup, color, bubbleGroup.getRegionWidth()/4f, bubbleGroup.getRegionHeight()/4f,
                    position[0] + xOffset, position[1] + yOffset, dimension[0], dimension[1]);
        }
    }

    /** Reference to the bubble group textures. Needs a number and a .png. */
    private static final String BUBBLE_GROUP_BASE_PATH = "ui/main_menu/bubble_group";

    /** A Random Object used for variance in bubble animations. */
    private static final Random r = new Random();

    /** The number of bubbles to be animated on screen. */
    public static final int NUMBER_OF_BUBBLES = 20;
    /** The number of bubble textures currently in the assets folder. */
    public static final int NUMBER_OF_BUBBLE_TEXTURES = 6;

    /** The amount of time in seconds that it takes for the bubbles to reach the top of the screen. */
    private static final float BUBBLE_ANIMATION_TIME = 16f;
    /** The offset amount (in percent of width) from the edge of the screen for the bubbles to be placed. */
    private static final float BUBBLE_GROUP_OFFSET_PERCENT = 0.05f;
    /** The offset amount (in seconds) from the bottom of the screen to replace the bubbles. */
    private static final float BUBBLE_GROUP_UNDER_OFFSET = 4f;
    /** The high bound of screen bubble size variance. */
    private static final float BUBBLE_GROUP_VARIANCE = 0.6f;

    /** The scaled dimensions of the bubble groups. */
    private float[] bubbleGroupDimensions = new float[NUMBER_OF_BUBBLE_TEXTURES * 2];
    /** The collection of textures for the bubble groups. */
    private TextureRegion[] bubbleGroupTextures = new TextureRegion[NUMBER_OF_BUBBLE_TEXTURES];
    /** All bubble groups currently being animated */
    private Array<BubbleGroup> screenBubbles = new Array<>();
    /** Texture filmstrip for the grabbing */
    private FilmStrip grabbingTentacle;
    /** Reference to the grabbing texture */
    private static final String GRABBING_TEXTURE = "filmstrips/arm.png";
    private int grabbing_frame = 0;
    private static final String INDICATOR_TEXTURE_FIGHT_1 = "images/indicator1_fight.png";
    private static final String INDICATOR_TEXTURE_FIGHT_2 = "images/indicator2_fight.png";
    private static final String INDICATOR_TEXTURE_FIGHT_3 = "images/indicator3_fight.png";

    private static final String INDICATOR_TEXTURE_FLIGHT_1 = "images/indicator1_flight.png";
    private static final String INDICATOR_TEXTURE_FLIGHT_2 = "images/indicator2_flight.png";
    private static final String INDICATOR_TEXTURE_FLIGHT_3 = "images/indicator3_flight.png";

    private static final String INDICATOR_TEXTURE_FOLD_1 = "images/indicator1_fold.png";
    private static final String INDICATOR_TEXTURE_FOLD_2 = "images/indicator2_fold.png";
    private static final String INDICATOR_TEXTURE_FOLD_3 = "images/indicator3_fold.png";

    private static final float TIME_BEFORE_POP_UP = 1;
    /** Counter that count down the number of milliseconds before popping up the win/fail table */
    private float finish_countdown;

    //#endregion
        //======================================================

    // Pathnames to shared assets
    // Dictionaries of the assets
    private HashMap<EnemyEntity.EnemySubType, SkeletonData> enemySkeletonData = new HashMap<>();
    private HashMap<EnemyEntity.EnemySubType, AnimationStateData> enemyAnimationStateData = new HashMap<>();
    private HashMap<EnemyEntity.EnemySubType,ObjectMap<BoxEntity.State, InputController.StateView>> enemyStates = new HashMap<>();
    private HashMap<OctopusEntity.OctopusSubType,SkeletonData> octopusSkeletonData = new HashMap<>();
    private HashMap<OctopusEntity.OctopusSubType,AnimationStateData> octopusAnimationStateData = new HashMap<>();
    private HashMap<OctopusEntity.OctopusSubType,ObjectMap<BoxEntity.State, InputController.StateView>> octopusStates = new HashMap<>();

        //===========================================================
    //#region File paths
    /**  */
    private static final String BACKG_FILE = "backgrounds/Beta_SolidBG.png";
    private static final String BACKG_FILE2 = "backgrounds/Pirate_SolidBG.png";
    /** Reference to the mouse crosshair texture */
    private static final String MOUSE_FILE = "ui/cursors/crosshair.png";
    /** Reference to the mouse crosshair texture when the teleport ability is active */
    private static final String TELEPORT_MOUSE_FILE = "ui/cursors/teleport_selector.png";
    /** Reference to the world pausing texture */
    private static final String PAUSE_TEXTURE = "ui/gameplay/pause.png";
    /** Reference to texture for the pause border */
    private static final String PAUSE_BORDER = "ui/gameplay/pause_border.png";
    /** Reference to texture for the pause border */
    private static final String FREEZE_REF_OFF = "ui/gameplay/freeze_info_button_off.png";
    /** Reference to texture for the pause border */
    private static final String FREEZE_REF_ON = "ui/gameplay/freeze_info_button_on.png";
//    /** Reference to texture for the pause border */
//    private static final String FIGHT_REF = "ui/gameplay/fight_reference.png";
//    /** Reference to texture for the pause border */
//    private static final String FLIGHT_REF = "ui/gameplay/flight_reference.png";
//    /** Reference to texture for the pause border */
//    private static final String FOLD_REF = "ui/gameplay/fold_reference.png";

    //Level done feedback UI paths
    /** path to the level complete screen background */
    private static final String COMPLETE_BG = "ui/gameplay/level_done/complete_background_bare.png";
    /** path to the level complete screen background */
    private static final String COMPLETE_TEXT = "ui/gameplay/level_done/complete_text.png";
    /** path to the level complete screen continue button */
    private static final String COMPLETE_CONTINUE = "ui/gameplay/level_done/complete_continue.png";
    /** path to the level complete screen level select button*/
    private static final String COMPLETE_LEVEL_SELECT = "ui/gameplay/level_done/complete_level_select.png";
    /** path to the level complete screen octopi*/
    private static final String COMPLETE_OCTOPI = "ui/gameplay/level_done/complete_octopi.png";
    /** path to the level complete screen retry button */
    private static final String COMPLETE_RETRY = "ui/gameplay/level_done/complete_retry.png";
    /** path to the level failed screen background */
    private static final String FAILED_BG = "ui/gameplay/level_done/failed_background.png";
    /** path to the level failed screen background */
    private static final String FAILED_TEXT = "ui/gameplay/level_done/failed_text.png";
    /** path to the level failed screen enemies */
    private static final String FAILED_ENEMIES = "ui/gameplay/level_done/failed_enemies.png";
    /** path to the level failed screen level select button */
    private static final String FAILED_LEVEL_SELECT = "ui/gameplay/level_done/failed_level_select.png";
    /** path to the level failed screen retry button */
    private static final String FAILED_RETRY = "ui/gameplay/level_done/failed_retry.png";
    /**  */
    private static final String FIGHT_GLOW = "static_sprites/fight_glow.png";
    /**  */
    private static final String FLIGHT_GLOW = "static_sprites/flight_glow.png";
    /**  */
    private static final String FOLD_GLOW = "static_sprites/fold_glow.png";
    /**  */
    private static final String NO_GLOW = "static_sprites/no_glow.png";

    //Music Paths
    /** The base path of the cave music during gameplay. Useless without current state and file extension. */
    private static final String CAVE_MUSIC_BASE_PATH = "music/Cave-v0.3FullExports/cave_";
    /** The base path of the ship music during gameplay. Useless without current steleport_selectortate and file extension. */
    private static final String SHIP_MUSIC_BASE_PATH = "music/Ship-v0.1FullExports/ship_"; //TODO: Fill out once ship music is finished.
    /** The base path of the cave music during gameplay. Useless without current state and file extension. */
    private static final String OCEAN_MUSIC_BASE_PATH = "oceanmusictest"; //TODO: Fill out once ship music is finished.

    //Menu UI Paths
    /** Reference to the background to cloud the screen for reset */
    private static final String CLOUDY_BACKGROUND = "backgrounds/cloudy_background.png";
    /** Font for displaying messages */
    private static String FONT_FILE = "font/LightPixel7.ttf";
    /** Reference to the font used by the reset screen */
    private static final String RESET_TABLE_FONT = "ui/RubikOne-Regular.ttf";
    /** Reference to the pause screen table background */
    private static final String PAUSE_BACKGROUND = "ui/gameplay/pause_menu/pause_menu_background.png";
    /** Reference to the resume button texture */
    private static final String PAUSE_PLAY = "ui/gameplay/pause_menu/play_button.png";
    /** Reference to the reset button texture */
    private static final String PAUSE_RESET = "ui/gameplay/pause_menu/restart_button.png";
    /** Reference to the level select button texture */
    private static final String PAUSE_LEVEL_SELECT = "ui/gameplay/pause_menu/level_select.png";
    /** Reference to the main menu button texture */
    private static final String PAUSE_MAIN_MENU = "ui/gameplay/pause_menu/main_menu.png";
    /** Reference to the level paused image */
    private static final String PAUSE_LEVEL_PAUSED = "ui/gameplay/pause_menu/level_paused.png";
    /** Reference to the game logo in the pause screen */
    private static final String PAUSE_LOGO = "ui/gameplay/pause_menu/pause_logo.png";
    /** Reference to the x icon in the pause menu */
    private static final String PAUSE_X_ICON = "ui/gameplay/pause_menu/x_icon.png";
    /** Reference to the x icon in the pause menu */
    private static final String PAUSE_CONTROLS = "ui/gameplay/pause_menu/pause_controls_info.png";
    /** Reference to the Fight texture for the pause screen */
    private static final String PAUSE_FIGHT = "ui/gameplay/pause_menu/fight.png"; //TODO: Put in pause menu
    /** Reference to the Flight texture for the pause screen */
    private static final String PAUSE_FLIGHT = "ui/gameplay/pause_menu/flight.png"; //TODO: Put in pause menu
    /** Reference to the Fold texture for the pause screen */
    private static final String PAUSE_FOLD = "ui/gameplay/pause_menu/fold.png"; //TODO: Put in pause menu

    /** Reference to the menu click backward sound. */
    private static final String MENU_CLICK_BACKWARD = "sounds/ui/menu_click_backward.wav";
    /** Reference to the level select mouse over sound. */
    private static final String BUTTON_MOUSEOVER = "sounds/ui/button_mouseover.wav";
    /** Reference to the level select mouse over sound. */
    private static final String GAMEPLAY_MENU_CLICK = "sounds/ui/gameplay_menu_click.wav";
    /**  */
    private static final String GAME_WIN = "sounds/gameplay/win_sound.wav";
    /**  */
    private static final String WIN_ANIMATION = "sounds/gameplay/win_animation.wav";

    //Stored for other Controllers, kinda unsafe though
    /**  */
    private static final String TRANSITION = "sounds/gameplay/transition.wav";
    /**  */
    private static final String FLIGHT_WALL = "sounds/gameplay/flight_wall.wav";
    /**  */
    private static final String FOLD_WALL = "sounds/gameplay/fold_wall.wav";
    //#endregion
        //======================================================


        //===========================================================
    //#region Final Variables
    /** Exit code for quitting the game */
    public static final int EXIT_QUIT = 0;
    /** Exit code for advancing to next level */
    public static final int EXIT_NEXT = 1;
    /** Exit code for jumping back to previous level */
    public static final int EXIT_PREV = 2;
    /** Exit code for going back to the level select screen. */
    public static final int EXIT_LEVEL = 3;
    /** Exit code for going back to the main menu screen. */
    public static final int EXIT_MENU = 4;
    /** Exit code for going back to the main menu screen because a populate level failed. */
    public static final int EXIT_POPULATE_FAILED = 5;
    /** How many frames after winning/losing do we continue? */
    public static final int EXIT_COUNT = 120;

    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1/60.0f;
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;
    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /** The default value of gravity (going down) */
    protected static final float DEFAULT_GRAVITY = 0f;
    /** radius of boxobject for detecting selection of objects (follows cursor) */
    protected static final float SELECTOR_RADIUS = 0.01f;

    /** The intended pixel width of the game screen, stored as a variable for resizing. */
    private static final float INTENDED_WIDTH = 1920;
    /** The intended pixel height of the game screen, stored as a variable for resizing. */
    private static final float INTENDED_HEIGHT = 1080;
    /**  */
    private static final float INTENDED_COMPLETE_OCTOPI_POS = (INTENDED_HEIGHT - 239f) - 284.2f/2f;

    /** The amount of time in seconds that it takes for the octopus sprite movement to loop. */
    private static final float ANIMATION_PERIOD = 6f;
    /** The intended pixel amplitude of the octopus sprite movement. */
    private static final float INTENDED_ANIMATION_AMPLITUDE = 25f;

    /** The intended y position of the reference. */
    private static final int INTENDED_REF_Y_POS = (int) INTENDED_HEIGHT - 109;
    /** The intended x position of the reference. */
    private static final int INTENDED_REF_X_POS = 1800;
    /** The font size of the you win/fail message. */
    private static final int LEVEL_FONT_SIZE = 32;
    /** The font size of the you win/fail message. */
    private static final int FONT_SIZE = 64;
    /** Counter that counts how many second have passed */
    private float time_counter;
    //#endregion
        //======================================================

    /** These are the spine part of the animations
     *
     */
    TextureAtlas teleporterAtlas,exploderAtlas,flyerAtlas,normalEnemyAtlas,armorEnemyAtlas,spikedEnemyAtlas,holeEnemyAtlas,invincibleEnemyAtlas;
    SkeletonData teleporterSkeletonData,exploderSkeletonData,flyerSkeletonData,normalEnemySkeletonData,armorEnemySkeletonData,spikedEnemySkeletonData,holeEnemySkeletonData,invincibleEnemySkeletonData;
    AnimationStateData teleporterAnimationData, exploderAnimationData,flyerAnimationData,normalAnimationData,armorAnimationData,spikedAnimationData,holeAnimationData,invincibleAnimationData;
    ObjectMap<BoxEntity.State, InputController.StateView> teleporterStates = new ObjectMap<>(),
            exploderStates = new ObjectMap<>(),
            flyerStates = new ObjectMap<>(),
            normalEnemyStates = new ObjectMap<>(),
            armorEnemyStates = new ObjectMap<>(),
            spikedEnemyStates = new ObjectMap<>(),
            holeEnemyStates = new ObjectMap<>(),
            invincibleEnemyStates = new ObjectMap<>();

    /** Are we in level editor mode */
    private boolean levelEdit;
    /** Track asset loading from all instances and subclasses */
    private AssetState worldAssetState = AssetState.EMPTY;
    /** Track all loaded assets (for unloading purposes) */
    private Array<String> assets;
    /** Texture asset for background image */
    private TextureRegion backgroundTexture;
    private TextureRegion backgroundTexture1;
    private TextureRegion backgroundTexture2;
    /**  */
    private TextureRegion completeBgTexture;
    /**  */
    private TextureRegion failedBgTexture;
    /**  */
    private TextureRegion failedText;
    private HashMap<OctopusEntity.OctopusSubType,TextureRegion> indicator_texture_1 = new HashMap<>();
    private HashMap<OctopusEntity.OctopusSubType,TextureRegion> indicator_texture_2 = new HashMap<>();
    private HashMap<OctopusEntity.OctopusSubType,TextureRegion> indicator_texture_3 = new HashMap<>();

    /** Texture asset for mouse crosshair */
    private TextureRegion crosshairTexture;
    /** Texture asset for pause texture */
    private TextureRegion pauseTexture;
    /** Texture asset for the teleportation selector texture */
    private TextureRegion teleportSelectorTexture;
    /** Texture asset for the pause border */
    private TextureRegion pauseBorder;
    /** Texture asset for the pause controls */
    private TextureRegion pauseControls;
    /** Texture asset for the freeze reference button when it is off */
    private TextureRegion freezeRefOff;
    /** Texture asset for the freeze reference button when it is on */
    private TextureRegion freezeRefOn;
    /** Texture asset for the fight reference */
    private TextureRegion fightRef;
    /** Texture asset for the flight reference */
    private TextureRegion flightRef;
    /** Texture asset for the fold reference */
    private TextureRegion foldRef;
    /**  */
    private TextureRegion completeOctopi;
    /**  */
    private TextureRegion fold_glow;
    /**  */
    private TextureRegion fight_glow;
    /**  */
    private TextureRegion flight_glow;
    /**  */
    private TextureRegion no_glow;

    /** Collision controller for controlling collisions*/
    private CollisionController CC;
    /** OctopusController for doing octopus controlling*/
    private OctopusController OC;
    /** EnemyController for doing enemy controlling*/
    private EnemyController EC;
    /** ObstacleController for doing obstacle controlling*/
    private ObstacleController BC;
    /** LevelEditorController for doing level editor*/
    private LevelEditorController LC;

    /** the levelmodel of the current level*/
    private LevelModel model;

    /** All the objects in the world. */
    protected PooledList<Entity> objects;
    /** Queue for adding objects */
    protected PooledList<Entity> addQueue = new PooledList<>();
    /** Box object for detecting selection of objects (follows cursor) */
    protected BoxEntity selector;

    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The scale for camera space to box2d coordinates */
    protected Rectangle scaleBounds = new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
    /** The world scale */
    protected Vector2 scale;
    /** Cache object for setting vector positions */
    private Vector2 cache;

    /** The Scene2D table for the reset screen. */
    private Table resetTable;
    /** The Scene2D table for the level complete screen. */
    private Table completeTable;
    /** The Scene2D table for the level failed screen. */
    private Table failedTable;
    /** The Scene2D table for the reset screen. */
    private Table freezeTable;
    /** The Scene2D table for the pause screen. */
    private Table pausedTable;

    private ImageButton referenceButton;

    private LevelList levelList;
    private SaveGame saveGame;

    /** The font used by the reset table. */
    private BitmapFont resetFont;

    private AssetManager manager;

    /** Countdown active for winning or losing */
    private int countdown;
    /** Current level to be loaded */
    private int currentLevel;

    /** The x variable for sin function used in the octopus animation. */
    private float animationVar = 0;

    /** Whether or not this is an active controller */
    private boolean active;
    /** Whether we have completed this level */
    private boolean complete;
    /** Whether we have failed at this world (and need a reset) */
    private boolean failed;
    /** Whether or not debug mode is active */
    private boolean debug;
    /** Tracks if this controller was started in level editing mode or gameplay mode. */
    private boolean playing;
    /** keep track of if time is frozen */
    private boolean frozen;
    /** If we just switched to this screen. */
    private boolean justSwitched;
    /** If we are showing reset table */
    private boolean promptForReset;
    /** If we are showing the paused screen. */
    private boolean paused;
    /** whether reference mode is on */
    private boolean referenceMode = true;
    /** The current area of the level. */
    private GameWorld currentArea;
    /** keeps track of when level is first done */
    private boolean firstCompleted = true;
    /** String output to screen to tell which level we are at */
    BitmapFont levelFont;
    /** String output to screen to tell which level we are at in the begining of the game */
    BitmapFont beginFont;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a new game world with the default values.
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     */
    protected GameplayController() {
        this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),
                new Vector2(0,DEFAULT_GRAVITY));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param width  	The width in Box2d coordinates
     * @param height	The height in Box2d coordinates
     * @param gravity	The downward gravity
     */
    protected GameplayController(float width, float height, float gravity) {
        this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param bounds	The game bounds in Box2d coordinates
     * @param gravity	The gravitational force on this Box2d world
     */
    protected GameplayController(Rectangle bounds, Vector2 gravity)  {
        assets = new Array<String>();
        time_counter = 0;
        world = new World(gravity,false);
        levelFont = new BitmapFont();
        beginFont = new BitmapFont();
        this.bounds = new Rectangle(bounds);
        this.playing = true;
        this.levelEdit = false;

        this.scale = new Vector2(1,1);
        finish_countdown = TIME_BEFORE_POP_UP;
        complete = false;
        failed = false;
        debug  = false;
        active = false;
        promptForReset = false;
        paused = false;
        countdown = -1;
        cache = new Vector2();
        setDebug(false);
        setFailure(false);
        objects = new PooledList<>();
        selector = new BoxEntity(12,12,SELECTOR_RADIUS*scale.x,SELECTOR_RADIUS*scale.y, Entity.EntityType.SELECTOR);
        selector.setActive(true);
        selector.setSensor(true);
        objects.add(selector);

        BC = new ObstacleController(canvas, scale, assets);
        OC = new OctopusController(scale, canvas, assets);
        EC = new EnemyController(scale, canvas, assets);
        model = new LevelModel();
        model.bounds = new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
        model.initCamPos = new Vector2(DEFAULT_WIDTH/2,DEFAULT_HEIGHT/2);
        LC = new LevelEditorController(canvas,bounds,objects,scale,OC,EC,CC,BC);
        world.setContactListener(CC);

        CC = new CollisionController(BC, OC);
        CC.setComplete(false);
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**
     * sets whether frozen or not
     *
     * @param value
     */
    public void setFrozen(boolean value) {
        frozen = value;
        if (AudioController.getInstance().isMusicPlaying()) {
            AudioController.getInstance().setDuckMusic(value);
        }
    }

    /**
     * Returns true if debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @return true if debug mode is active.
     */
    public boolean isDebug( ) {
        return debug;
    }

    /**
     * Sets whether debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @param value whether debug mode is active.
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Returns true if the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * @return true if the level is failed.
     */
    public boolean isFailure( ) {
        return failed;
    }

    /**
     * Sets whether the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * @param value whether the level is failed.
     */
    public void setFailure(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        failed = value;
    }

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
        //todo if we dont want things to be distorted
        //want 16:9 w:h ratio preserved
        this.scale.x = canvas.getWidth()/scaleBounds.getWidth();
        this.scale.y = canvas.getHeight()/scaleBounds.getHeight();
//        if(canvas.getWidth()*9 < canvas.getHeight()*16){
//            //height should be kept, width should be bigger than canvas width
//            this.scale.x = this.scale.y;
//        } else if(canvas.getWidth()*9 > canvas.getHeight()*16){
//            this.scale.y = this.scale.x;
//        }
//        System.out.println("scale x: " + scale.x + " scale y: "+ scale.y);

        BC.setCanvas(canvas, scaleBounds);
        OC.setCanvas(canvas, scaleBounds);
        EC.setCanvas(canvas, scaleBounds);
        LC.setCanvas(canvas, scaleBounds);
    }

    /**
     * If this controller is in level edit mode.
     *
     * @return Whether or not the controller is in level edit mode.
     */
    public boolean isLevelEdit() {return !playing && levelEdit;}

    /**
     * Changes the gameplay controller to editing a level.
     *
     * @param levelEdit Whether or not the level editor mode is enabled.
     */
    public void setLevelEdit(boolean levelEdit) {
        this.playing = !levelEdit;
        this.levelEdit = levelEdit;
        if (levelEdit) {
            //model = new LevelModel();
        }
    }

    /**
     * Sets the current level model held by this controller.
     *
     * @param currentLevel The exit code related to the level to load.
     * @return Whether the the load was successful.
     */
    public boolean setCurrentLevel(int currentLevel) {
        try {
            model = LevelLoader.parseJson(exitCodeToLevelName(currentLevel));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        this.currentLevel = currentLevel;
        setArea(currentLevel / 100 == 1 ? GameWorld.CAVE : currentLevel / 100 == 2 ? GameWorld.SHIP : GameWorld.OCEAN);
        canvas.setCameraPosInScreen(model.initCamPos);
        return true;
    }

    /**
     *
     * @return
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Converts an exit code instance to a level name String. Exit code encoding is given in
     * {@link LevelSelectMode#CAVE_EXIT_CODE}.
     *
     * @param exitCode The exit code to be converted.
     * @return The string representation of the file related to that exit code.
     */
    private String exitCodeToLevelName(int exitCode) {
        return "world" + exitCode/100 + "_" + exitCode%100;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
        LC.setScreenListener(listener);
    }

    /**  */
    public void setManager(AssetManager manager) {
        this.manager = manager;
    }

    /**  */
    public void setLevelList(LevelList levelList) {
        this.levelList = levelList;
    }

    /**  */
    public void setSaveGame(SaveGame saveGame) {
        this.saveGame = saveGame;
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Asset Management
    void loadFont() {
        // Load the font
        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = FONT_FILE;
        size2Params.fontParameters.size = LEVEL_FONT_SIZE;
        manager.load(FONT_FILE, BitmapFont.class, size2Params);
        assets.add(FONT_FILE);

    }

    void loadFilmStrip() {
        // Explosion textures
        manager.load(GRABBING_TEXTURE, Texture.class);
        assets.add(GRABBING_TEXTURE);
    }

    void loadOctopusAssets () {
        // For teleporter
        teleporterAtlas = new TextureAtlas(Gdx.files.internal("spines/fold/skeleton.atlas"));

        SkeletonJson json = new SkeletonJson(teleporterAtlas);
        json.setScale(1); // Scale of 1? not sure yet //TODO: ask group member
        teleporterSkeletonData = json.readSkeletonData(Gdx.files.internal("spines/fold/skeleton.json"));

        teleporterAnimationData = new AnimationStateData(teleporterSkeletonData);
        teleporterAnimationData.setDefaultMix(0.2f);
        setMix(teleporterAnimationData, "idle", "move", 0.3f);
        setMix(teleporterAnimationData, "move", "idle", 0.1f);

        setupState(teleporterStates, BoxEntity.State.death, teleporterSkeletonData, "death", false);
        setupState(teleporterStates, BoxEntity.State.idle, teleporterSkeletonData, "idle", true);
        setupState(teleporterStates, BoxEntity.State.move, teleporterSkeletonData, "move", true);
        setupState(teleporterStates, BoxEntity.State.ability, teleporterSkeletonData, "ability", true);
        setupState(teleporterStates, BoxEntity.State.win, teleporterSkeletonData, "win", false);

        octopusSkeletonData.put(OctopusEntity.OctopusSubType.TELEPORTER,teleporterSkeletonData);
        octopusAnimationStateData.put(OctopusEntity.OctopusSubType.TELEPORTER,teleporterAnimationData);
        octopusStates.put(OctopusEntity.OctopusSubType.TELEPORTER,teleporterStates);
        // For exploder
        exploderAtlas = new TextureAtlas(Gdx.files.internal("spines/fight/skeleton.atlas"));

        json = new SkeletonJson(exploderAtlas);
        json.setScale(1);
        exploderSkeletonData = json.readSkeletonData(Gdx.files.internal("spines/fight/skeleton.json"));

        exploderAnimationData = new AnimationStateData(exploderSkeletonData);
        exploderAnimationData.setDefaultMix(0.2f);
        setMix(exploderAnimationData, "idle", "move", 0.3f);
        setMix(exploderAnimationData, "move", "idle", 0.1f);

        setupState(exploderStates, BoxEntity.State.death, exploderSkeletonData, "death", false);
        setupState(exploderStates, BoxEntity.State.idle, exploderSkeletonData, "idle", true);
        setupState(exploderStates, BoxEntity.State.move, exploderSkeletonData, "move", true);
        setupState(exploderStates, BoxEntity.State.ability, exploderSkeletonData, "ability", false);
        setupState(exploderStates, BoxEntity.State.win, exploderSkeletonData, "win", false);

        octopusSkeletonData.put(OctopusEntity.OctopusSubType.EXPLODER,exploderSkeletonData);
        octopusAnimationStateData.put(OctopusEntity.OctopusSubType.EXPLODER,exploderAnimationData);
        octopusStates.put(OctopusEntity.OctopusSubType.EXPLODER,exploderStates);
        // For flyer
        flyerAtlas = new TextureAtlas(Gdx.files.internal("spines/flight/skeleton.atlas"));

        json = new SkeletonJson(flyerAtlas);
        json.setScale(1);
        flyerSkeletonData = json.readSkeletonData(Gdx.files.internal("spines/flight/skeleton.json"));

        flyerAnimationData = new AnimationStateData(flyerSkeletonData);
        flyerAnimationData.setDefaultMix(0.2f);
        setMix(flyerAnimationData, "idle", "move", 0.3f);
        setMix(flyerAnimationData, "move", "idle", 0.1f);
        setMix(flyerAnimationData, "idle", "ability", 0.1f);
        setMix(flyerAnimationData, "ability", "death", 0.1f);

        setupState(flyerStates, BoxEntity.State.death, flyerSkeletonData, "death", false);
        setupState(flyerStates, BoxEntity.State.idle, flyerSkeletonData, "idle", true);
        setupState(flyerStates, BoxEntity.State.move, flyerSkeletonData, "move", true);
        setupState(flyerStates, BoxEntity.State.ability, flyerSkeletonData, "ability", true);
        setupState(flyerStates, BoxEntity.State.win, flyerSkeletonData, "win", false);

        octopusSkeletonData.put(OctopusEntity.OctopusSubType.FLYER,flyerSkeletonData);
        octopusAnimationStateData.put(OctopusEntity.OctopusSubType.FLYER,flyerAnimationData);
        octopusStates.put(OctopusEntity.OctopusSubType.FLYER,flyerStates);
    }

    void loadEnemyAssets () {
        // For Normal Enemy
        normalEnemyAtlas = new TextureAtlas(Gdx.files.internal("spines/normal/skeleton.atlas"));

        SkeletonJson json = new SkeletonJson(normalEnemyAtlas);
        json.setScale(1); // Scale of 1? not sure yet //TODO: ask group member
        normalEnemySkeletonData = json.readSkeletonData(Gdx.files.internal("spines/normal/skeleton.json"));

        normalAnimationData = new AnimationStateData(normalEnemySkeletonData);
        normalAnimationData.setDefaultMix(0.2f);

        setupState(normalEnemyStates, BoxEntity.State.death, normalEnemySkeletonData, "death", false);
        setupState(normalEnemyStates, BoxEntity.State.idle, normalEnemySkeletonData, "idle", true);

        enemySkeletonData.put(EnemyEntity.EnemySubType.NORMAL_ENEMY,normalEnemySkeletonData);
        enemyAnimationStateData.put(EnemyEntity.EnemySubType.NORMAL_ENEMY,normalAnimationData);
        enemyStates.put(EnemyEntity.EnemySubType.NORMAL_ENEMY,normalEnemyStates);
        // For Armor Enemy
        armorEnemyAtlas = new TextureAtlas(Gdx.files.internal("spines/armored/skeleton.atlas"));

        json = new SkeletonJson(armorEnemyAtlas);
        json.setScale(1); // Scale of 1? not sure yet //TODO: ask group member
        armorEnemySkeletonData = json.readSkeletonData(Gdx.files.internal("spines/armored/skeleton.json"));

        armorAnimationData = new AnimationStateData(armorEnemySkeletonData);
        armorAnimationData.setDefaultMix(0.2f);

        setupState(armorEnemyStates, BoxEntity.State.death, armorEnemySkeletonData, "death", false);
        setupState(armorEnemyStates, BoxEntity.State.idle, armorEnemySkeletonData, "idle", true);

        enemySkeletonData.put(EnemyEntity.EnemySubType.ARMORED_ENEMY,armorEnemySkeletonData);
        enemyAnimationStateData.put(EnemyEntity.EnemySubType.ARMORED_ENEMY,armorAnimationData);
        enemyStates.put(EnemyEntity.EnemySubType.ARMORED_ENEMY,armorEnemyStates);
        // For Spiked Enemy
        spikedEnemyAtlas = new TextureAtlas(Gdx.files.internal("spines/spiked/skeleton.atlas"));

        json = new SkeletonJson(spikedEnemyAtlas);
        json.setScale(1); // Scale of 1? not sure yet //TODO: ask group member
        spikedEnemySkeletonData = json.readSkeletonData(Gdx.files.internal("spines/spiked/skeleton.json"));

        spikedAnimationData = new AnimationStateData(spikedEnemySkeletonData);
        spikedAnimationData.setDefaultMix(0.2f);

        setupState(spikedEnemyStates, BoxEntity.State.death, spikedEnemySkeletonData, "death", false);
        setupState(spikedEnemyStates, BoxEntity.State.idle, spikedEnemySkeletonData, "idle", true);

        enemySkeletonData.put(EnemyEntity.EnemySubType.SPIKED_ENEMY,spikedEnemySkeletonData);
        enemyAnimationStateData.put(EnemyEntity.EnemySubType.SPIKED_ENEMY,spikedAnimationData);
        enemyStates.put(EnemyEntity.EnemySubType.SPIKED_ENEMY,spikedEnemyStates);
        // For Hole Enemy
        holeEnemyAtlas = new TextureAtlas(Gdx.files.internal("spines/donut/skeleton.atlas"));

        json = new SkeletonJson(holeEnemyAtlas);
        json.setScale(1); // Scale of 1? not sure yet //TODO: ask group member
        holeEnemySkeletonData = json.readSkeletonData(Gdx.files.internal("spines/donut/skeleton.json"));

        holeAnimationData = new AnimationStateData(holeEnemySkeletonData);
        holeAnimationData.setDefaultMix(0.2f);

        setupState(holeEnemyStates, BoxEntity.State.death, holeEnemySkeletonData, "death", false);
        setupState(holeEnemyStates, BoxEntity.State.idle, holeEnemySkeletonData, "idle", true);

        enemySkeletonData.put(EnemyEntity.EnemySubType.HOLE_ENEMY,holeEnemySkeletonData);
        enemyAnimationStateData.put(EnemyEntity.EnemySubType.HOLE_ENEMY,holeAnimationData);
        enemyStates.put(EnemyEntity.EnemySubType.HOLE_ENEMY,holeEnemyStates);
        // For Invincible Enemy
        invincibleEnemyAtlas = new TextureAtlas(Gdx.files.internal("spines/invinsible/skeleton.atlas"));

        json = new SkeletonJson(invincibleEnemyAtlas);
        json.setScale(1); // Scale of 1? not sure yet //TODO: ask group member
        invincibleEnemySkeletonData = json.readSkeletonData(Gdx.files.internal("spines/invinsible/skeleton.json"));

        invincibleAnimationData = new AnimationStateData(invincibleEnemySkeletonData);
        invincibleAnimationData.setDefaultMix(0.2f);

        setupState(invincibleEnemyStates, BoxEntity.State.idle, invincibleEnemySkeletonData, "idle", true);

        enemySkeletonData.put(EnemyEntity.EnemySubType.INVINCIBLE_ENEMY,invincibleEnemySkeletonData);
        enemyAnimationStateData.put(EnemyEntity.EnemySubType.INVINCIBLE_ENEMY,invincibleAnimationData);
        enemyStates.put(EnemyEntity.EnemySubType.INVINCIBLE_ENEMY,invincibleEnemyStates);
    }

    void setMix (AnimationStateData data, String from, String to, float mix) {
        Animation fromAnimation = data.getSkeletonData().findAnimation(from);
        Animation toAnimation = data.getSkeletonData().findAnimation(to);
        if (fromAnimation == null || toAnimation == null) return;
        data.setMix(fromAnimation, toAnimation, mix);
    }

    InputController.StateView setupState (ObjectMap map, BoxEntity.State state, SkeletonData skeletonData, String name, boolean loop) {
        InputController.StateView stateView = new InputController.StateView();
        stateView.animation = skeletonData.findAnimation(name);
        stateView.loop = loop;
        map.put(state, stateView);
        return stateView;
    }

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
        // Background texture
        manager.load(BACKG_FILE, Texture.class);
        assets.add(BACKG_FILE);
        manager.load(BACKG_FILE2, Texture.class);
        assets.add(BACKG_FILE2);

        manager.load(CLOUDY_BACKGROUND, Texture.class);
        assets.add(CLOUDY_BACKGROUND);

        manager.load(PAUSE_CONTROLS, Texture.class);
        assets.add(PAUSE_CONTROLS);

        // Mouse Selection Crosshair & Teleport Crosshair textures
        manager.load(MOUSE_FILE, Texture.class);
        assets.add(MOUSE_FILE);
        manager.load(TELEPORT_MOUSE_FILE, Texture.class);
        assets.add(TELEPORT_MOUSE_FILE);

        // Indicators of position and index
        manager.load(INDICATOR_TEXTURE_FIGHT_1, Texture.class);
        assets.add(INDICATOR_TEXTURE_FIGHT_1);
        manager.load(INDICATOR_TEXTURE_FIGHT_2, Texture.class);
        assets.add(INDICATOR_TEXTURE_FIGHT_2);
        manager.load(INDICATOR_TEXTURE_FIGHT_3, Texture.class);
        assets.add(INDICATOR_TEXTURE_FIGHT_3);

        manager.load(INDICATOR_TEXTURE_FLIGHT_1, Texture.class);
        assets.add(INDICATOR_TEXTURE_FLIGHT_1);
        manager.load(INDICATOR_TEXTURE_FLIGHT_2, Texture.class);
        assets.add(INDICATOR_TEXTURE_FLIGHT_2);
        manager.load(INDICATOR_TEXTURE_FLIGHT_3, Texture.class);
        assets.add(INDICATOR_TEXTURE_FLIGHT_3);

        manager.load(INDICATOR_TEXTURE_FOLD_1, Texture.class);
        assets.add(INDICATOR_TEXTURE_FOLD_1);
        manager.load(INDICATOR_TEXTURE_FOLD_2, Texture.class);
        assets.add(INDICATOR_TEXTURE_FOLD_2);
        manager.load(INDICATOR_TEXTURE_FOLD_3, Texture.class);
        assets.add(INDICATOR_TEXTURE_FOLD_3);

        // Freeze Image Texture
        manager.load(PAUSE_TEXTURE, Texture.class);
        assets.add(PAUSE_TEXTURE);
        manager.load(PAUSE_BORDER, Texture.class);
        assets.add(PAUSE_BORDER);
        manager.load(FREEZE_REF_OFF, Texture.class);
        assets.add(FREEZE_REF_OFF);
        manager.load(FREEZE_REF_ON, Texture.class);
        assets.add(FREEZE_REF_ON);
//        manager.load(FIGHT_REF, Texture.class);
//        assets.add(FIGHT_REF);
//        manager.load(FLIGHT_REF, Texture.class);
//        assets.add(FLIGHT_REF);
//        manager.load(FOLD_REF, Texture.class);
//        assets.add(FOLD_REF);

        // Level compelte textures
        manager.load(COMPLETE_BG, Texture.class);
        assets.add(COMPLETE_BG);
        manager.load(COMPLETE_TEXT, Texture.class);
        assets.add(COMPLETE_TEXT);
        manager.load(COMPLETE_CONTINUE, Texture.class);
        assets.add(COMPLETE_CONTINUE);
        manager.load(COMPLETE_LEVEL_SELECT, Texture.class);
        assets.add(COMPLETE_LEVEL_SELECT);
        manager.load(COMPLETE_OCTOPI, Texture.class);
        assets.add(COMPLETE_OCTOPI);
        manager.load(COMPLETE_RETRY, Texture.class);
        assets.add(COMPLETE_RETRY);

        //Level failed textures
        manager.load(FAILED_BG, Texture.class);
        assets.add(FAILED_BG);
        manager.load(FAILED_TEXT, Texture.class);
        assets.add(FAILED_BG);
        manager.load(FAILED_ENEMIES, Texture.class);
        assets.add(FAILED_ENEMIES);
        manager.load(FAILED_LEVEL_SELECT, Texture.class);
        assets.add(FAILED_LEVEL_SELECT);
        manager.load(FAILED_RETRY, Texture.class);
        assets.add(FAILED_RETRY);

        manager.load(FIGHT_GLOW, Texture.class);
        assets.add(FIGHT_GLOW);
        manager.load(FLIGHT_GLOW, Texture.class);
        assets.add(FLIGHT_GLOW);
        manager.load(FOLD_GLOW, Texture.class);
        assets.add(FOLD_GLOW);
        manager.load(NO_GLOW, Texture.class);
        assets.add(NO_GLOW);

        BC.preLoadContent(manager);
        OC.preLoadContent(manager);
        EC.preLoadContent(manager);
        if (worldAssetState != AssetState.EMPTY) {
            return;
        }

        worldAssetState = AssetState.LOADING;
        // Load the shared tiles.
        //TODO: for each additional wall texture type we need to do more of these
        manager.load(WallEntity.getTextureFile(WallEntity.WallTexture.EARTH),Texture.class);
        assets.add(WallEntity.getTextureFile(WallEntity.WallTexture.EARTH));
        manager.load(WallEntity.getTextureFile(WallEntity.WallTexture.PIRATE),Texture.class);
        assets.add(WallEntity.getTextureFile(WallEntity.WallTexture.PIRATE));
        manager.load(WallEntity.getTextureFile(WallEntity.WallTexture.WOODEN),Texture.class);
        assets.add(WallEntity.getTextureFile(WallEntity.WallTexture.WOODEN));
        manager.load(WallEntity.getWallCornerFile(),Texture.class);
        assets.add(WallEntity.getWallCornerFile());
        manager.load(WallEntity.getWallEdgeFile(WallEntity.WallTexture.EARTH),Texture.class);
        assets.add(WallEntity.getWallEdgeFile(WallEntity.WallTexture.EARTH));
        manager.load(WallEntity.getWallEdgeFile(WallEntity.WallTexture.PIRATE),Texture.class);
        assets.add(WallEntity.getWallEdgeFile(WallEntity.WallTexture.PIRATE));
        manager.load(WallEntity.getWallEdgeFile(WallEntity.WallTexture.WOODEN),Texture.class);
        assets.add(WallEntity.getWallEdgeFile(WallEntity.WallTexture.WOODEN));

        // Load the font
        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = RESET_TABLE_FONT;
        size2Params.fontParameters.size = FONT_SIZE;
        size2Params.fontParameters.gamma = 1;
        manager.load(RESET_TABLE_FONT, BitmapFont.class, size2Params);
        assets.add(RESET_TABLE_FONT);

        //Load ALL THE MUSIC
        manager.load(CAVE_MUSIC_BASE_PATH + "noflight" + ".ogg", AudioSource.class);
        assets.add(CAVE_MUSIC_BASE_PATH + "noflight" + ".ogg");
        manager.load(CAVE_MUSIC_BASE_PATH + "nofold" + ".ogg", AudioSource.class);
        assets.add(CAVE_MUSIC_BASE_PATH + "nofold" + ".ogg");
        manager.load(CAVE_MUSIC_BASE_PATH + "nofight" + ".ogg", AudioSource.class);
        assets.add(CAVE_MUSIC_BASE_PATH + "nofight" + ".ogg");
        manager.load(CAVE_MUSIC_BASE_PATH + "all" + ".ogg", AudioSource.class);
        assets.add(CAVE_MUSIC_BASE_PATH + "all" + ".ogg");
        manager.load(CAVE_MUSIC_BASE_PATH + "fold" + ".ogg", AudioSource.class);
        assets.add(CAVE_MUSIC_BASE_PATH + "fold" + ".ogg");
        manager.load(CAVE_MUSIC_BASE_PATH + "fight" + ".ogg", AudioSource.class);
        assets.add(CAVE_MUSIC_BASE_PATH + "fight" + ".ogg");
        manager.load(CAVE_MUSIC_BASE_PATH + "flight" + ".ogg", AudioSource.class);
        assets.add(CAVE_MUSIC_BASE_PATH + "flight" + ".ogg");
        manager.load(CAVE_MUSIC_BASE_PATH + "none" + ".ogg", AudioSource.class);
        assets.add(CAVE_MUSIC_BASE_PATH + "none" + ".ogg");

        manager.load(SHIP_MUSIC_BASE_PATH + "noflight" + ".ogg", AudioSource.class);
        assets.add(SHIP_MUSIC_BASE_PATH + "noflight" + ".ogg");
        manager.load(SHIP_MUSIC_BASE_PATH + "nofold" + ".ogg", AudioSource.class);
        assets.add(SHIP_MUSIC_BASE_PATH + "nofold" + ".ogg");
        manager.load(SHIP_MUSIC_BASE_PATH + "nofight" + ".ogg", AudioSource.class);
        assets.add(SHIP_MUSIC_BASE_PATH + "nofight" + ".ogg");
        manager.load(SHIP_MUSIC_BASE_PATH + "all" + ".ogg", AudioSource.class);
        assets.add(SHIP_MUSIC_BASE_PATH + "all" + ".ogg");
        manager.load(SHIP_MUSIC_BASE_PATH + "fold" + ".ogg", AudioSource.class);
        assets.add(SHIP_MUSIC_BASE_PATH + "fold" + ".ogg");
        manager.load(SHIP_MUSIC_BASE_PATH + "fight" + ".ogg", AudioSource.class);
        assets.add(SHIP_MUSIC_BASE_PATH + "fight" + ".ogg");
        manager.load(SHIP_MUSIC_BASE_PATH + "flight" + ".ogg", AudioSource.class);
        assets.add(SHIP_MUSIC_BASE_PATH + "flight" + ".ogg");
        manager.load(SHIP_MUSIC_BASE_PATH + "none" + ".ogg", AudioSource.class);
        assets.add(SHIP_MUSIC_BASE_PATH + "none" + ".ogg");

        //Pause menu assets
        manager.load(PAUSE_BACKGROUND, Texture.class);
        assets.add(PAUSE_BACKGROUND);

        manager.load(PAUSE_PLAY, Texture.class);
        assets.add(PAUSE_PLAY);

        manager.load(PAUSE_RESET, Texture.class);
        assets.add(PAUSE_RESET);

        manager.load(PAUSE_LEVEL_SELECT, Texture.class);
        assets.add(PAUSE_LEVEL_SELECT);

        manager.load(PAUSE_MAIN_MENU, Texture.class);
        assets.add(PAUSE_MAIN_MENU);

        manager.load(PAUSE_LEVEL_PAUSED, Texture.class);
        assets.add(PAUSE_LEVEL_PAUSED);

        manager.load(PAUSE_LOGO, Texture.class);
        assets.add(PAUSE_LOGO);

        manager.load(PAUSE_X_ICON, Texture.class);
        assets.add(PAUSE_X_ICON);

        //Sounds
        manager.load(MENU_CLICK_BACKWARD, Sound.class);
        assets.add(MENU_CLICK_BACKWARD);

        manager.load(BUTTON_MOUSEOVER, Sound.class);
        assets.add(BUTTON_MOUSEOVER);

        manager.load(GAMEPLAY_MENU_CLICK, Sound.class);
        assets.add(GAMEPLAY_MENU_CLICK);

        manager.load(GAME_WIN, Sound.class);
        assets.add(GAME_WIN);

        manager.load(WIN_ANIMATION, Sound.class);
        assets.add(WIN_ANIMATION);
        manager.load(TRANSITION, Sound.class);
        assets.add(TRANSITION);
        manager.load(FLIGHT_WALL, Sound.class);
        assets.add(FLIGHT_WALL);
        manager.load(FOLD_WALL, Sound.class);
        assets.add(FOLD_WALL);
    }

    /**
     * Loads the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(AssetManager manager) {
        AudioController audio = AudioController.getInstance();

        // load background texture
        backgroundTexture1 = createTexture(manager,BACKG_FILE,false);
        backgroundTexture2 = createTexture(manager,BACKG_FILE2,false);
        backgroundTexture = backgroundTexture1;
        completeBgTexture = createTexture(manager,COMPLETE_BG,false);
        failedBgTexture = createTexture(manager,FAILED_BG,false);
        failedText = createTexture(manager,FAILED_TEXT,false);

        // Indicator
        indicator_texture_1.put(OctopusEntity.OctopusSubType.EXPLODER,createTexture(manager,INDICATOR_TEXTURE_FIGHT_1,false));
        indicator_texture_1.put(OctopusEntity.OctopusSubType.FLYER,createTexture(manager,INDICATOR_TEXTURE_FLIGHT_1,false));
        indicator_texture_1.put(OctopusEntity.OctopusSubType.TELEPORTER,createTexture(manager,INDICATOR_TEXTURE_FOLD_1,false));

        indicator_texture_2.put(OctopusEntity.OctopusSubType.EXPLODER,createTexture(manager,INDICATOR_TEXTURE_FIGHT_2,false));
        indicator_texture_2.put(OctopusEntity.OctopusSubType.FLYER,createTexture(manager,INDICATOR_TEXTURE_FLIGHT_2,false));
        indicator_texture_2.put(OctopusEntity.OctopusSubType.TELEPORTER,createTexture(manager,INDICATOR_TEXTURE_FOLD_2,false));

        indicator_texture_3.put(OctopusEntity.OctopusSubType.EXPLODER,createTexture(manager,INDICATOR_TEXTURE_FIGHT_3,false));
        indicator_texture_3.put(OctopusEntity.OctopusSubType.FLYER,createTexture(manager,INDICATOR_TEXTURE_FLIGHT_3,false));
        indicator_texture_3.put(OctopusEntity.OctopusSubType.TELEPORTER,createTexture(manager,INDICATOR_TEXTURE_FOLD_3,false));

        // Create other assets
        crosshairTexture = createTexture(manager, MOUSE_FILE,false);
        pauseTexture = createTexture(manager,PAUSE_TEXTURE,false);
        teleportSelectorTexture = createTexture(manager,TELEPORT_MOUSE_FILE, false);
        pauseBorder = createTexture(manager, PAUSE_BORDER, false);
        pauseControls = createTexture(manager, PAUSE_CONTROLS, false);
        freezeRefOff = createTexture(manager,FREEZE_REF_OFF,false);
        freezeRefOn = createTexture(manager,FREEZE_REF_ON,false);
//        fightRef = createTexture(manager,FIGHT_REF,false);
//        flightRef = createTexture(manager,FLIGHT_REF,false);
//        foldRef = createTexture(manager,FOLD_REF,false);
        completeOctopi = createTexture(manager,COMPLETE_OCTOPI, false);
        fight_glow = createTexture(manager,FIGHT_GLOW, false);
        flight_glow = createTexture(manager,FLIGHT_GLOW, false);
        fold_glow = createTexture(manager,FOLD_GLOW, false);
        no_glow = createTexture(manager,NO_GLOW, false);

        grabbingTentacle = createFilmStrip(manager,GRABBING_TEXTURE,1,7, 7);

        BC.loadContent(manager);
        OC.loadContent(manager);
        EC.loadContent(manager);

        // Allocate sound effects
        audio.allocateSound(manager, MENU_CLICK_BACKWARD);
        audio.allocateSound(manager, BUTTON_MOUSEOVER);
        audio.allocateSound(manager, GAMEPLAY_MENU_CLICK);
        audio.allocateSound(manager, GAME_WIN);
        audio.allocateSound(manager, WIN_ANIMATION);
        audio.allocateSound(manager, TRANSITION);
        audio.allocateSound(manager, FLIGHT_WALL);
        audio.allocateSound(manager, FOLD_WALL);

        if (worldAssetState != AssetState.LOADING) {
            return;
        }

        // Allocate the tiles
        WallEntity.setWallTextures(createTexture(manager,WallEntity.getTextureFile(WallEntity.WallTexture.EARTH),true),
                createTexture(manager,WallEntity.getTextureFile(WallEntity.WallTexture.PIRATE),true),
                createTexture(manager,WallEntity.getTextureFile(WallEntity.WallTexture.WOODEN),true));
        WallEntity.setWallEdgeTexture(createTexture(manager,WallEntity.getWallEdgeFile(WallEntity.WallTexture.EARTH),true),
                createTexture(manager,WallEntity.getWallEdgeFile(WallEntity.WallTexture.PIRATE),true),
                createTexture(manager,WallEntity.getWallEdgeFile(WallEntity.WallTexture.WOODEN),true));
        WallEntity.setWallCornerTexture(createTexture(manager,WallEntity.getWallCornerFile(),true));

        // Allocate the font
        resetFont = manager.isLoaded(RESET_TABLE_FONT) ? manager.get(RESET_TABLE_FONT, BitmapFont.class) : null;

        worldAssetState = AssetState.COMPLETE;

        createResetTable(manager);
        createPauseTable(manager);
        createFreezeTable(manager);
        createCompleteTable(manager);
        createFailedTable(manager);
        resizeAndRepositionBubbles();
    }

        //===========================================================
    //#region Table Creation
    /**
     * TODO: @Stephen Documentation
     *
     * @param manager
     */
    private void createResetTable(AssetManager manager) {
        resetTable = new Table();
        resetTable.setFillParent(true);

        TextureRegionDrawable settingsBG = new TextureRegionDrawable(manager.get(CLOUDY_BACKGROUND, Texture.class));
        resetTable.setBackground(settingsBG);

        //Text For Reset
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = resetFont;
        style.fontColor = Color.WHITE;
        Label text = new Label("Are you sure you want to reset?\n\nPress R for yes, ESC for no.", style);
        text.setAlignment(Align.center);

        //TODO: Image Buttons

        resetTable.add(text).center();
    }

    /**
     * creates the level complete screen table
     *
     * @param manager
     */
    private void createCompleteTable(AssetManager manager) {
        final GameplayController game = this;
        final AudioController audio = AudioController.getInstance();
        final ImageButton.ImageButtonStyle buttonStyle1;
        final ImageButton.ImageButtonStyle buttonStyle2;
        final ImageButton.ImageButtonStyle buttonStyle3;
        completeTable = new Table();
        completeTable.setFillParent(true);
        completeTable.center();

        TextureRegionDrawable completeText = new TextureRegionDrawable(manager.get(COMPLETE_TEXT, Texture.class));
        completeText.setMinWidth(completeText.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        completeText.setMinHeight(completeText.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image completeTextImage = new Image(completeText, Scaling.fit);

        // level select button
        buttonStyle1 = createButtonStyle(manager, COMPLETE_LEVEL_SELECT);
//        buttonStyle1.imageOver = new TextureRegionDrawable(new Texture(COMPLETE_LEVEL_SELECT)).tint(Color.LIGHT_GRAY);
        ImageButton complete_ls = new ImageButton(buttonStyle1);
        complete_ls.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuLevelClick", MENU_CLICK_BACKWARD, false);
                canvas.removeTable(completeTable);
                firstCompleted = true;
                listener.exitScreen(game, EXIT_LEVEL);
                complete = false;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                audio.playSound("pauseMenuLevelOver", BUTTON_MOUSEOVER, false);
            }
        });

        // retry button
        buttonStyle2 = createButtonStyle(manager, COMPLETE_RETRY);
//        buttonStyle2.imageOver = new TextureRegionDrawable(new Texture(COMPLETE_RETRY)).tint(Color.LIGHT_GRAY);
        ImageButton complete_retry = new ImageButton(buttonStyle2);
        complete_retry.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuLevelClick", MENU_CLICK_BACKWARD, false);
                canvas.removeTable(completeTable);
                firstCompleted = true;
                reset();
                Gdx.input.setCursorCatched(true);
                complete = false;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                audio.playSound("pauseMenuLevelOver", BUTTON_MOUSEOVER, false);
            }
        });

        // continue button
        buttonStyle3 = createButtonStyle(manager, COMPLETE_CONTINUE);
//        buttonStyle3.imageOver = new TextureRegionDrawable(new Texture(COMPLETE_CONTINUE)).tint(Color.LIGHT_GRAY);
        ImageButton complete_continue = new ImageButton(buttonStyle3);
        complete_continue.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuLevelClick", MENU_CLICK_BACKWARD, false);
                canvas.removeTable(completeTable);
                firstCompleted = true;
                if (currentArea == GameWorld.CAVE && (currentLevel % 100) < levelList.numberCaveLevels
                        || currentArea == GameWorld.SHIP && (currentLevel % 100) < levelList.numberShipLevels
                        || currentArea == GameWorld.OCEAN && (currentLevel % 100) < levelList.numberOceanLevels) {
                    //Move to next level in world
                    setCurrentLevel(currentLevel+1);
                    reset();
                    Gdx.input.setCursorCatched(true);
                } else {
                    //We have reached the end of a world
                    listener.exitScreen(game, EXIT_LEVEL);
                }
                complete = false;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                audio.playSound("pauseMenuLevelOver", BUTTON_MOUSEOVER, false);
            }
        });

//        TextureRegionDrawable completeOctopiTexture = new TextureRegionDrawable(manager.get(COMPLETE_OCTOPI, Texture.class));
//        completeOctopiTexture.setMinWidth(completeOctopiTexture.getMinWidth()/INTENDED_WIDTH * Gdx.graphics.getWidth());
//        completeOctopiTexture.setMinHeight(completeOctopiTexture.getMinHeight()/INTENDED_HEIGHT * Gdx.graphics.getHeight());
//        Image complete_octopi = new Image(completeOctopiTexture, Scaling.fit);
//
//        completeTable.row().padBottom(pauseTexture.getRegionHeight()*6f);
//        completeTable.add(complete_octopi);

        completeTable.row().padTop(Value.percentHeight(0.3f, completeTable));
        completeTable.add(completeTextImage).colspan(3);
        completeTable.row().padTop(pauseTexture.getRegionHeight()/2f).padLeft(pauseTexture.getRegionWidth()/2f);
        completeTable.add(complete_ls);
        completeTable.add(complete_retry);
        completeTable.add(complete_continue);
//        completeTable.setDebug(true, true);
    }

    /**
     * creates the level failed screen table
     *
     * @param manager
     */
    private void createFailedTable(AssetManager manager) {
        final GameplayController game = this;
        final AudioController audio = AudioController.getInstance();
        final ImageButton.ImageButtonStyle buttonStyle1;
        final ImageButton.ImageButtonStyle buttonStyle2;
        failedTable = new Table();
        failedTable.setFillParent(true);
        failedTable.center();

        // level select button
        buttonStyle1 = createButtonStyle(manager, FAILED_LEVEL_SELECT);
//        buttonStyle1.imageOver = new TextureRegionDrawable(new Texture(FAILED_LEVEL_SELECT)).tint(Color.LIGHT_GRAY);
        ImageButton failed_ls = new ImageButton(buttonStyle1);
        failed_ls.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuLevelClick", MENU_CLICK_BACKWARD, false);
                canvas.removeTable(failedTable);
                firstCompleted = true;
                listener.exitScreen(game, EXIT_LEVEL);
                complete = false;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                audio.playSound("pauseMenuLevelOver", BUTTON_MOUSEOVER, false);
            }
        });

        // retry button
        buttonStyle2 = createButtonStyle(manager, FAILED_RETRY);
//        buttonStyle2.imageOver = new TextureRegionDrawable(new Texture(FAILED_RETRY)).tint(Color.LIGHT_GRAY);
        ImageButton failed_retry = new ImageButton(buttonStyle2);
        failed_retry.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuLevelClick", MENU_CLICK_BACKWARD, false);
                canvas.removeTable(failedTable);
                firstCompleted = true;
                reset();
                Gdx.input.setCursorCatched(true);
                complete = false;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                audio.playSound("pauseMenuLevelOver", BUTTON_MOUSEOVER, false);
            }
        });

        failedTable.row().padTop(pauseTexture.getRegionHeight()*3f);
        failedTable.add(failed_ls).padRight(pauseTexture.getRegionHeight()/3f);
        failedTable.add(failed_retry).padLeft(pauseTexture.getRegionHeight()/3f);
//        failedTable.setDebug(true, true);
    }

    /**
     * creates the freeze screen table
     *
     * @param manager
     */
    private void createFreezeTable(AssetManager manager) {
        final GameplayController game = this;
        final AudioController audio = AudioController.getInstance();
        final ImageButton.ImageButtonStyle buttonStyle;
        freezeTable = new Table();
        freezeTable.setFillParent(true);
        freezeTable.top().right().padTop(freezeRefOff.getRegionHeight()).padRight(freezeRefOff.getRegionWidth());

        TextureRegionDrawable freezeBG = new TextureRegionDrawable(manager.get(PAUSE_BORDER, Texture.class));
        freezeTable.setBackground(freezeBG);

        // button for toggling references
        buttonStyle = createButtonStyle(manager, FREEZE_REF_ON);
        buttonStyle.imageChecked = new TextureRegionDrawable(manager.get(FREEZE_REF_OFF, Texture.class));
        buttonStyle.imageChecked.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageChecked.setMinHeight(buttonStyle.imageUp.getMinHeight());
        buttonStyle.imageCheckedOver = new TextureRegionDrawable(manager.get(FREEZE_REF_OFF, Texture.class)).tint(Color.LIGHT_GRAY);
        buttonStyle.imageCheckedOver.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageCheckedOver.setMinHeight(buttonStyle.imageUp.getMinHeight());
        referenceButton = new ImageButton(buttonStyle);
        referenceButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuLevelClick", MENU_CLICK_BACKWARD, false);
                if (referenceMode) {
                    referenceMode = false;
                } else {
                    referenceMode = true;
                }
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!referenceButton.isPressed())
                    audio.playSound("pauseMenuLevelOver", BUTTON_MOUSEOVER, false);
            }
        });

        freezeTable.add(referenceButton);
    }

    /**
     * TODO: @Stephen Documentation
     * @param manager
     */
    private void createPauseTable(AssetManager manager) {
        //Reusable Variables
        final GameplayController game = this;
        final AudioController audio = AudioController.getInstance();
        ImageButton.ImageButtonStyle buttonStyle;

        //Create main table
        pausedTable = new Table();
        pausedTable.setFillParent(true);
        pausedTable.setBackground(new TextureRegionDrawable(manager.get(CLOUDY_BACKGROUND, Texture.class)));

        //Create widget group for image placement
        WidgetGroup mainGroup = new WidgetGroup();

        // Pause menu logo image
        TextureRegionDrawable pauseLogoTexture = new TextureRegionDrawable(manager.get(PAUSE_LOGO, Texture.class));
        pauseLogoTexture.setMinWidth(pauseLogoTexture.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        pauseLogoTexture.setMinHeight(pauseLogoTexture.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image pauseLogo = new Image(pauseLogoTexture, Scaling.fit);

        //Center button table for actual pause menu
        Table buttonTable = new Table();
        buttonTable.center();

        //Set button table Background
        TextureRegionDrawable background = new TextureRegionDrawable(manager.get(PAUSE_BACKGROUND, Texture.class));
        background.setMinWidth(background.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        background.setMinHeight(background.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        buttonTable.setBackground(background);
        buttonTable.setWidth(background.getMinWidth());
        buttonTable.setHeight(background.getMinHeight());

        // X out menu button
        TextureRegionDrawable xIconTexture = new TextureRegionDrawable(manager.get(PAUSE_X_ICON, Texture.class));
        xIconTexture.setMinWidth(xIconTexture.getMinWidth()/INTENDED_WIDTH/2f * canvas.getWidth());
        xIconTexture.setMinHeight(xIconTexture.getMinHeight()/INTENDED_HEIGHT/2f * canvas.getHeight());
        Image xIcon = new Image(xIconTexture, Scaling.fit);
        xIcon.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuXIconClick", GAMEPLAY_MENU_CLICK, false);
                hidePauseTable();
                //todo: playtest to see if we want this or not, or a frozenBeforePause field
//                frozen = false;
            }
        });

        // Level Paused image
        TextureRegionDrawable levelPausedTexture = new TextureRegionDrawable(manager.get(PAUSE_LEVEL_PAUSED, Texture.class));
        levelPausedTexture.setMinWidth(levelPausedTexture.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        levelPausedTexture.setMinHeight(levelPausedTexture.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image levelPaused = new Image(levelPausedTexture, Scaling.fit);

        //Table for left side
        Table leftSide = new Table();
        leftSide.center();
        leftSide.defaults().expandY();

        //Image for right side
        TextureRegionDrawable controlsTexture = new TextureRegionDrawable(manager.get(PAUSE_CONTROLS, Texture.class));
        controlsTexture.setMinWidth(controlsTexture.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        controlsTexture.setMinHeight(controlsTexture.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image controls = new Image(controlsTexture, Scaling.fit);

        //Table for small buttons
        Table smallButtons = new Table();
        smallButtons.center();
        //Create small buttons
        buttonStyle = createButtonStyle(manager, PAUSE_PLAY);
        ImageButton play = new ImageButton(buttonStyle);
        buttonStyle = createButtonStyle(manager, PAUSE_RESET);
        ImageButton reset = new ImageButton(buttonStyle);
        play.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuPlayClick", GAMEPLAY_MENU_CLICK, false);
                hidePauseTable();
                //todo: playtest to see if we want this or not, or a frozenBeforePause field
//                frozen = false;
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                audio.playSound("pauseMenuPlayOver", BUTTON_MOUSEOVER, false);
            }
        });
        reset.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuResetClick", GAMEPLAY_MENU_CLICK, false);
                hidePauseTable();
                reset();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                audio.playSound("pauseMenuResetOver", BUTTON_MOUSEOVER, false);
            }
        });
        smallButtons.add(play).expandX();
        smallButtons.add(reset).expandX();

        //Create Larger Buttons
        buttonStyle = createButtonStyle(manager, PAUSE_LEVEL_SELECT);
        ImageButton levelSelect = new ImageButton(buttonStyle);
        buttonStyle = createButtonStyle(manager, PAUSE_MAIN_MENU);
        ImageButton mainMenu = new ImageButton(buttonStyle);
        //Button Listeners
        levelSelect.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuLevelClick", MENU_CLICK_BACKWARD, false);
                hidePauseTable();
                setFrozen(false);
                listener.exitScreen(game, EXIT_LEVEL);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                audio.playSound("pauseMenuLevelOver", BUTTON_MOUSEOVER, false);
            }
        });
        mainMenu.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playSound("pauseMenuMainClick", MENU_CLICK_BACKWARD, false);
                hidePauseTable();
                setFrozen(false);
                listener.exitScreen(game, EXIT_MENU);
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                audio.playSound("pauseMenuMainOver", BUTTON_MOUSEOVER, false);
            }
        });

        //Add Buttons to Button Table
        buttonTable.defaults().expandY();
        buttonTable.row().padTop(pauseLogoTexture.getMinHeight()/2).padRight(pauseLogoTexture.getMinHeight()/2);
        buttonTable.add(xIcon).right().top().colspan(3);

        buttonTable.row();
        buttonTable.add(levelPaused).colspan(3).top();

        leftSide.row();
        leftSide.add(smallButtons).fillX();

        leftSide.row();
        leftSide.add(levelSelect);

        leftSide.row();
        leftSide.add(mainMenu);
        //TODO: add octopus

        buttonTable.row().padBottom(pauseLogoTexture.getMinHeight()/2);
        buttonTable.add(leftSide).grow();
        buttonTable.add(controls).padRight(pauseLogoTexture.getMinHeight()/2);

        //Set elements position in main group
        buttonTable.setPosition(0,0,Align.center);
        pauseLogo.setPosition(0,buttonTable.getHeight()/2f - pauseLogoTexture.getMinHeight()/4f,Align.center);

        //Add elements to main center group
        mainGroup.addActor(buttonTable);
        mainGroup.addActor(pauseLogo);

        //Add group to main table
        pausedTable.add(mainGroup);
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

    /** TODO: @Stephen Documentation */
    private ImageButton.ImageButtonStyle createButtonStyle(AssetManager manager, String path) {
        Texture buttonTexture = manager.get(path, Texture.class);
        ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(buttonTexture);
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver = new TextureRegionDrawable(buttonTexture).tint(Color.LIGHT_GRAY);
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageOver.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageOver.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        return buttonStyle;
    }
    //#endregion
        //======================================================

    /**
     * Returns a newly loaded texture region for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * whether or not the texture should repeat) after loading.
     *
     * @param manager Reference to global asset manager.
     * @param file The texture (region) file
     * @param repeat Whether the texture should be repeated
     *
     * @return a newly loaded texture region for the given file.
     */
    protected TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
        if (manager.isLoaded(file)) {
            TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (repeat) {
                region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            }
            return region;
        }
        return null;
    }

    /**
     * Returns a newly loaded filmstrip for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * the number of animation frames) after loading.
     *
     * @param manager Reference to global asset manager.
     * @param file The texture (region) file
     * @param rows The number of rows in the filmstrip
     * @param cols The number of columns in the filmstrip
     * @param size The number of frames in the filmstrip
     *
     * @return a newly loaded texture region for the given file.
     */
    protected FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
        if (manager.isLoaded(file)) {
            FilmStrip strip = new FilmStrip(manager.get(file, Texture.class),rows,cols,size);
            strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return strip;
        }
        return null;
    }

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

    //=========================================================================
    //#region Gameplay Loop
    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        teleporterAtlas.dispose();
        exploderAtlas.dispose();
        flyerAtlas.dispose();
        normalEnemyAtlas.dispose();
        armorEnemyAtlas.dispose();
        spikedEnemyAtlas.dispose();
        holeEnemyAtlas.dispose();
        invincibleEnemyAtlas.dispose();
        for(Entity obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        objects = null;
        addQueue = null;
        bounds = null;
        scale  = null;
        world  = null;
        canvas = null;
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public boolean reset() {
        OC.reset();
        EC.reset();
        BC.reset();
        time_counter = 0;
        Vector2 gravity = new Vector2(world.getGravity() );
        for(Entity obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        finish_countdown = TIME_BEFORE_POP_UP;

        world = new World(gravity,false);
        world.setContactListener(CC);
        CC.setComplete(false);
        complete = false;
        setFailure(false);
        selector = new BoxEntity(12,12,SELECTOR_RADIUS*scale.x,SELECTOR_RADIUS*scale.y, Entity.EntityType.SELECTOR);
        selector.setActive(true);
        selector.setSensor(true);
        selector.activatePhysics(world);
        backgroundTexture = currentArea == GameWorld.CAVE ? backgroundTexture1 : backgroundTexture2;
        setFrozen(false);
        canvas.clearStage();
        canvas.addTable(freezeTable);
        paused = false;
        promptForReset = false;
        complete = false;

        objects.add(selector);
        //to scale everything
        setCanvas(canvas);
        //levelloader now sets the bounds of the level as well
        try {
            if(levelEdit){
                LC.bounds = LevelLoader.populateLevel(world, objects, model, scale, OC, EC, BC, currentArea);
            }
            else{
                bounds = LevelLoader.populateLevel(world, objects, model, scale, OC, EC, BC, currentArea);
            }
            setCanvas(canvas);
            frozen = false;
            canvas.resetCameraToWorldPos();
            canvas.setCameraPosInScreen(model.initCamPos);
//            Gdx.input.setCursorPosition((int)(model.initCamPos.x)/2,(int)(Gdx.graphics.getHeight() - model.initCamPos.y/2));
            //canvas.resetCameraToWorldPos();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            listener.exitScreen(this, EXIT_POPULATE_FAILED);
            return false;
        }
    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        InputController input = InputController.getInstance();

        for (OctopusEntity oct : OC.getOctopusList()) {
            oct.setTexture(no_glow);
        }
        for (EnemyEntity enemy : EC.getEnemies()) {
            enemy.setTexture(no_glow);
        }

        //we want the bounds of the camera in box2d space
        Rectangle cb = canvas.cameraBounds();
        Rectangle scaledCameraBounds = new Rectangle(cb.x/scale.x,cb.y/scale.y,
                cb.width/scale.x,cb.height/scale.y);
        input.readInput(scaledCameraBounds, scale);
        if (listener == null) {
            return true;
        }

        // Toggle level editor level testing
        if(!playing && input.didLevelEdit()) {
            if(levelEdit) LC.saveToModel(model);
            levelEdit = !levelEdit;
            AudioController.getInstance().clearMusicBuffer();
            reset();
        }

        // Toggle debug
        if (input.didDebug()) {
            debug = !debug;
        }

        // Handle resets
        if (input.didReset() && !paused && !levelEdit && !complete) {
            if (!promptForReset) {
                showResetTable();
            } else {
                reset();
                hideResetTable();
            }
        }

        //Handle resetting in the reset menu and the pause screen
        if (input.didSelectEscape() && !levelEdit && !complete) {
            if (promptForReset) {
                hideResetTable();
            } else {
                if (paused) {
                    hidePauseTable();
                } else {
                    showPauseTable();
                }
            }
        }

        //Handle world freezing if we are not in a menu
        if (!promptForReset && !paused && !levelEdit && !complete) {
            if (input.didFreeze() && !BC.isRespawnActive()){
                setFrozen(!frozen);
                if (frozen)
                    canvas.addTable(freezeTable);
                else
                    canvas.removeTable(freezeTable);
            } else if (BC.getToFreeze()) {
                if (!frozen)
                    canvas.addTable(freezeTable);
                setFrozen(true);
            } else if (!BC.getToFreeze() && BC.isRespawnActive()) {
                if (frozen)
                    canvas.removeTable(freezeTable);
                setFrozen(false);
            }
        }

//        // Now it is time to maybe switch screens.
//        if (input.didSelectEscape()) {
////            listener.exitScreen(this, EXIT_QUIT);
//            return false;
//        } else
        if (input.didAdvance() && !promptForReset && !paused && !playing) {
            complete = false;
            listener.exitScreen(this, EXIT_NEXT);
            return false;
        } else if (input.didRetreat() && !promptForReset && !paused && !playing) {
            complete = false;
            listener.exitScreen(this, EXIT_PREV);
            return false;
        }

//        if (CC.getCountdown() > 0) {
//            CC.decrementCountdown();
//        } else if (CC.getCountdown() == 0) {
//            if (failed) {
//                reset();
//            } else if (CC.isComplete()) {
//                complete = true;
////                if (!setCurrentLevel(currentLevel+1) || !reset())
////                    listener.exitScreen(this, EXIT_POPULATE_FAILED);
//            }
//        }

        return true;
    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code for the game.
     * This method is called after input is read, but before collisions are resolved.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        InputController input = InputController.getInstance();

        //Update the camera position
        canvas.updateCamera();
        //Update the camera position

        if (!promptForReset && !paused) {
            //Mouse Crosshair and Icon Handling
            if ((!BC.isRespawnActive() && input.isUp()) || input.wIsHeld()) {
                canvas.moveCameraUp((bounds.y + bounds.height) * scale.y);
            }
            if ((!BC.isRespawnActive() &&input.isDown()) || input.sIsHeld()) {
                canvas.moveCameraDown((bounds.y) * scale.y);
            }
            if ((!BC.isRespawnActive() &&input.isLeft()) || input.aIsHeld()) {
                canvas.moveCameraLeft((bounds.x) * scale.x);
            }
            if ((!BC.isRespawnActive() &&input.isRight()) || input.dIsHeld()) {
                canvas.moveCameraRight((bounds.x + bounds.width) * scale.x);
            }
        }

        if (input.didPressLeft()) {
            listener.setCursor(ScreenListener.CursorState.CLICKED);
        } else if (input.didReleaseLeft()) {
            listener.setCursor(ScreenListener.CursorState.DEFAULT);
        }
        Vector2 crosshair = input.getCrossHair();
        selector.setPosition(crosshair.x,crosshair.y);

//        System.out.println(selector.getCollidingWith());
//        System.out.println(selector.getPosition());
        //Updates in sub controllers
        BC.update(dt,frozen, world, OC.getDeadOctopi());
        OC.update(dt,frozen, objects, world,selector);
        EC.update(dt,OC.getOctopusList(),frozen, world);

        //Handle camera movement
        canvas.testBounds();

        updateAudio();
        justSwitched = false;

        // add glows if necessary
        OctopusEntity ao = OC.getActiveOctopus();
        if (ao != null && referenceMode) {
            switch (ao.getOctopusSubType()) {
                case EXPLODER:
                    for (EnemyEntity e : EC.getEnemies()) {
                        switch (e.getEnemySubType()) {
                            case INVINCIBLE_ENEMY:
                            case ARMORED_ENEMY:
                                break;
                            default:
                                e.setTexture(fight_glow);
                        }
                    }
                    break;
                case FLYER:
                    for (EnemyEntity e : EC.getEnemies()) {
                        switch (e.getEnemySubType()) {
                            case INVINCIBLE_ENEMY:
                            case HOLE_ENEMY:
                                break;
                            default:
                                e.setTexture(flight_glow);
                        }
                    }
                    break;
                case TELEPORTER:
                    for (OctopusEntity o : OC.getOctopusList()) {
                        if (o != ao) {
                            o.setTexture(fold_glow);
                        }
                    }
                    for (EnemyEntity e : EC.getEnemies()) {
                        switch (e.getEnemySubType()) {
                            case SPIKED_ENEMY:
                                break;
                            default:
                                e.setTexture(fold_glow);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Processes physics
     *
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        AudioController audio = AudioController.getInstance();
        if(frozen) {
            for (Entity entity : objects) {
                if (entity.isActive() && entity != selector) {
                    entity.getBody().setAwake(false);
                }
            }
        } else {
            canvas.removeTable(freezeTable);
            for (Entity entity : objects) {
                if (entity.isActive()) {
                    entity.getBody().setAwake(true);
                    if (entity.isRemoved()) {
                        entity.setActive(false);
                    }
                    //Clamp all box entities to world
                    if (entity.getEntityType() == Entity.EntityType.ENEMY || entity.getEntityType() == Entity.EntityType.OCTOPUS) {
                        cache.set(entity.getPosition());
                        cache.set(Math.min(Math.max(cache.x, 0), bounds.width), Math.min(Math.max(cache.y, 0), bounds.height));
                        entity.setPosition(cache);
                    }
                }
                if (entity.getOctopusTeleportEntity() != null) {
                    OC.setFoldVelocity(entity.getOctopusTeleportEntity(), entity);
                }
            }
        }
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            LevelLoader.addObject(addQueue.poll(), objects,world, bounds);
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<Entity>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Entity>.Entry entry = iterator.next();
            Entity obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
//                switch(obj.getEntityType()) {
//                    case OCTOPUS:
//                        OC.getOctopusList().remove(obj);
//                        break;
//                    case ENEMY:
//                        EC.getEnemies().remove(obj);
//                        break;
//                    case OBSTACLE:
//                        BC.getObstacleList().remove(obj);
//                        break;
//                }
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }

        //Update on game completion or octopus deaths
        //TODO: Play the last death animation before failure
        if (CC.isComplete()) {
            if (!complete && finish_countdown == TIME_BEFORE_POP_UP) {
                ObstacleEntity goal = CC.getGoalReached();
                int i = 0;
                for (Entity oct : objects) {
                    if (oct.getEntityType() == Entity.EntityType.OCTOPUS) {
                        ((OctopusEntity) oct).setState(BoxEntity.State.win);
                        cache.set((i == 0 ? 0 : i == 1 ? -goal.getTexture().getRegionWidth()/2f : -goal.getTexture().getRegionWidth()/2f)/scale.x,
                                (i == 0 ? 0 : i == 1 ? goal.getTexture().getRegionHeight()/2f : -goal.getTexture().getRegionHeight()/2f)/scale.y)
                                .rotateRad(goal.getAngle());
                        oct.setPosition(goal.getPosition().add(cache));
                        oct.setAngle(0);
                        i++;
                    }
                }
                OC.resetActiveOctopus();
            }
            if (!complete && finish_countdown == TIME_BEFORE_POP_UP)
                audio.playSound("winAnimation", WIN_ANIMATION, false);
            if (finish_countdown < 0) {
                finish_countdown = TIME_BEFORE_POP_UP;
                if (!complete) {
                    canvas.removeTable(freezeTable);
                    canvas.removeTable(pausedTable);
                    canvas.removeTable(resetTable);
                    canvas.addTable(completeTable);
                    audio.playSound("gameWin", GAME_WIN, false);
                    int currentWorldLevelCount = currentArea == GameWorld.CAVE ? levelList.numberCaveLevels :
                            currentArea == GameWorld.SHIP ? levelList.numberShipLevels : levelList.numberOceanLevels;
                    int nextLevel = (currentLevel != currentWorldLevelCount) ? currentLevel + 1 :
                            currentLevel - (currentLevel % 100) + 101;
                    if (nextLevel > saveGame.currentLevel)
                        listener.updateSaveGameLevel(nextLevel);
                }
                complete = true;
            } else {
                finish_countdown -= dt;
            }
            setFailure(false);
            frozen = true;
            Gdx.input.setCursorCatched(false);
        } else if (OC.getDeadOctopi().size() == OC.getOctopusList().size() && !isLevelEdit() && !BC.isRespawnActive()) {
            if (finish_countdown < 0) {
                finish_countdown = TIME_BEFORE_POP_UP;
                if (!complete) {
                    canvas.removeTable(freezeTable);
                    canvas.removeTable(pausedTable);
                    canvas.removeTable(resetTable);
                    canvas.addTable(failedTable);
                }
                complete = true;
                frozen = true;
            } else {
                finish_countdown -= dt;
            }
            setFailure(true);
            Gdx.input.setCursorCatched(false);
        }
    }

    /**
     * Draw the physics objects to the canvas with the part before the
     * octopus and enemies are drawn.
     *
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     *
     * The method draws all objects in the order that they were added.
     *
     * @param delta The drawing context
     */
    public void predraw(float delta) {
        canvas.clear();

        canvas.begin();
        canvas.draw(backgroundTexture, Color.WHITE, 0, 0,bounds.getWidth()*scale.x,bounds.getHeight()*scale.y);
        canvas.end();

        canvas.begin();
        for(Entity obj : objects) {
            // TODO this is very very bad, need to fix
            if (obj.isActive() && obj.getEntityType() != Entity.EntityType.OCTOPUS
                    && obj.getEntityType() != Entity.EntityType.ENEMY
                    && obj.getEntityType() != Entity.EntityType.OBSTACLE)
                obj.draw(canvas);
        }
        canvas.end();
    }

    /**
     * Draw the physics objects to the canvas with the part after the
     * octopus and enemies are drawn.
     *
     * For simple worlds, this method is enough by itself.  It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     *
     * The method draws all objects in the order that they were added.
     *
     * @param delta The drawing context
     */
    public void postdraw(float delta) {
        InputController input = InputController.getInstance();
        // Debug rectangle drawing
        if (debug) {
            canvas.beginDebug();
            for(Entity obj : objects) {
                if (obj.isActive())
                    obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }




        // UI Drawing
        if (!promptForReset && !paused && !complete) {
            // Drawing for reference mode
            canvas.begin();
            if (frozen && OC.getActiveOctopus() != null && referenceMode) {
                cache.set(canvas.getCameraPosInScreen())
                        .sub(canvas.getViewport().getWorldWidth()/2, canvas.getViewport().getWorldHeight()/2)
                        .sub(canvas.getCameraMoveX(), canvas.getCameraMoveY());
//                switch(OC.getActiveOctopus().getOctopusSubType()) {
//                    case EXPLODER:
//                        canvas.draw(fightRef, Color.WHITE,fightRef.getRegionWidth()/1.25f, fightRef.getRegionHeight()/1.6f,
//                                INTENDED_REF_X_POS/INTENDED_WIDTH * canvas.getWidth() + cache.x,
//                                INTENDED_REF_Y_POS/INTENDED_HEIGHT * canvas.getHeight() + cache.y,
//                                fightRef.getRegionWidth()/1.5f, fightRef.getRegionHeight()/1.5f);
//                        break;
//                    case FLYER:
//                        canvas.draw(flightRef, Color.WHITE,fightRef.getRegionWidth()/1.25f, fightRef.getRegionHeight()/1.6f,
//                                INTENDED_REF_X_POS/INTENDED_WIDTH * canvas.getWidth() + cache.x,
//                                INTENDED_REF_Y_POS/INTENDED_HEIGHT * canvas.getHeight() + cache.y,
//                                fightRef.getRegionWidth()/1.5f, fightRef.getRegionHeight()/1.5f);
//                        break;
//                    case TELEPORTER:
//                        canvas.draw(foldRef, Color.WHITE,fightRef.getRegionWidth()/1.25f, fightRef.getRegionHeight()/1.6f,
//                                INTENDED_REF_X_POS/INTENDED_WIDTH * canvas.getWidth() + cache.x,
//                                INTENDED_REF_Y_POS/INTENDED_HEIGHT * canvas.getHeight() + cache.y,
//                                fightRef.getRegionWidth()/1.5f, fightRef.getRegionHeight()/1.5f);
//                        break;
//                }
            }
            canvas.end();

            // Octopi Index-Position Indicator
            if (frozen && (!CC.isComplete() && !complete)) {
                cache.set(canvas.getCameraPosInScreen())
                        .sub(canvas.getCameraMoveX(), canvas.getCameraMoveY());
                canvas.begin();
                for (int i = 0; i < OC.getOctopusList().size(); i++) {
                    OctopusEntity ent = OC.getOctopusList().get(i);
                    if (ent.isActive()) {
                        Vector2 v = ent.getPosition().cpy().scl(scale).sub(cache);
                        float angle = v.angleRad();
                        float dx = Math.min((canvas.getViewport().getWorldWidth()/2),Math.max(-canvas.getViewport().getWorldWidth()/2,v.x))-((float)Math.sin(angle+Math.PI/2))*ent.getHeight()/2;
                        float dy = Math.min((canvas.getViewport().getWorldHeight()/2),Math.max(-canvas.getViewport().getWorldHeight()/2,v.y))+((float)Math.cos(angle+Math.PI/2))*ent.getHeight()/2;
                        // If in screen, then show all of them upwards, otherwise should which direction to trace the octopus
                        if (dx != v.x-((float)Math.sin(angle+Math.PI/2))*ent.getHeight()/2 || dy != v.y+((float)Math.cos(angle+Math.PI/2))*ent.getHeight()/2) {
                            if (i == 0) {
                                canvas.draw(indicator_texture_1.get(ent.getOctopusSubType()),indicator_texture_1.get(ent.getOctopusSubType()).getRegionWidth()/2f,indicator_texture_1.get(ent.getOctopusSubType()).getRegionHeight()/2f,cache.x+dx,cache.y+dy, (float) (angle+Math.PI/2),0.7f,0.6f);
                            } else if (i == 1) {
                                canvas.draw(indicator_texture_2.get(ent.getOctopusSubType()),indicator_texture_2.get(ent.getOctopusSubType()).getRegionWidth()/2f,indicator_texture_2.get(ent.getOctopusSubType()).getRegionHeight()/2f,cache.x+dx,cache.y+dy, (float) (angle+Math.PI/2),0.7f,0.6f);
                            } else {
                                canvas.draw(indicator_texture_3.get(ent.getOctopusSubType()),indicator_texture_3.get(ent.getOctopusSubType()).getRegionWidth()/2f,indicator_texture_3.get(ent.getOctopusSubType()).getRegionHeight()/2f,cache.x+dx,cache.y+dy, (float) (angle+Math.PI/2),0.7f,0.6f);
                            }
                        } else {
                            if (ent.getY()*scale.y+ent.getHeight()/2 < cache.y+canvas.getViewport().getWorldHeight()/2) {
                                if (i == 0) {
                                    canvas.draw(indicator_texture_1.get(ent.getOctopusSubType()),indicator_texture_1.get(ent.getOctopusSubType()).getRegionWidth()/2f,indicator_texture_1.get(ent.getOctopusSubType()).getRegionHeight()/2f,cache.x+v.x,cache.y+v.y+ent.getHeight()/2, 0,0.7f,0.6f);
                                } else if (i == 1) {
                                    canvas.draw(indicator_texture_2.get(ent.getOctopusSubType()),indicator_texture_2.get(ent.getOctopusSubType()).getRegionWidth()/2f,indicator_texture_2.get(ent.getOctopusSubType()).getRegionHeight()/2f,cache.x+v.x,cache.y+v.y+ent.getHeight()/2, 0,0.7f,0.6f);
                                } else {
                                    canvas.draw(indicator_texture_3.get(ent.getOctopusSubType()),indicator_texture_3.get(ent.getOctopusSubType()).getRegionWidth()/2f,indicator_texture_3.get(ent.getOctopusSubType()).getRegionHeight()/2f,cache.x+v.x,cache.y+v.y+ent.getHeight()/2, 0,0.7f,0.6f);
                                }
                            } else {
                                if (i == 0) {
                                    canvas.draw(indicator_texture_1.get(ent.getOctopusSubType()),indicator_texture_1.get(ent.getOctopusSubType()).getRegionWidth()/2f,indicator_texture_1.get(ent.getOctopusSubType()).getRegionHeight()/2f,cache.x+v.x,cache.y+v.y-ent.getHeight()/2, (float) Math.PI,0.7f,0.6f);
                                } else if (i == 1) {
                                    canvas.draw(indicator_texture_2.get(ent.getOctopusSubType()),indicator_texture_2.get(ent.getOctopusSubType()).getRegionWidth()/2f,indicator_texture_2.get(ent.getOctopusSubType()).getRegionHeight()/2f,cache.x+v.x,cache.y+v.y-ent.getHeight()/2, (float) Math.PI,0.7f,0.6f);
                                } else {
                                    canvas.draw(indicator_texture_3.get(ent.getOctopusSubType()),indicator_texture_3.get(ent.getOctopusSubType()).getRegionWidth()/2f,indicator_texture_3.get(ent.getOctopusSubType()).getRegionHeight()/2f,cache.x+v.x,cache.y+v.y-ent.getHeight()/2, (float) Math.PI,0.7f,0.6f);
                                }
                            }
                        }
                    }
                }
                canvas.end();
            }

            // Mouse Reticles
            canvas.begin();
            if (OC.getTeleportSelectionActive()) {
                canvas.draw(teleportSelectorTexture, Color.WHITE, teleportSelectorTexture.getRegionWidth() / 2f,
                        teleportSelectorTexture.getRegionHeight() / 2f, input.getMouse().x,
                        input.getMouse().y, scale.x, scale.y);
            } else if (OC.getTeleportQueued()) {
                for (OctopusEntity o : OC.getOctopusList()) {
                    if (o.isAlive() && o.getOctopusSubType() == OctopusEntity.OctopusSubType.TELEPORTER && o.getTeleportEntity() != null) {
                        canvas.draw(teleportSelectorTexture, Color.WHITE, teleportSelectorTexture.getRegionWidth() / 2f,
                                teleportSelectorTexture.getRegionHeight() / 2f, o.getTeleportEntity().getX() * scale.x,
                                o.getTeleportEntity().getY() * scale.y, scale.x, scale.y);
                    }
                }
                if (!BC.isRespawnActive()) {
                    canvas.draw(crosshairTexture, Color.WHITE, crosshairTexture.getRegionWidth() / 2f, crosshairTexture.getRegionHeight() / 2f, input.getMouse().x,
                            input.getMouse().y, scale.x, scale.y);
                }
            } else {
                if (!BC.isRespawnActive()) {
                    canvas.draw(crosshairTexture, Color.WHITE, crosshairTexture.getRegionWidth() / 2f, crosshairTexture.getRegionHeight() / 2f, input.getMouse().x,
                            input.getMouse().y, scale.x, scale.y);
                }
            }
            canvas.end();
        }

        //Draw other parts of the completed / failed screens
        if (complete && !failed) {
            Vector2 offset = canvas.getCameraPosInScreen().sub(canvas.getViewport().getWorldWidth()/2f, canvas.getViewport().getWorldHeight()/2f);
            canvas.begin();
            canvas.draw(completeBgTexture, Color.WHITE, offset.x, offset.y, canvas.getWidth(), canvas.getHeight());
            for (BubbleGroup bubbleGroup : screenBubbles) {
                bubbleGroup.updatePosition(delta);
                bubbleGroup.draw(canvas, offset.x, offset.y);
            }

            animationVar += delta;
            float ANIMATION_ANGVEL = (2f * (float) Math.PI) / ANIMATION_PERIOD;
            float animationAmplitude = INTENDED_ANIMATION_AMPLITUDE/INTENDED_HEIGHT * canvas.getHeight();
            float heightOff = animationAmplitude * (float) Math.sin(ANIMATION_ANGVEL * animationVar);
            float width = completeOctopi.getRegionWidth()/INTENDED_WIDTH * canvas.getWidth();
            float height = completeOctopi.getRegionHeight()/INTENDED_HEIGHT * canvas.getHeight();
            //not sure why these are voer 4 instead of 2, but it works
            canvas.draw(completeOctopi, Color.WHITE, width/2f, height/2f,
                    canvas.getWidth()/2f + offset.x,
                    (INTENDED_COMPLETE_OCTOPI_POS/INTENDED_HEIGHT * canvas.getHeight()) + offset.y + heightOff, width, height);
            canvas.end();
        } else if (complete) {
            Vector2 offset = canvas.getCameraPosInScreen().sub(canvas.getViewport().getWorldWidth()/2f, canvas.getViewport().getWorldHeight()/2f);
            canvas.begin();
            canvas.draw(failedBgTexture, Color.WHITE, offset.x, offset.y, canvas.getWidth(), canvas.getHeight());
            //TODO: Draw moving enemies
            canvas.end();
        }

//        canvas.drawStage(delta);
    }

    @Override
    public void transitionDraw(float delta) {
        if (!levelEdit) {
            predraw(delta);
//            setCharacterView(delta,frozen);
//            BC.draw(delta, frozen, OC);
//            OC.draw(delta, frozen);
//            EC.draw(delta, frozen, complete || CC.isComplete());
//            postdraw(delta);
        } else {
            setCharacterView(delta,frozen);
            LC.draw(delta, crosshairTexture, backgroundTexture, OC.lineTexture, debug);
        }
    }

    @Override
    public void showTransition() {
        canvas.clearStage();
        canvas.setCameraPosInScreen(model.initCamPos);
        canvas.updateCamera();
    }

    /**
     * TODO: @Stephen Documentation
     */
    private void showResetTable() {
        if (frozen)
            canvas.removeTable(freezeTable);
        canvas.addTable(resetTable);
        setFrozen(true);
        promptForReset = true;
        Gdx.input.setCursorCatched(false);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/3 * 2);
    }

    /**
     * TODO: @Stephen Documentation
     */
    private void hideResetTable() {
        canvas.removeTable(resetTable);
        canvas.addTable(freezeTable);
        promptForReset = false;
        Gdx.input.setCursorCatched(true);
    }

    /**
     * TODO: @Stephen Documentation
     */
    private void showPauseTable() {
        if (frozen)
            canvas.removeTable(freezeTable);
        canvas.addTable(pausedTable);
        setFrozen(true);
        paused = true;
        Gdx.input.setCursorCatched(false);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/3 * 2);
    }

    /**
     * TODO: @Stephen Documentation
     */
    private void hidePauseTable() {
        canvas.removeTable(pausedTable);
        canvas.addTable(freezeTable);
        paused = false;
        Gdx.input.setCursorCatched(true);
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Screen Interface Methods

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        setCanvas(canvas);
        for(Entity obj : objects) {
            // TODO this is very very bad, need to fix
            obj.setDrawScale(scale);
        }
        createPauseTable(manager);
        createResetTable(manager);
        resizeAndRepositionBubbles();
        canvas.clearStage();
        if (!levelEdit) {
            if (paused)
                canvas.addTable(pausedTable);
            else if (promptForReset)
                canvas.addTable(resetTable);
            else if (complete && OC.getDeadOctopi().size() == OC.getOctopusList().size())
                canvas.addTable(failedTable);
            else if (complete)
                canvas.addTable(completeTable);
            else if (frozen)
                canvas.addTable(freezeTable);
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
    public void render(float delta) {
        //TODO: add functionality for saving model
        InputController input = InputController.getInstance();
        if (active) {
            if (!levelEdit) {
                // Get the center position of camera
                time_counter += delta;
                if (preUpdate(delta)) {
                    update(delta); // This is the one that must be defined.
                    postUpdate(delta);
                    predraw(delta);
                    setCharacterView(delta,frozen);
                    BC.draw(delta, frozen,OC);
                    OC.draw(delta, frozen);
                    EC.draw(delta, frozen, complete || CC.isComplete());
                    postdraw(delta);
                    // Draw grabbing effect
                    for (Entity ent : objects) {
                        if (ent.getEntityType() == Entity.EntityType.OCTOPUS && (((OctopusEntity)ent).getTeleportEntity() != null && ((OctopusEntity)ent).getTeleportEntity().isActive())) {
                            if (grabbing_frame >= 0 && grabbing_frame < 35) {
                                Entity teleportedEntity = ((OctopusEntity) ent).getTeleportEntity();
                                grabbingTentacle.setFrame(grabbing_frame/5);
                                if (!frozen) grabbing_frame = (grabbing_frame + 1) % 35;
                                canvas.begin();
                                canvas.draw(grabbingTentacle, com.badlogic.gdx.graphics.Color.WHITE,grabbingTentacle.getRegionWidth()/2f,
                                        grabbingTentacle.getRegionHeight()/2f,teleportedEntity.getX()*scale.x,teleportedEntity.getY()*scale.y,ent.getPosition().sub(teleportedEntity.getPosition()).angleRad(),0.8f,0.8f);
                                canvas.end();
                            }
                        }
                    }
                    if (frozen && !complete && !CC.isComplete()) {
                        cache.set(canvas.getCameraPosInScreen())
                                .sub(canvas.getViewport().getWorldWidth()/2, canvas.getViewport().getWorldHeight()/2)
                                .sub(canvas.getCameraMoveX(), canvas.getCameraMoveY());
                        levelFont = manager.get(FONT_FILE,BitmapFont.class);
                        levelFont.setColor(Color.WHITE);
                        canvas.begin();
                        canvas.drawText("Level "+currentLevel/100+"-"+currentLevel%100,levelFont,100+cache.x,100+cache.y);
                        canvas.end();
                    }
                }
                if (time_counter < 3) {
                    cache.set(canvas.getCameraPosInScreen())
                            .sub(canvas.getViewport().getWorldWidth()/2, canvas.getViewport().getWorldHeight()/2)
                            .sub(canvas.getCameraMoveX(), canvas.getCameraMoveY());
                    beginFont = manager.get(FONT_FILE,BitmapFont.class);
                    if (time_counter < 0.5) beginFont.setColor(Color.WHITE.r,Color.WHITE.g,Color.WHITE.b,time_counter/0.5f);
                    else if (time_counter > 2) beginFont.setColor(Color.WHITE.r,Color.WHITE.g,Color.WHITE.b,1-(time_counter-2));
                    else beginFont.setColor(Color.WHITE.r,Color.WHITE.g,Color.WHITE.b,1);
                    canvas.begin();
                    canvas.drawTextWithWidth("Level "+currentLevel/100+"-"+currentLevel%100,beginFont,(float)(canvas.getWidth())/2+cache.x,(float)(canvas.getHeight())/2+cache.y);
                    canvas.end();
                }
                canvas.drawStage(delta);
            } else {
                if(LC.getLoadLevelFlagged() != null){
                    try {
                        model = LevelLoader.parseJson(LC.getLoadLevelFlagged());
                        reset();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (preUpdate(delta)) {
                    LC.update(selector, delta, world, model);
                    setCharacterView(delta,frozen);
                }
                LC.draw(delta, crosshairTexture, backgroundTexture, OC.lineTexture, debug);
            }
        }
    }

    public void setCharacterView(float delta, boolean frozen) {
        for (Entity entity : objects) {
            if (entity.characterView==null) {
                switch (entity.getEntityType()) {
                    case OCTOPUS:
                        entity.characterView = new OctopusView(octopusSkeletonData,octopusAnimationStateData,octopusStates,(OctopusEntity)entity,scale);
                        break;
                    case ENEMY:
                        entity.characterView = new EnemyView(enemySkeletonData,enemyAnimationStateData,enemyStates,(EnemyEntity)entity,scale);
                        break;
                    default:
                        break;
                }
            }
            if (entity.characterView!=null) {
//                if (((BoxEntity)entity).state== BoxEntity.State.death) {
//                    System.out.println("dead");
//                }
                entity.characterView.update(delta,frozen);
            }
        }
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        if (!active) {
            // Useless if called in outside animation loop
            AudioController audio = AudioController.getInstance();
            active = true;
            justSwitched = true;
            canvas.clearStage();
            if (frozen && playing)
                canvas.addTable(freezeTable);
            audio.clearMusicBuffer();
            if (playing)
                enqueueMusic();
            if (audio.isFoleyPlaying())
                audio.stopFoley();
        }
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
        canvas.resetCameraToWindowSize();
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Music Code
    //@author Stephen Chin
    /**
     * Sets the current area of the game world. Used for world specific actions, such as changing music.
     *
     * @param area The current area of the game world.
     */
    public void setArea(GameWorld area) {
        currentArea = area;
    }

    /**
     * Updates the music and sound effects in the world.
     */
    private void updateAudio() {
        AudioController audio = AudioController.getInstance();
        if ((OC.changedMusicState() || justSwitched) && playing ) {
            String filepath = currentArea == GameWorld.CAVE ? CAVE_MUSIC_BASE_PATH :
                    currentArea == GameWorld.SHIP ? SHIP_MUSIC_BASE_PATH : OCEAN_MUSIC_BASE_PATH;
            if (justSwitched) {
                audio.playMusic(filepath + musicStateToString(OC.getMusicState()) + ".ogg");
            } else {
                audio.playMusicFromSpot(filepath + musicStateToString(OC.getMusicState()) + ".ogg");
            }
        }
        audio.update();
    }

    /**
     * Converts a music state into its corresponding filename encoding.
     *
     * @param currentMusicState The current music state of the game.
     * @return The string representation of the state as per filename guidelines.
     */
    public String musicStateToString(OctopusController.MusicState currentMusicState) {
        switch(currentMusicState) {
            case ALL:
                return "all"; //TODO: Make final variables
            case NO_FIGHT:
                return "nofight";
            case NO_FLIGHT:
                return "noflight";
            case NO_FOLD:
                return "nofold";
            case FOLD:
                return "fold";
            case FIGHT:
                return "fight";
            case FLIGHT:
                return "flight";
            case NONE:
                return "none";
        }
        return null;
    }

    /**
     * Preloads all the music related to the current area and places it into the current audio buffer.
     */
    private void preloadMusic() {
        AudioController audio = AudioController.getInstance();
        String filepath = currentArea == GameWorld.CAVE ? CAVE_MUSIC_BASE_PATH :
                currentArea == GameWorld.SHIP ? SHIP_MUSIC_BASE_PATH : OCEAN_MUSIC_BASE_PATH;
        audio.loadAsyncAudio(filepath + "all" + ".ogg");
        audio.loadAsyncAudio(filepath + "noflight" + ".ogg");
        audio.loadAsyncAudio(filepath + "nofold" + ".ogg");
        audio.loadAsyncAudio(filepath + "nofight" + ".ogg");
        audio.loadAsyncAudio(filepath + "fold" + ".ogg");
        audio.loadAsyncAudio(filepath + "fight" + ".ogg");
        audio.loadAsyncAudio(filepath + "flight" + ".ogg");
        audio.loadAsyncAudio(filepath + "none" + ".ogg");
    }

    /**
     * Enqueues all the music related to the current area into the current audio buffer.
     * Music files must have been loaded.
     */
    private void enqueueMusic() {
        AudioController audio = AudioController.getInstance();
        String filepath = currentArea == GameWorld.CAVE ? CAVE_MUSIC_BASE_PATH :
                currentArea == GameWorld.SHIP ? SHIP_MUSIC_BASE_PATH : OCEAN_MUSIC_BASE_PATH;
        audio.enqueueMusicToBuffer(filepath + "all" + ".ogg");
        audio.enqueueMusicToBuffer(filepath + "noflight" + ".ogg");
        audio.enqueueMusicToBuffer(filepath + "nofold" + ".ogg");
        audio.enqueueMusicToBuffer(filepath + "nofight" + ".ogg");
        audio.enqueueMusicToBuffer(filepath + "fold" + ".ogg");
        audio.enqueueMusicToBuffer(filepath + "fight" + ".ogg");
        audio.enqueueMusicToBuffer(filepath + "flight" + ".ogg");
        audio.enqueueMusicToBuffer(filepath + "none" + ".ogg");
    }
    //#endregion
    //=================================

}
