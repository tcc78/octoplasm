package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.octoplasm.util.AudioController;
import edu.cornell.gdiac.octoplasm.util.LevelList;
import edu.cornell.gdiac.octoplasm.util.SaveGame;
import edu.cornell.gdiac.octoplasm.util.ScreenListener;

import java.util.Deque;
import java.util.LinkedList;

/**
 * The class that handles the level selection screen.
 *
 * @author Stephen Chin
 */
public class LevelSelectMode extends SuperController implements Screen {
    //================================================================================
    //#region Fields
    /**
     * The current level select world selection screen.
     */
    private enum WorldSelection {
        /** The Cave world level select screen */
        CAVE,
        /** The Ship world level select screen */
        SHIP,
        /** The Ocean world level select screen */
        OCEAN
    }

    /** Exit code for returning to the main menu screen */
    public static final int EXIT_BACK = 0;
    /** Exit code for going to the cave world. Cave world level xx given in exit code 1xx. */
    public static final int CAVE_EXIT_CODE = 100;
    /** Exit code for going to the ship world. Ship world level xx given in exit code 2xx. */
    public static final int SHIP_EXIT_CODE = 200;
    /** Exit code for going to the ocean world. Ocean world level xx given in exit code 3xx. */
    public static final int OCEAN_EXIT_CODE = 300;

    /** The intended pixel width of the game screen, stored as a variable for resizing. */
    private static final float INTENDED_WIDTH = 1920f;
    /** The intended pixel height of the game screen, stored as a variable for resizing. */
    private static final float INTENDED_HEIGHT = 1080f;
    /** The intended pixel space between the level icon box and the text box. */
    private static final float INTENDED_LEVEL_ICON_HEIGHT = 200f;
    /** The intended pixel width of the middle text box. */
    private static final float INTENDED_MIDDLE_BOX_WIDTH = 1236f;
    /** The intended pixel space between the play button and the bottom of the text box. */
    private static final float INTENDED_PLAY_BOTTOM_PADDING = 53f;
    /** The intended pixel space between the play button and the right side of the text box. */
    private static final float INTENDED_PLAY_RIGHT_PADDING = 72f;
    /** The intended pixel space between the flavor text and the left side of the text box. */
    private static final float INTENDED_TEXT_LEFT_PADDING = 50f;
    /** The intended pixel space between the button table and the text box. */
    private static final float INTENDED_BUTTON_TABLE_BOTTOM_PADDING = 40;
    /** The intended pixel space between the button table and the select level text. */
    private static final float INTENDED_BUTTON_TABLE_TOP_PADDING = 75f;
    /** The intended pixel space between the master table and the top of the screen. */
    private static final float INTENDED_MASTER_TABLE_TOP_PADDING = 96f;
    /** The intended pixel space between the back button and the left side of the screen. */
    private static final float INTENDED_BACK_BUTTON_LEFT_PADDING = 92.69f;
    /** The intended pixel space between the forward button and the right side of the screen. */
    private static final float INTENDED_FORWARD_BUTTON_RIGHT_PADDING = 104.08f;
    /** The intended pixel space between the dots on the bottom of the screen. */
    private static final float INTENDED_DOTS_MIDDLE_PADDING = 68.37f;
    /** The intended pixel space between the bottom dots and the bottom side of the screen. */
    private static final float INTENDED_DOTS_BOTTOM_PADDING = 69.37f;
    /** The intended pixel space between the bottom dots and the text box. */
    private static final float INTENDED_DOTS_TOP_PADDING = 69.37f;

    /** How large the small title text should be  */
    private static final int INTENDED_BOLD_FONT_SIZE = 35;
    /** How large the normal text should be */
    private static final int INTENDED_TEXT_FONT_SIZE = 30;
    /** How large the level icon font text should be */
    private static final int INTENDED_LEVEL_ICON_FONT_SIZE = 120;
    /** Placeholder for the default selected level */
    private static final int DEFAULT_SELECTED_LEVEL = CAVE_EXIT_CODE + 1; //TODO: Change

    /** Top padding of the level icon image file */
    private static final int LEVEL_ICON_TOP_PADDING = 4;
    /** Left padding of the level icon image file */
    private static final int LEVEL_ICON_LEFT_PADDING = 4;
    /** Bottom padding of the level icon image file */
    private static final int LEVEL_ICON_BOTTOM_PADDING = 11;
    /** Right padding of the level icon image file */
    private static final int LEVEL_ICON_RIGHT_PADDING = 11;

    /** Reference to the cave background */
    private static final String CAVE_BACKGROUND = "backgrounds/caveConcept.png";
    /** Reference to the ship background */
    private static final String SHIP_BACKGROUND = "backgrounds/shipConcept.png";
    /** Reference to the ocean background */
    private static final String OCEAN_BACKGROUND = "backgrounds/caveConcept.png"; //TODO: fill once ocean concept art comes out

    /** Reference to the font for the bold text in the level details */
    private static final String RUBIK_ONE_FONT_FILE = "ui/RubikOne-Regular.ttf";
    /** Reference to the font for regular text in the level details */
    private static final String RUBIK_FONT_FILE = "ui/Rubik-Regular.ttf";
    /** Reference to the font for level button icons */
    private static final String ROBOTO_FONT_FILE = "ui/Roboto-Black.ttf";
    /** Reference to the arrow indicator for going back to the main menu */
    private static final String LEFT_ARROW_FILE = "ui/level_select/back_arrow.png";
    /** Reference to the triangle indicator for going forward to the next area */
    private static final String RIGHT_ARROW_FILE = "ui/level_select/world_arrow.png";
    /** Reference to the locked level button for selecting levels */
    private static final String LOCKED_LEVEL = "ui/level_select/locked_level.png";
    /** Reference to the cave level icon button texture. */
    private static final String LEVEL_ICON_CAVE = "ui/level_select/level_icon_cave.png";
    /** Reference to the ship level icon button texture. */
    private static final String LEVEL_ICON_SHIP = "ui/level_select/level_icon_ship.png";
    /** Reference to the play button texture for the cave world */
    private static final String PLAY_BUTTON_CAVE = "ui/level_select/play_button_cave.png";
    /** Reference to the play button texture for the ship world*/
    private static final String PLAY_BUTTON_SHIP = "ui/level_select/play_button_ship.png";
    /** Reference to the select level text texture */
    private static final String SELECT_LEVEL_TEXT = "ui/level_select/select_level.png";
    /** Reference to the text box background texture */
    private static final String TEXT_BOX_BG = "ui/level_select/textbox.png";
    /**  */
    private static final String DOT_ICON_UNSELECTED = "ui/level_select/dot_unchecked.png";
    /**  */
    private static final String DOT_ICON_SELECTED = "ui/level_select/dot_checked.png";

    /** Reference to the menu click forward sound. */
    private static final String MENU_CLICK_FORWARD = "sounds/ui/menu_click_forward.wav";
    /** Reference to the menu click backward sound. */
    private static final String MENU_CLICK_BACKWARD = "sounds/ui/menu_click_backward.wav";
    /** Reference to the level select mouse over sound. */
    private static final String BUTTON_MOUSEOVER = "sounds/ui/button_mouseover.wav";
    /** Reference to the level select icon click sound. */
    private static final String LEVEL_SELECT_ICON_CLICK = "sounds/ui/level_select_click.wav";
    /** Reference to the main menu music. */
    private static final String MENU_MUSIC = "music/main_menu.ogg"; //TODO: Decide if using the same music for both screens

    /** Placeholder text for the first Title for the level */
    private static final String PLACEHOLDER_TITLE_1 = "Level ";
    /** Placeholder text for the first description for the level */
    private static final String PLACEHOLDER_TEXT_1 = "Just another day as an octopus";
    /** Placeholder text for the second title for the level */
    private static final String PLACEHOLDER_TITLE_2 = "Octopus Time";
    /** Placeholder text for the second description for the level */
    private static final String PLACEHOLDER_TEXT_2 = "";
    /** Text for the player if the selected level failed to load. */
    private static final String LOAD_FAILED_TITLE = "Level Failed to load.";
    /**  */
    private static final String POPULATE_FAILED_TITLE = "Failed to populate level.";
    /**  */
    private static final String POPULATE_FAILED_TEXT = "There may be something wrong with this level file.";

    /** AssetManager to be loading in the background */
    private AssetManager manager;
    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Track all loaded assets (for unloading purposes) */
    protected Array<String> assets;
    /** Whether or not this is an active controller */
    private boolean active;

    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;

    /** The background texture for when the cave area is selected. */
    private Texture caveBackground;
    /** The background texture for when the ship area is selected. */
    private Texture shipBackground;
    /** The background texture for when the ocean area is selected. */
    private Texture oceanBackground;

    /** The display font for the bolded text in the box */
    private BitmapFont boldTextDisplayFont;
    /** The display font for the normal text in the box */
    private BitmapFont normalTextDiplayFont;
    /** The display font for the button number text */
    private BitmapFont buttonNumberFont;

    /** The button group for holding all the level buttons. Used for setting and unsetting. */
    private ButtonGroup<ImageButton> levelButtonGroup;

    /** The main table for the screen ui elements */
    private Table masterTable;
    /** The table holding all the level icons for the cave. Held as a field for toggling visibility. */
    private Table caveLevelIcons;
    /** The table holding all the level icons for the ship. Held as a field for toggling visibility. */
    private Table shipLevelIcons;
    /** The table holding all the level icons for the ocean. Held as a field for toggling visibility. */
    private Table oceanLevelIcons;

    /** The level play button for the cave. Reference kept for disabling. */
    private ImageButton playButtonCave;
    /** The level play button for the ship. Reference kept for disabling. */
    private ImageButton playButtonShip;
    /** The level play button for the ocean. Reference kept for disabling. */
    private ImageButton playButtonOcean;

    /**  */
    private ImageButton backwardButton;
    /**  */
    private ImageButton forwardButton;

    /**  */
    private ImageButton caveDot;
    /**  */
    private ImageButton shipDot;
    /**  */
    private ImageButton oceanDot;

    /**  */
    private ImageButton[] caveLevelButtons;
    /**  */
    private ImageButton[] shipLevelButtons;
    /**  */
    private ImageButton[] oceanLevelButtons;
    /**  */
    private Label[] caveLevelButtonLabels;
    /**  */
    private Label[] shipLevelButtonLabels;
    /**  */
    private Label[] oceanLevelButtonLabels;

    /** Reference to the first title label for changing the active text. */
    private Label firstTitle;
    /** Reference to the first info label for changing the active text. */
    private Label firstInfo;
    /** Reference to the second title label for changing the active text. */
    private Label secondTitle;
    /** Reference to the second info label for changing the active text. */
    private Label secondInfo;

    /** The currently selected level, encoded in an exit code. */
    private int selectedLevel;
    /**  */
    private int mouseOverSelected;
    /** The level list information, including level names, flavor text, and numbers of levels in each world. */
    private LevelList levelList;
    /**  */
    private SaveGame saveGame;
    /** The current world selection screen. */
    private WorldSelection currentWorldSelection;
    //#endregion
    //=================================

    //================================================================================
    //#region Constructor
    /**
     * Creates a new Level Selection Screen
     *
     * @param canvas The game canvas for drawing
     * @param manager The asset manager for loading and getting textures
     */
    public LevelSelectMode(GameCanvas canvas, AssetManager manager) {
        this.manager = manager;
        this.canvas = canvas;

        mouseOverSelected = -1;
        scale = new Vector2(1,1);
        assets = new Array<>();
        bounds = new Rectangle();
        selectedLevel = DEFAULT_SELECTED_LEVEL;
        currentWorldSelection = WorldSelection.CAVE; //TODO: Change to area of players current level
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
    public boolean isActive() {
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
        this.scale.x = canvas.getWidth()/bounds.getWidth();
        this.scale.y = canvas.getHeight()/bounds.getHeight();
    }

    /**
     * Sets the ScreenListener for this mode.
     *
     * The ScreenListener will respond to requests to quit.
     *
     * @param listener The new screen listener for this mode.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the text in the text box of the level to reflect the fact that a level failed to load.
     */
    public void setLoadFailedText() {
        selectedLevel = -1;
        firstTitle.setText(LOAD_FAILED_TITLE);
        firstInfo.setText("");
        secondTitle.setText("");
        secondInfo.setText("");
    }

    /** Sets the text in the text box of the level to reflect the fact that a level failed to be populated. */
    public void setPopulateFailedText() {
        selectedLevel = -1;
        firstTitle.setText(POPULATE_FAILED_TITLE);
        firstInfo.setText(POPULATE_FAILED_TEXT);
        secondTitle.setText("");
        secondInfo.setText("");
    }

    /**
     *
     */
    public void setSelectedLevel(int selectedLevel) {
        if (selectedLevel / 100 == 1 && selectedLevel % 100 <= levelList.numberCaveLevels
                && !caveLevelButtons[(selectedLevel % 100) - 1].isDisabled()) {
            this.selectedLevel = selectedLevel;
            caveLevelButtons[(selectedLevel % 100) - 1].setChecked(true);
        } else if (selectedLevel / 100 == 2 && selectedLevel % 100 <= levelList.numberShipLevels
                && !shipLevelButtons[(selectedLevel % 100) - 1].isDisabled()) {
            this.selectedLevel = selectedLevel;
            shipLevelButtons[(selectedLevel % 100) - 1].setChecked(true);
        } else if (selectedLevel / 100 == 3 && selectedLevel % 100 <= levelList.numberOceanLevels
                && !oceanLevelButtons[(selectedLevel % 100) - 1].isDisabled()) {
            this.selectedLevel = selectedLevel;
            oceanLevelButtons[(selectedLevel % 100) - 1].setChecked(true);
        }
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

    //================================================================================
    //#region Asset Loading
    /**
     * Preloads the assets for this controller.
     *
     * @param manager Reference to global asset manager.
     */
    public void preLoadContent(AssetManager manager) {
        manager.load(LEFT_ARROW_FILE, Texture.class);
        assets.add(LEFT_ARROW_FILE);

        manager.load(RIGHT_ARROW_FILE, Texture.class);
        assets.add(LEFT_ARROW_FILE);

        manager.load(CAVE_BACKGROUND, Texture.class);
        assets.add(CAVE_BACKGROUND);

        manager.load(SHIP_BACKGROUND, Texture.class);
        assets.add(SHIP_BACKGROUND);

        manager.load(OCEAN_BACKGROUND, Texture.class);
        assets.add(OCEAN_BACKGROUND);

        manager.load(LOCKED_LEVEL, Texture.class);
        assets.add(LOCKED_LEVEL);

        manager.load(PLAY_BUTTON_CAVE, Texture.class);
        assets.add(PLAY_BUTTON_CAVE);

        manager.load(PLAY_BUTTON_SHIP, Texture.class);
        assets.add(PLAY_BUTTON_SHIP);

        manager.load(SELECT_LEVEL_TEXT, Texture.class);
        assets.add(SELECT_LEVEL_TEXT);

        manager.load(TEXT_BOX_BG, Texture.class);
        assets.add(TEXT_BOX_BG);

        manager.load(DOT_ICON_UNSELECTED, Texture.class);
        assets.add(DOT_ICON_UNSELECTED);

        manager.load(DOT_ICON_SELECTED, Texture.class);
        assets.add(DOT_ICON_SELECTED);

        manager.load(LEVEL_ICON_CAVE, Texture.class);
        assets.add(LEVEL_ICON_CAVE);

        manager.load(LEVEL_ICON_SHIP, Texture.class);
        assets.add(LEVEL_ICON_SHIP);

        //Music
        manager.load(MENU_MUSIC, AudioSource.class);
        assets.add(MENU_MUSIC);

        //Sounds
        manager.load(MENU_CLICK_FORWARD, Sound.class);
        assets.add(MENU_CLICK_FORWARD);

        manager.load(MENU_CLICK_BACKWARD, Sound.class);
        assets.add(MENU_CLICK_BACKWARD);

        manager.load(BUTTON_MOUSEOVER, Sound.class);
        assets.add(BUTTON_MOUSEOVER);

        manager.load(LEVEL_SELECT_ICON_CLICK, Sound.class);
        assets.add(LEVEL_SELECT_ICON_CLICK);

        //Fonts
        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = RUBIK_ONE_FONT_FILE;
        size2Params.fontParameters.size = (int) (INTENDED_BOLD_FONT_SIZE * (canvas.getWidth() / INTENDED_WIDTH));
        manager.load(RUBIK_ONE_FONT_FILE, BitmapFont.class, size2Params);
        assets.add(RUBIK_ONE_FONT_FILE);

        size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = RUBIK_FONT_FILE;
        size2Params.fontParameters.size = (int) (INTENDED_TEXT_FONT_SIZE * (canvas.getWidth() / INTENDED_WIDTH));
        manager.load(RUBIK_FONT_FILE, BitmapFont.class, size2Params);
        assets.add(RUBIK_FONT_FILE);

        size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = ROBOTO_FONT_FILE;
        size2Params.fontParameters.size = (int) Math.ceil(INTENDED_LEVEL_ICON_FONT_SIZE * (canvas.getWidth() / INTENDED_WIDTH));
        manager.load(ROBOTO_FONT_FILE, BitmapFont.class, size2Params);
        assets.add(ROBOTO_FONT_FILE);
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

        // Asset creation
        caveBackground = manager.get(CAVE_BACKGROUND, Texture.class);
        shipBackground = manager.get(SHIP_BACKGROUND, Texture.class);
        oceanBackground = manager.get(OCEAN_BACKGROUND, Texture.class);

        // Text font creation
        boldTextDisplayFont = manager.isLoaded(RUBIK_ONE_FONT_FILE) ? manager.get(RUBIK_ONE_FONT_FILE, BitmapFont.class) : null;
        if (boldTextDisplayFont != null)
            boldTextDisplayFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        normalTextDiplayFont = manager.isLoaded(RUBIK_FONT_FILE) ? manager.get(RUBIK_FONT_FILE, BitmapFont.class) : null;
        if (normalTextDiplayFont != null)
            normalTextDiplayFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        buttonNumberFont = manager.isLoaded(ROBOTO_FONT_FILE) ? manager.get(ROBOTO_FONT_FILE, BitmapFont.class) : null;
        if (buttonNumberFont != null)
            buttonNumberFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        audio.allocateSound(manager, MENU_CLICK_FORWARD);
        audio.allocateSound(manager, MENU_CLICK_BACKWARD);
        audio.allocateSound(manager, BUTTON_MOUSEOVER);
        audio.allocateSound(manager, LEVEL_SELECT_ICON_CLICK);

        //TODO: Load current Level and set
        createMasterTable();
        updateFlavorText();
    }

        //===========================================================
    //#region Table Creation
    /**
     * Creates and populates the main master table with the proper values.
     */
    private void createMasterTable() {
        //Reusable variables
        final LevelSelectMode levelSelect = this;
        final AudioController audio = AudioController.getInstance();
        ImageButton.ImageButtonStyle buttonStyle;

        //Initialize main table
        masterTable = new Table();
        masterTable.setFillParent(true);

        //Top Left Button
        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(LEFT_ARROW_FILE, Texture.class));
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        final ImageButton backButton = new ImageButton(buttonStyle);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.exitScreen(levelSelect, EXIT_BACK);
                audio.playSound("levelSelectBackButton", MENU_CLICK_BACKWARD, false);
            }
        });

        //Top Middle Select Level Text
        TextureRegionDrawable levelText = new TextureRegionDrawable(manager.get(SELECT_LEVEL_TEXT, Texture.class));
        levelText.setMinWidth(levelText.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        levelText.setMinHeight(levelText.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        Image labelSelectLevel = new Image(levelText, Scaling.fit);

        //Middle Left Arrow & Middle Right Arrow
        TextureRegion leftButton = new TextureRegion(manager.get(RIGHT_ARROW_FILE, Texture.class));
        leftButton.flip(true, false);

        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(leftButton);
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        backwardButton = new ImageButton(buttonStyle);

        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(RIGHT_ARROW_FILE, Texture.class));
        buttonStyle.imageDisabled = new TextureRegionDrawable(manager.get(RIGHT_ARROW_FILE, Texture.class)).tint(Color.GRAY);
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageDisabled.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageDisabled.setMinHeight(buttonStyle.imageUp.getMinHeight());
        forwardButton = new ImageButton(buttonStyle);

        forwardButton.setVisible(currentWorldSelection != WorldSelection.SHIP);
        forwardButton.setDisabled(currentWorldSelection == WorldSelection.CAVE && saveGame.currentLevel < 200 ||
                currentWorldSelection == WorldSelection.SHIP && saveGame.currentLevel < 300);
        backwardButton.setVisible(currentWorldSelection != WorldSelection.CAVE);

        forwardButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!forwardButton.isDisabled()) {
                    if (currentWorldSelection == WorldSelection.CAVE) {
                        currentWorldSelection = WorldSelection.SHIP;
                        if (shipLevelButtons.length > 0) {
                            shipLevelButtons[0].setChecked(true);
                            selectedLevel = 201;
                        } else {
                            unCheckAll();
                        }
                    } else if (currentWorldSelection == WorldSelection.SHIP) {
                        currentWorldSelection = WorldSelection.OCEAN;
                        if (oceanLevelButtons.length > 0) {
                            oceanLevelButtons[0].setChecked(true);
                            selectedLevel = 301;
                        } else {
                            unCheckAll();
                        }
                    }
                    audio.playSound("levelSelectForwardButton", MENU_CLICK_FORWARD, false);
                }
            }
        });
        backwardButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentWorldSelection == WorldSelection.SHIP) {
                    currentWorldSelection = WorldSelection.CAVE;
                    if (caveLevelButtons.length > 0) {
                        caveLevelButtons[0].setChecked(true);
                        selectedLevel = 101;
                    } else {
                        unCheckAll();
                    }
                } else if (currentWorldSelection == WorldSelection.OCEAN) {
                    currentWorldSelection = WorldSelection.SHIP;
                    if (shipLevelButtons.length > 0) {
                        shipLevelButtons[0].setChecked(true);
                        selectedLevel = 201;
                    } else {
                        unCheckAll();
                    }
                }
                audio.playSound("levelSelectBackwardButton", MENU_CLICK_BACKWARD, false);
            }
        });

        //Middle Middle Table filling
        Table middleMaster = createMiddleMasterTable();

        //Bottom Middle dots for world selection
        Table dotSelection = new Table();

        Color buttonColor = new Color(Color.SCARLET).sub(0,0,0,0.5f);
        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(DOT_ICON_UNSELECTED, Texture.class));
        buttonStyle.imageChecked = new TextureRegionDrawable(manager.get(DOT_ICON_SELECTED, Texture.class));
        buttonStyle.imageDisabled = new TextureRegionDrawable(manager.get(DOT_ICON_UNSELECTED, Texture.class)).tint(buttonColor); //TODO: Change to a red
        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH/2f * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT/2f * canvas.getHeight());
        buttonStyle.imageChecked.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageChecked.setMinHeight(buttonStyle.imageUp.getMinHeight());
        buttonStyle.imageDisabled.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageDisabled.setMinHeight(buttonStyle.imageUp.getMinHeight());
        caveDot = new ImageButton(buttonStyle);

        buttonStyle = new ImageButton.ImageButtonStyle(buttonStyle);
        shipDot = new ImageButton(buttonStyle);

        buttonStyle = new ImageButton.ImageButtonStyle(buttonStyle);
        oceanDot = new ImageButton(buttonStyle);

        caveDot.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!caveDot.isDisabled()) {
                    currentWorldSelection = WorldSelection.CAVE;
                    if (caveLevelButtons.length > 0) {
                        caveLevelButtons[0].setChecked(true);
                        selectedLevel = 101;
                    } else {
                        unCheckAll();
                    }
                    audio.playSound("levelSelectDot", MENU_CLICK_FORWARD, false);
                }
            }
        });
        shipDot.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!shipDot.isDisabled()) {
                    currentWorldSelection = WorldSelection.SHIP;
                    if (shipLevelButtons.length > 0) {
                        shipLevelButtons[0].setChecked(true);
                        selectedLevel = 201;
                    } else {
                        unCheckAll();
                    }
                    audio.playSound("levelSelectDot", MENU_CLICK_FORWARD, false);
                }
            }
        });
        oceanDot.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!oceanDot.isDisabled()) {
                    currentWorldSelection = WorldSelection.OCEAN;
                    if (oceanLevelButtons.length > 0) {
                        oceanLevelButtons[0].setChecked(true);
                        selectedLevel = 301;
                    } else {
                        unCheckAll();
                    }
                    audio.playSound("levelSelectDot", MENU_CLICK_FORWARD, false);
                }
            }
        });

        //Only one of these buttons can be selected at a time
        ButtonGroup<ImageButton> dotButton = new ButtonGroup<>();
        dotButton.add(caveDot, shipDot, oceanDot);

        dotSelection.defaults()
                .padRight(Value.percentHeight(INTENDED_DOTS_MIDDLE_PADDING/INTENDED_HEIGHT, masterTable));
        dotSelection.add(caveDot);
        dotSelection.add(shipDot)
                .padRight(0);

        //Adding all the assets to the mainTable
        masterTable.row()
                .padTop(Value.percentHeight(INTENDED_MASTER_TABLE_TOP_PADDING/INTENDED_HEIGHT, masterTable));
        masterTable.add(backButton)
                .padLeft(Value.percentWidth(INTENDED_BACK_BUTTON_LEFT_PADDING/INTENDED_WIDTH, masterTable));
        masterTable.add(labelSelectLevel).center();
        masterTable.add();
        masterTable.row();
        masterTable.add(backwardButton).center().right();
        masterTable.add(middleMaster).grow();
        masterTable.add(forwardButton).center()
                .padRight(Value.percentWidth(INTENDED_FORWARD_BUTTON_RIGHT_PADDING/INTENDED_WIDTH, masterTable));
        masterTable.row()
                .padBottom(Value.percentHeight(INTENDED_DOTS_BOTTOM_PADDING/INTENDED_HEIGHT, masterTable))
                .padTop(Value.percentHeight(INTENDED_DOTS_TOP_PADDING/INTENDED_HEIGHT, masterTable));
        masterTable.add();
        masterTable.add(dotSelection).center();
        masterTable.add();

//        middleMaster.setDebug(true,true);
//        masterTable.setDebug(true, true);
    }

    /**
     * Creates the master table for the middle section of the ui. Contains Level Icons and Level Text.
     *
     * @return Reference to the completed middle master table.
     */
    private Table createMiddleMasterTable() {
        //Create Table
        Table middleMaster = new Table();

        //Create Level Button Group
        levelButtonGroup = new ButtonGroup<>();

        //Create level icon tables
        Stack levelButtonStack = new Stack();
        caveLevelIcons = createLevelIconTable(levelList.numberCaveLevels, WorldSelection.CAVE);
        shipLevelIcons = createLevelIconTable(levelList.numberShipLevels, WorldSelection.SHIP);
        oceanLevelIcons = createLevelIconTable(levelList.numberOceanLevels, WorldSelection.OCEAN);
        levelButtonStack.add(oceanLevelIcons);
        levelButtonStack.add(shipLevelIcons);
        levelButtonStack.add(caveLevelIcons);
        caveLevelIcons.setVisible(currentWorldSelection == WorldSelection.CAVE);
        shipLevelIcons.setVisible(currentWorldSelection == WorldSelection.SHIP);
//        oceanLevelIcons.setVisible(currentWorldSelection == WorldSelection.OCEAN);
        oceanLevelIcons.setVisible(false);
        updateDisabledLevels();

        //Middle level info selection
        Table middleTextTable = createMiddleTextTable();

        //Add sub tables to Middle Master
        middleMaster.add(levelButtonStack).uniformX().fillX()
                .padBottom(Value.percentHeight(INTENDED_BUTTON_TABLE_BOTTOM_PADDING/INTENDED_HEIGHT, masterTable))
                .padTop(Value.percentHeight(INTENDED_BUTTON_TABLE_TOP_PADDING/INTENDED_HEIGHT, masterTable));
        middleMaster.row();
        middleMaster.add(middleTextTable).uniformX();

        return middleMaster;
    }

    /**
     * Creates the level icon area containing the clickable buttons for selecting each level.
     *
     * @param numberLevelIcons The number of level icons to place inside the level icon table.
     * @param world The world we are currently creating buttons for.
     * @return Reference to the completed level icon table.
     */
    private Table createLevelIconTable(int numberLevelIcons, WorldSelection world) {
        //Reusable variables
        final AudioController audio = AudioController.getInstance();
        BitmapFont buttonFont = buttonNumberFont;
        WidgetGroup buttonGroup;
        ImageButton levelSelectIcon;
        Label.LabelStyle style = new Label.LabelStyle(buttonFont, Color.BLACK);
        ImageButton.ImageButtonStyle buttonStyle = createButtonStyle(world);
        Label buttonLabelNumber;

        if (world == WorldSelection.CAVE) {
            caveLevelButtons = new ImageButton[numberLevelIcons];
            caveLevelButtonLabels = new Label[numberLevelIcons];
        } else if (world == WorldSelection.SHIP) {
            shipLevelButtons = new ImageButton[numberLevelIcons];
            shipLevelButtonLabels = new Label[numberLevelIcons];
        } else if (world == WorldSelection.OCEAN) {
            oceanLevelButtons = new ImageButton[numberLevelIcons];
            oceanLevelButtonLabels = new Label[numberLevelIcons];
        }
        ImageButton[] levelButtons = world == WorldSelection.CAVE ? caveLevelButtons :
                (world == WorldSelection.SHIP ? shipLevelButtons : oceanLevelButtons);
        Label[] levelButtonLabels = world == WorldSelection.CAVE ? caveLevelButtonLabels :
                (world == WorldSelection.SHIP ? shipLevelButtonLabels : oceanLevelButtonLabels);
        final int baseExitCode = world == WorldSelection.CAVE ? CAVE_EXIT_CODE :
                (world == WorldSelection.SHIP ? SHIP_EXIT_CODE : OCEAN_EXIT_CODE);
        boolean twoRows = numberLevelIcons >= 11;

        //Buttons To be added
        Deque<WidgetGroup> buttonQueue = new LinkedList<>();

        //Create level icon table
        Table levelIconTable = new Table();

        //Compute Button offsets for text
        int offsetX = (int) ((LEVEL_ICON_LEFT_PADDING - LEVEL_ICON_RIGHT_PADDING) /INTENDED_WIDTH * canvas.getWidth());
        int offsetY = (int) ((LEVEL_ICON_BOTTOM_PADDING - LEVEL_ICON_TOP_PADDING) /INTENDED_HEIGHT * canvas.getHeight());
        float width = buttonStyle.imageUp.getMinWidth();
        float height = buttonStyle.imageUp.getMinHeight();
        //Test width
        float maxIconsPerRow = twoRows ? (float) Math.ceil(numberLevelIcons/2f) : numberLevelIcons;
        float expectedWidth = width * maxIconsPerRow;
        if (expectedWidth > INTENDED_MIDDLE_BOX_WIDTH/INTENDED_WIDTH * canvas.getWidth()) {
            //Recompute width to fit inside the boxes
            float difference = expectedWidth - (INTENDED_MIDDLE_BOX_WIDTH/INTENDED_WIDTH * canvas.getWidth());
            float diffPerButton = difference / maxIconsPerRow;
            float scaleFactor = (width - diffPerButton) / width;
            width = width * scaleFactor;
            height = height * scaleFactor;

            //Resize text font to fit
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(ROBOTO_FONT_FILE));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = (int) Math.ceil(INTENDED_LEVEL_ICON_FONT_SIZE * (canvas.getWidth() / INTENDED_WIDTH) * scaleFactor);
            buttonFont = generator.generateFont(parameter);
            generator.dispose();
            style = new Label.LabelStyle(buttonFont, Color.BLACK);

            //Recompute offsets
            offsetX *= scaleFactor;
            offsetY *= scaleFactor;
        }

        //Create Buttons
        for (int i = 1; i <= numberLevelIcons; i++) {
            //Create components of a level button
            buttonGroup = new WidgetGroup();
            style = new Label.LabelStyle(style);
            buttonStyle = createButtonStyle(width, height, world);
            buttonLabelNumber = new Label("" + i, style);
            levelSelectIcon = new ImageButton(buttonStyle);

            //Add smaller components to button group
            buttonGroup.addActor(levelSelectIcon);
            buttonGroup.addActor(buttonLabelNumber);

            //Set spacing for button label number
            buttonLabelNumber.setAlignment(Align.center);
            buttonLabelNumber.setWidth(width);
            buttonLabelNumber.setHeight(height);

            //Place objects within buttonGroup
            levelSelectIcon.setPosition(0, 0, Align.bottomLeft);
            buttonLabelNumber.setPosition(buttonStyle.imageUp.getMinWidth()/2f + offsetX,
                    buttonStyle.imageUp.getMinHeight()/2f + offsetY, Align.center);

            //Create Click Listener for level Select Icon
            final int finalI = i;
            final ImageButton finalLevelSelectIcon = levelSelectIcon;
            levelSelectIcon.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!finalLevelSelectIcon.isDisabled()) {
                        if (selectedLevel != baseExitCode + finalI) {
                            audio.playSound("levelSelectClickIcon", LEVEL_SELECT_ICON_CLICK, false);
                        }
                        selectedLevel = baseExitCode + finalI;
                        updateFlavorText();
                    }
                }
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (!finalLevelSelectIcon.isChecked() && !finalLevelSelectIcon.isDisabled() && pointer == -1) {
                        audio.playSound("levelSelectOverIcon" + finalI, BUTTON_MOUSEOVER, false);
                        mouseOverSelected = baseExitCode + finalI;
                    }
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (!finalLevelSelectIcon.isChecked() && !finalLevelSelectIcon.isDisabled() && pointer == -1) {
                        mouseOverSelected = -1;
                    }
                }
            });
            buttonLabelNumber.setTouchable(Touchable.disabled);

            //Add newly created button to list of buttons to be added
            levelButtons[i - 1] = levelSelectIcon;
            levelButtonLabels[i - 1] = buttonLabelNumber;
            buttonQueue.add(buttonGroup);
            levelButtonGroup.add(levelSelectIcon);
        }

        //Add Level Icons to table
        int i = 0;
        for (WidgetGroup button : buttonQueue) {
            if (!twoRows && i == 0 && numberLevelIcons > 1)
                levelIconTable.add(button).size(width, height).left().expandX();
            else if (!twoRows && i == numberLevelIcons - 1 && numberLevelIcons > 1)
                levelIconTable.add(button).size(width, height).right().expandX();
            else
                levelIconTable.add(button).size(width,height).center().expandX();

            if (twoRows && i == (int) Math.ceil(numberLevelIcons / 2f) - 1)
                levelIconTable.row();

            i++;
        }
        levelIconTable.setHeight(INTENDED_LEVEL_ICON_HEIGHT/INTENDED_HEIGHT * canvas.getHeight());
        levelIconTable.setWidth(INTENDED_MIDDLE_BOX_WIDTH/INTENDED_WIDTH * canvas.getWidth());

        return levelIconTable;
    }

    /**
     * Creates the middle text table, containing the name, flavor text, and play button
     * related to each level.
     *
     * @return A reference to the completed middle text table.
     */
    private Table createMiddleTextTable() {
        //Reusable Variables
        final AudioController audio = AudioController.getInstance();
        final LevelSelectMode levelSelect = this;
        Label.LabelStyle style;
        ImageButton.ImageButtonStyle buttonStyle;

        //Create table
        Table middleTextTable = new Table();

        //Level flavor text
        style = new Label.LabelStyle(boldTextDisplayFont, Color.BLACK);
        firstTitle = new Label(PLACEHOLDER_TITLE_1, style);
        style = new Label.LabelStyle(normalTextDiplayFont, Color.BLACK);
        firstInfo = new Label(PLACEHOLDER_TEXT_1, style);
        style = new Label.LabelStyle(boldTextDisplayFont, Color.BLACK);
        secondTitle = new Label(PLACEHOLDER_TITLE_2, style);
        style = new Label.LabelStyle(normalTextDiplayFont, Color.BLACK);
        secondInfo = new Label(PLACEHOLDER_TEXT_2, style);

        //Play Buttons
        Stack playButtonStack = new Stack();
        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(PLAY_BUTTON_CAVE, Texture.class));
        buttonStyle.imageOver = new TextureRegionDrawable(manager.get(PLAY_BUTTON_CAVE, Texture.class)).tint(Color.LIGHT_GRAY);
        buttonStyle.imageDisabled = new TextureRegionDrawable(manager.get(PLAY_BUTTON_CAVE, Texture.class)).tint(Color.DARK_GRAY);

        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageUp.getMinHeight());
        buttonStyle.imageDisabled.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageDisabled.setMinHeight(buttonStyle.imageUp.getMinHeight());
        playButtonCave = new ImageButton(buttonStyle);

        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(PLAY_BUTTON_SHIP, Texture.class));
        buttonStyle.imageOver = new TextureRegionDrawable(manager.get(PLAY_BUTTON_SHIP, Texture.class)).tint(Color.LIGHT_GRAY);
        buttonStyle.imageDisabled = new TextureRegionDrawable(manager.get(PLAY_BUTTON_SHIP, Texture.class)).tint(Color.DARK_GRAY);

        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageUp.getMinHeight());
        buttonStyle.imageDisabled.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageDisabled.setMinHeight(buttonStyle.imageUp.getMinHeight());
        playButtonShip = new ImageButton(buttonStyle);

        buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(PLAY_BUTTON_CAVE, Texture.class));
        buttonStyle.imageOver = new TextureRegionDrawable(manager.get(PLAY_BUTTON_CAVE, Texture.class)).tint(Color.LIGHT_GRAY);
        buttonStyle.imageDisabled = new TextureRegionDrawable(manager.get(PLAY_BUTTON_CAVE, Texture.class)).tint(Color.DARK_GRAY);

        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageUp.getMinHeight());
        buttonStyle.imageDisabled.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageDisabled.setMinHeight(buttonStyle.imageUp.getMinHeight());
        playButtonOcean = new ImageButton(buttonStyle);

        playButtonCave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!playButtonCave.isDisabled()) {
                    listener.exitScreen(levelSelect, selectedLevel);
                    audio.playSound("levelSelectPlayButton", MENU_CLICK_FORWARD, false);
                }
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!playButtonCave.isChecked() && !playButtonCave.isDisabled() && pointer == -1) {
                    audio.playSound("levelSelectOverPlay", BUTTON_MOUSEOVER, false);
                }
            }
        });
        playButtonShip.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!playButtonShip.isDisabled()) {
                    listener.exitScreen(levelSelect, selectedLevel);
                    audio.playSound("levelSelectPlayButton", MENU_CLICK_FORWARD, false);
                }
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!playButtonShip.isChecked() && !playButtonShip.isDisabled() && pointer == -1) {
                    audio.playSound("levelSelectOverPlay", BUTTON_MOUSEOVER, false);
                }
            }
        });
        playButtonOcean.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!playButtonOcean.isDisabled()) {
                    listener.exitScreen(levelSelect, selectedLevel);
                    audio.playSound("levelSelectPlayButton", MENU_CLICK_FORWARD, false);
                }
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!playButtonOcean.isChecked() && !playButtonOcean.isDisabled() && pointer == -1) {
                    audio.playSound("levelSelectOverPlay", BUTTON_MOUSEOVER, false);
                }
            }
        });
        playButtonShip.setVisible(currentWorldSelection == WorldSelection.CAVE);
        playButtonShip.setVisible(currentWorldSelection == WorldSelection.SHIP);
//        playButtonOcean.setVisible(currentWorldSelection == WorldSelection.OCEAN);
        playButtonOcean.setVisible(false);

        playButtonCave.setDisabled(selectedLevel == -1);
        playButtonShip.setDisabled(selectedLevel == -1);
        playButtonOcean.setDisabled(selectedLevel == -1);
        playButtonStack.add(playButtonCave);
        playButtonStack.add(playButtonShip);
        playButtonStack.add(playButtonOcean);

        //Adding texts to nested middle table
        Table firstText = new Table();
        firstText.add(firstTitle).left().top().expandX();
        firstText.row();
        firstText.add(firstInfo).left().expandX();
        firstText.pad(10f,10f,0f,10f);

        Table secondText = new Table();
        secondText.add(secondTitle).left().top().expandX();
        secondText.row();
        secondText.add(secondInfo).left().expandX();
        secondText.pad(0f,10f,10f,10f);

        //Background for the text area
        TextureRegionDrawable background = new TextureRegionDrawable(manager.get(TEXT_BOX_BG, Texture.class));
        background.setMinWidth(background.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        background.setMinHeight(background.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        middleTextTable.setBackground(background);

        //Add text sub tables to the middle text table
        middleTextTable.add(firstText).grow().left().colspan(2)
                .padLeft(Value.percentWidth(INTENDED_TEXT_LEFT_PADDING/INTENDED_WIDTH, masterTable));
        middleTextTable.row();
        middleTextTable.add(secondText).grow().left()
                .padLeft(Value.percentWidth(INTENDED_TEXT_LEFT_PADDING/INTENDED_WIDTH, masterTable));
        middleTextTable.add(playButtonStack).bottom().right()
                .padBottom(Value.percentHeight(INTENDED_PLAY_BOTTOM_PADDING/INTENDED_HEIGHT, masterTable))
                .padRight(Value.percentWidth(INTENDED_PLAY_RIGHT_PADDING/INTENDED_WIDTH, masterTable));

        return middleTextTable;
    }

    /**
     * Creates an Image Button style with the appropriate values for imageUp, imageOver, imageChecked, and
     * imageDisabled. Resizes button texture to keep ratio between screen size and button size.
     *
     * @return The created image button style.
     */
    private ImageButton.ImageButtonStyle createButtonStyle(WorldSelection world) {
        String imagePath = world == WorldSelection.CAVE ? LEVEL_ICON_CAVE :
                world == WorldSelection.SHIP ? LEVEL_ICON_SHIP : LEVEL_ICON_CAVE; //TODO: Fix
        ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(imagePath, Texture.class));
        buttonStyle.imageOver = new TextureRegionDrawable(manager.get(imagePath, Texture.class)).tint(Color.LIGHT_GRAY);
        buttonStyle.imageChecked = new TextureRegionDrawable(manager.get(imagePath, Texture.class)).tint(Color.GRAY);
        buttonStyle.imageDisabled = new TextureRegionDrawable(manager.get(LOCKED_LEVEL, Texture.class));

        buttonStyle.imageUp.setMinWidth(buttonStyle.imageUp.getMinWidth()/INTENDED_WIDTH * canvas.getWidth());
        buttonStyle.imageUp.setMinHeight(buttonStyle.imageUp.getMinHeight()/INTENDED_HEIGHT * canvas.getHeight());
        buttonStyle.imageOver.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageOver.setMinHeight(buttonStyle.imageUp.getMinHeight());
        buttonStyle.imageChecked.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageChecked.setMinHeight(buttonStyle.imageUp.getMinHeight());
        buttonStyle.imageDisabled.setMinWidth(buttonStyle.imageUp.getMinWidth());
        buttonStyle.imageDisabled.setMinHeight(buttonStyle.imageUp.getMinHeight());

        return buttonStyle;
    }

    /**
     * Creates an Image Button style with the appropriate values for imageUp, imageOver, imageChecked, and
     * imageDisabled. Sets button to size given in parameters.
     *
     * @param width The width of the button in pixels.
     * @param height The height of the button in pixels.
     * @return The created image button style.
     */
    private ImageButton.ImageButtonStyle createButtonStyle(float width, float height, WorldSelection world) {
        String imagePath = world == WorldSelection.CAVE ? LEVEL_ICON_CAVE :
                world == WorldSelection.SHIP ? LEVEL_ICON_SHIP : LEVEL_ICON_CAVE; //TODO: Fix level Icon for ocean
        ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();
        buttonStyle.imageUp = new TextureRegionDrawable(manager.get(imagePath, Texture.class));
        buttonStyle.imageOver = new TextureRegionDrawable(manager.get(imagePath, Texture.class)).tint(Color.LIGHT_GRAY);
        buttonStyle.imageChecked = new TextureRegionDrawable(manager.get(imagePath, Texture.class)).tint(Color.GRAY);
        buttonStyle.imageDisabled = new TextureRegionDrawable(manager.get(LOCKED_LEVEL, Texture.class));

        buttonStyle.imageUp.setMinWidth(width);
        buttonStyle.imageUp.setMinHeight(height);
        buttonStyle.imageOver.setMinWidth(width);
        buttonStyle.imageOver.setMinHeight(height);
        buttonStyle.imageChecked.setMinWidth(width);
        buttonStyle.imageChecked.setMinHeight(height);
        buttonStyle.imageDisabled.setMinWidth(width);
        buttonStyle.imageDisabled.setMinHeight(height);

        return buttonStyle;
    }
    //#endregion
        //======================================================

    /**
     * Unloads the assets for this game.
     *
     * This method erases the static variables.  It also deletes the associated textures
     * from the asset manager. If no assets are loaded, this method does nothing.
     *
     * @param manager Reference to global asset manager
     */
    public void unloadContent(AssetManager manager) {
        for(String s : assets) {
            if (manager.isLoaded(s)) {
                manager.unload(s);
            }
        }
        masterTable = null;
    }
    //#endregion
    //=================================

    //================================================================================
    //#region Menu Loop
    /**
     *  @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        InputController input = InputController.getInstance();
        input.readInput(null, null);
        if (input.didPressLeft()) {
            listener.setCursor(ScreenListener.CursorState.CLICKED);
        } else if (input.didReleaseLeft()) {
            listener.setCursor(ScreenListener.CursorState.DEFAULT);
        }

        //Update Flavor Text
        updateFlavorText();

        //Update world selection arrows
//        forwardButton.setVisible(currentWorldSelection != WorldSelection.OCEAN);
        forwardButton.setVisible(currentWorldSelection != WorldSelection.SHIP);
        forwardButton.setDisabled(currentWorldSelection == WorldSelection.CAVE && saveGame.currentLevel < 200 ||
                currentWorldSelection == WorldSelection.SHIP && saveGame.currentLevel < 300);
        backwardButton.setVisible(currentWorldSelection != WorldSelection.CAVE);

        //Update world selection dots
        caveDot.setChecked(currentWorldSelection == WorldSelection.CAVE);
        shipDot.setChecked(currentWorldSelection == WorldSelection.SHIP);
        shipDot.setDisabled(saveGame.currentLevel < 200);
//        oceanDot.setChecked(currentWorldSelection == WorldSelection.OCEAN);
//        oceanDot.setDisabled(saveGame.currentLevel < 300);

        //Update play buttons
        playButtonShip.setVisible(currentWorldSelection == WorldSelection.CAVE);
        playButtonShip.setVisible(currentWorldSelection == WorldSelection.SHIP);
        playButtonOcean.setVisible(currentWorldSelection == WorldSelection.OCEAN);

        playButtonCave.setDisabled(selectedLevel == -1);
        playButtonShip.setDisabled(selectedLevel == -1);
        playButtonOcean.setDisabled(selectedLevel == -1);

        //Update level select button tables
        caveLevelIcons.setVisible(currentWorldSelection == WorldSelection.CAVE);
        shipLevelIcons.setVisible(currentWorldSelection == WorldSelection.SHIP);
//        oceanLevelIcons.setVisible(currentWorldSelection == WorldSelection.OCEAN);

        //Update audio
        AudioController.getInstance().update();
    }

    /**
     * Draw the background and stage to canvas.
     */
    private void draw(float delta) {
        canvas.begin();
        Texture background = currentWorldSelection == WorldSelection.CAVE ? caveBackground :
                currentWorldSelection == WorldSelection.SHIP ? shipBackground : oceanBackground;
        canvas.draw(background, Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.end();
        canvas.drawStage(delta);
    }

    /**
     * Updates the level flavor text in the text box based on the currently selected level.
     */
    private void updateFlavorText() {
        //Initial set text
        if ((selectedLevel > 0 || mouseOverSelected > 0) && levelButtonGroup.getChecked() != null && !levelButtonGroup.getChecked().isDisabled()) {
            int level = (mouseOverSelected == -1) ? selectedLevel : mouseOverSelected;
            String[][] flavorTextArray = currentWorldSelection == WorldSelection.CAVE ? levelList.caveLevelsInfo :
                    (currentWorldSelection == WorldSelection.SHIP ? levelList.shipLevelsInfo : levelList.oceanLevelsInfo);
            if (flavorTextArray.length >= level % 100) {
                String[] levelInfo = flavorTextArray[(level - 1) % 100];
                firstTitle.setText(levelInfo[0]);
                firstInfo.setText(levelInfo[1]);
                secondTitle.setText("Types of Octopi:");
                secondInfo.setText(levelInfo[2]);
            } else {
                firstTitle.setText(PLACEHOLDER_TITLE_1 + level % 100);
                firstInfo.setText(PLACEHOLDER_TEXT_1);
                secondTitle.setText(PLACEHOLDER_TITLE_2);
                secondInfo.setText(PLACEHOLDER_TEXT_2);
            }
        } else {
            if (!firstTitle.getText().toString().equals(POPULATE_FAILED_TITLE) &&
                    !firstTitle.getText().toString().equals(LOAD_FAILED_TITLE)) {
                firstTitle.setText("No Level Selected");
                firstInfo.setText("");
                secondTitle.setText("");
                secondInfo.setText("");
            }
        }
    }

    /**
     * Unchecks all created level select buttons.
     */
    private void unCheckAll() {
        levelButtonGroup.uncheckAll();
        selectedLevel = -1;
    }

    @Override
    public void transitionDraw(float delta) {
        draw(delta);
    }

    @Override
    public void showTransition() {
        canvas.clearStage();
        canvas.addTable(masterTable);
        //Update Flavor Text
        updateFlavorText();
        int oldSelected = selectedLevel;
        unCheckAll();

        //Update world selection arrows
//        forwardButton.setVisible(currentWorldSelection != WorldSelection.OCEAN);
        forwardButton.setVisible(currentWorldSelection != WorldSelection.SHIP);
        forwardButton.setDisabled(currentWorldSelection == WorldSelection.CAVE && saveGame.currentLevel < 200 ||
                currentWorldSelection == WorldSelection.SHIP && saveGame.currentLevel < 300);
        backwardButton.setVisible(currentWorldSelection != WorldSelection.CAVE);

        //Update world selection dots
        caveDot.setChecked(currentWorldSelection == WorldSelection.CAVE);
        shipDot.setChecked(currentWorldSelection == WorldSelection.SHIP);
        shipDot.setDisabled(saveGame.currentLevel < 200);
        oceanDot.setChecked(currentWorldSelection == WorldSelection.OCEAN);
        oceanDot.setDisabled(saveGame.currentLevel < 300);

        //Update play buttons
        playButtonShip.setVisible(currentWorldSelection == WorldSelection.CAVE);
        playButtonShip.setVisible(currentWorldSelection == WorldSelection.SHIP);
        playButtonOcean.setVisible(currentWorldSelection == WorldSelection.OCEAN);

        playButtonCave.setDisabled(selectedLevel == -1);
        playButtonShip.setDisabled(selectedLevel == -1);
        playButtonOcean.setDisabled(selectedLevel == -1);

        //Update level select button tables
        caveLevelIcons.setVisible(currentWorldSelection == WorldSelection.CAVE);
        shipLevelIcons.setVisible(currentWorldSelection == WorldSelection.SHIP);
        oceanLevelIcons.setVisible(currentWorldSelection == WorldSelection.OCEAN);
        selectedLevel = oldSelected;
    }

    /**
     *
     */
    public void updateDisabledLevels() {
        for (int i = 0; i < caveLevelButtons.length; i++) {
            //The levels should be enabled if the save file is on a later world
            // or on the same world but a later level
            boolean enableButton = saveGame.currentLevel / 100 > 1
                    || (saveGame.currentLevel / 100 == 1 && i < saveGame.currentLevel % 100);
            caveLevelButtons[i].setDisabled(!enableButton);
            caveLevelButtonLabels[i].setVisible(enableButton);
        }
        for (int i = 0; i < shipLevelButtons.length; i++) {
            boolean enableButton = saveGame.currentLevel / 100 > 2
                    || (saveGame.currentLevel / 100 == 2 && i < saveGame.currentLevel % 100);
            shipLevelButtons[i].setDisabled(!enableButton);
            shipLevelButtonLabels[i].setVisible(enableButton);
        }
        for (int i = 0; i < oceanLevelButtons.length; i++) {
            boolean enableButton = saveGame.currentLevel / 100 > 3
                    || (saveGame.currentLevel / 100 == 3 && i < saveGame.currentLevel % 100);
            oceanLevelButtons[i].setDisabled(!enableButton);
            oceanLevelButtonLabels[i].setVisible(enableButton);
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
            canvas.addTable(masterTable);

            //TODO: Change with save game
            updateDisabledLevels();
            if (currentWorldSelection == WorldSelection.OCEAN && oceanLevelButtons.length > 0) {
                oceanLevelButtons[0].setChecked(true);
                selectedLevel = 301;
            } else if (currentWorldSelection == WorldSelection.SHIP && shipLevelButtons.length > 0) {
                shipLevelButtons[0].setChecked(true);
                selectedLevel = 201;
            } else if (currentWorldSelection == WorldSelection.CAVE) {
                caveLevelButtons[0].setChecked(true);
                selectedLevel = 101;
            } else {
                unCheckAll();
            }

            updateFlavorText();

            AudioController audio = AudioController.getInstance();
            String musicFile = audio.getCurrentlyPlayingTrack();
            if (musicFile == null || !musicFile.equals(MENU_MUSIC)) {
                AudioController.getInstance().crossfade(MENU_MUSIC);
            }
            if (audio.isFoleyPlaying())
                audio.fadeOutFoley();

            //Update Flavor Text
            updateFlavorText();

            //Update world selection arrows
            forwardButton.setVisible(currentWorldSelection != WorldSelection.SHIP);
            forwardButton.setDisabled(currentWorldSelection == WorldSelection.CAVE && saveGame.currentLevel < 200 ||
                    currentWorldSelection == WorldSelection.SHIP && saveGame.currentLevel < 300);
            backwardButton.setVisible(currentWorldSelection != WorldSelection.CAVE);

            //Update world selection dots
            caveDot.setChecked(currentWorldSelection == WorldSelection.CAVE);
            shipDot.setChecked(currentWorldSelection == WorldSelection.SHIP);
            shipDot.setDisabled(saveGame.currentLevel < 200);
//            oceanDot.setChecked(currentWorldSelection == WorldSelection.OCEAN);
//            oceanDot.setDisabled(saveGame.currentLevel < 300);

            //Update play buttons
            playButtonShip.setVisible(currentWorldSelection == WorldSelection.CAVE);
            playButtonShip.setVisible(currentWorldSelection == WorldSelection.SHIP);
            playButtonOcean.setVisible(currentWorldSelection == WorldSelection.OCEAN);

            playButtonCave.setDisabled(selectedLevel == -1);
            playButtonShip.setDisabled(selectedLevel == -1);
            playButtonOcean.setDisabled(selectedLevel == -1);

            //Update level select button tables
            caveLevelIcons.setVisible(currentWorldSelection == WorldSelection.CAVE);
            shipLevelIcons.setVisible(currentWorldSelection == WorldSelection.SHIP);
            oceanLevelIcons.setVisible(currentWorldSelection == WorldSelection.OCEAN);
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
        createMasterTable();
        canvas.clearStage();
        canvas.addTable(masterTable);
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    @Override
    public void pause() {

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    @Override
    public void resume() {

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
        if (caveBackground != null) {
            caveBackground.dispose();
            caveBackground = null;
        }
        boldTextDisplayFont = null;
        normalTextDiplayFont = null;
        buttonNumberFont = null;
        masterTable = null;
        bounds = null;
        scale  = null;
        canvas = null;
    }
    //#endregion
    //=================================
}
