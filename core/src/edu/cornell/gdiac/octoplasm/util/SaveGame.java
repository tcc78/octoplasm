package edu.cornell.gdiac.octoplasm.util;

/**
 * The class representation of a save game file. Also stores user settings for display and volume.
 *
 * @author Stephen Chin
 */
public class SaveGame {
    //=========================================================================
    //#region Fields
    /** The default volume of a new save game. */
    private static final float DEFAULT_VOLUME = 1.0f;
    /** The default fullscreen setting. */
    private static final boolean DEFAULT_FULLSCREEN = false;
    /** The default sound on/off setting. */
    private static final boolean DEFAULT_SOUND_ACTIVE = true;
    /** The default starting level. */
//    private static final int STARTING_LEVEL = 101;
    private static final int STARTING_LEVEL = 999; //Disabled for the first Build

    /** The master volume of all sounds in the game. */
    public float masterVolume;
    /** The volume of the in-game music. */
    public float musicVolume;
    /** The volume of the in-game sfx. */
    public float sfxVolume;
    /** Whether or not the game is fullscreen. */
    public boolean isFullscreen;
    /** Whether or not the game sound is active. */
    public boolean soundActive;
    /** The current level of this save file. All other levels beyond this should be locked. */
    public int currentLevel;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructor
    /**
     * Creates a new save file. All settings are set to default. The current level is set to the first level.
     */
    public SaveGame() {
        this(DEFAULT_VOLUME, DEFAULT_VOLUME, DEFAULT_VOLUME,
                DEFAULT_FULLSCREEN, DEFAULT_SOUND_ACTIVE, STARTING_LEVEL);
    }

    /**
     * Creates a save file with the specified fields.
     *
     * @param masterVolume The master volume of the game.
     * @param musicVolume The volume of the in-game music.
     * @param sfxVolume The volume of the in-game sfx.
     * @param isFullscreen Whether or not the game is fullscreen.
     * @param currentLevel The current level of this save file. All other levels beyond this should be locked.
     */
    public SaveGame(float masterVolume, float musicVolume, float sfxVolume,
                    boolean isFullscreen, boolean soundActive, int currentLevel) {
        this.masterVolume = masterVolume;
        this.musicVolume = musicVolume;
        this.sfxVolume = sfxVolume;
        this.isFullscreen = isFullscreen;
        this.soundActive = soundActive;
        this.currentLevel = currentLevel;
    }
    //#endregion
    //=================================
}
