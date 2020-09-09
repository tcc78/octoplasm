/*
 * AudioController.java
 *
 * Sound management in LibGDX is horrible.  It is the absolute worse thing
 * about the engine.  Furthermore, because of OpenAL bugs in OS X, it is
 * even worse than that.  There is a lot of magic vodoo that you have to
 * do to get everything working properly.  This class hides all of that
 * for you and makes it easy to play sound effects.
 *
 * Note that this class is an instance of a Singleton.  There is only one
 * SoundController at a time.  The constructor is hidden and you cannot
 * make your own sound controller.  Instead, you use the method getInstance()
 * to get the current sound controller.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.octoplasm.util;

import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.Gdx;
import edu.cornell.gdiac.audio.AudioEngine;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.audio.MusicBuffer;
import edu.cornell.gdiac.audio.SoundBuffer;

/* From Walker White:
 * A singleton class for controlling sound effects in LibGDX
 *
 * Sound sucks in LibGDX for three reasons.  (1) You have to keep track of
 * a mysterious number every time you play a sound.  (2) You have no idea
 * when a sound has finished playing.  (3) OpenAL bugs on OS X cause popping
 * and distortions if you have no idea what you are doing.  This class 
 * provides a (not so great) solution to all of these.
 *
 * To get around (1), this sound engine uses a key management system.  
 * Instead of waiting for a number after playing the sound, you give it a
 * key ahead of time.  The key allows you to identify different instances
 * of the same sound.  See our example for collision sounds in the Rocket
 * demo for more.
 *
 * To get around (2), we have an update() method.  By calling this method
 * you let the AudioController know that time has progressed by one animation
 * frame.  The cooldown prevents you from playing the same instance of a 
 * sound too close together.  In addition, the frame limit prevents you 
 * from playing too many sounds during the same animation frame (which can
 * lead to distortion).  This is not as good as being able to tell when a
 * sound is finished, but it works for most applications.
 *
 * Finally, for (3), we never actually stop a Sound.  Instead we turn its
 * volume to 0 and allow it to be garbage collected when done.  This is why
 * we never allow you to access a sound object directly.
 */

/**
 * A singleton class for controlling sound.
 *
 * All Sound instances loading through this class should be shorter than 10 seconds.
 * Default encoding for loaded music is [.wav].
 *
 * All Music instances loaded through this class must be exported at a sample rate of
 * 44.1kHz and in a mono format. Default encoding for loaded music is [.ogg].
 *
 * @author Stephen Chin
 */
public class AudioController {
    //=========================================================================
    //#region All Fields
    /** The singleton Sound controller instance */
    private static AudioController controller;

    /** The default volume of everything */
    private static final float DEFAULT_VOLUME = 1.0f;

    /** The asset manager, used for loading AudioSources. */
    private AssetManager manager;

    /** The master volume modifier for all sound coming from the game */
    private float masterVolume;

    private boolean soundActive = true;

        //=============================================================
    //#region Foley Variables
    /** The volume modifier for foley effects */
    private static final float FOLEY_VOLUME_MODIFIER = 0.25f;
    /** The amplitude of the foley volume curve */
    private static final float FOLEY_VOL_AMP = 0.1f;

    /** The number of frames it takes for one full period of the foley volume sin curve */
    private static final int FOLEY_FRAME_PERIOD = 10 * 60;
    /** The default number of frames that a foley fade takes */
    private static final int FOLEY_FADE_FRAMES = (int) (2f * 60);

    /** The music buffer for playing background foley */
    private MusicBuffer foleyBuffer;

    /** The center of the foley volume curve */
    private float foleyVolCenter;
    /**  */
    private float foleyFadeStep;

    /** The counter used for fade outs and fade ins for foley */
    private int foleyFadeCounter;
    /** The variable used in the calculation of the foley volume curve */
    private int foleyVariable;

    /** If the foley is being faded out. */
    private boolean foleyFadeOut;
    //#endregion
        //=============================================

        //=============================================================
    //#region Sound Variables
    /**
     * Inner class to track and active sound instance
     */
    private static class ActiveSound {
        /** Reference to the sound resource */
        public SoundBuffer sound;
        /** The id number representing the sound instance */
        public long  id;
        /** Is the sound looping (so no garbage collection) */
        public boolean loop;
        /** How long this sound has been running in gameplay frames */
        public long frameLifespan;

        /**
         * Creates a new active sound with the given values
         *
         * @param s	Reference to the sound resource
         * @param n The id number representing the sound instance
         * @param b Is the sound looping (so no garbage collection)
         */
        public ActiveSound(SoundBuffer s, long n, boolean b) {
            sound = s;
            id = n;
            loop = b;
            frameLifespan = 0;
        }
    }

    /** The default sound cooldown */
    private static final int DEFAULT_COOL = 10;
    /** The default sound length limit */
    private static final int DEFAULT_LIMIT = 120;
    /** The default limit on sounds per frame */
    private static final int DEFAULT_FRAME = 2;

    /** Keeps track of all of the allocated sound resources */
    private IdentityMap<String,SoundBuffer> soundbank;
    /** Keeps track of all of the "active" sounds */
    private IdentityMap<String,ActiveSound> activeSounds;
    /** Support class for sound garbage collection */
    private Array<String> collection;

    /** The number of animation frames before a key can be reused */
    private long cooldown;
    /** The maximum amount of animation frames a sound can run */
    private long timeLimit;

    /** The maximum number of sounds we can play each animation frame */
    private int frameLimit;
    /** The number of sounds we have played this animation frame */
    private int currentSounds;

    /**  */
    private float soundVolume;

    //#endregion
        //=============================================

        //=============================================================
    //#region Music Variables
    /** The default sample rate of the music buffer. */
    private static final int DEFAULT_SAMPLE_RATE = 44100;
    /** The default number of frames that a crossfade takes */
    private static final int CROSSFADE_FRAMES = (int) (1.5f * 60);
    /** The default number of frames that a sidechain takes */
    private static final int SIDECHAIN_FRAMES = 2 * 60;
    /** The number of sections to split the crossfade curve into. */
    private static final int CROSSFADE_SECTIONS = 7;
    /** The number of sections it takes for a music track to  */
    private static final int SECTIONS_TO_FADE = 4;

    /** The volume modifier for a sidechain activation */
    private static final float SIDECHAIN_VOLUME = 0.5f;
    /**  */
    private static final float DUCK_MUSIC_VOLUME = 0.6f;

    /** Keeps track of all loaded audio sources and their relative positions in the buffer. */
    private ObjectIntMap<AudioSource> audioSourcePosTracker;
    /** Keeps track of all audio sources relative to their loaded filepath. */
    private ObjectMap<String, AudioSource> filenameTracker;
    /** Keeps track of all music files currently loading synchronously. */
    private Array<String> loadingFilepaths;

    /** The number of audio sources loaded into the current music buffer. */
    private int audioSourcesLoaded;

    /** The main music buffer for this class. */
    private MusicBuffer musicBuffer;
    /** The music buffer used for crossfading tracks. */
    private MusicBuffer crossfadeBuffer;

    /** The current position in the music buffer. Set to -1 if no music is playing. */
    private int currentBufferPos;
    /** The counter for sidechaining music. Contains the amount of frames left for sidechaining the music. */
    private int sidechainCounter;
    /** The counter for crossfading music. Contains the amount of frames left for crossfading the music. */
    private int crossfadeCounter;

    /** The amount of each sidechain step back to the original volume. */
    private float sidechainStep;
    /** The amount of each crossfade step back to the original volume. */
    private float crossfadeStep;
    /** The music volume  */
    private float musicVolume;

    /** If the music has been sidechained. */
    private boolean sidechained;
    /** If the music is being crossfaded. */
    private boolean crossfaded;
    /**  */
    private boolean duckMusic;
    /** If we are preparing to switch music tracks to simulate horizontal blending. */
    private boolean playLoadedMusicFromSpot;

    //#endregion
        //=============================================
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a new SoundController with the default settings.
     */
    private AudioController() {
        // Reference to the audio engine created by Walker White
        AudioEngine audioEngine = (AudioEngine) Gdx.audio;
        masterVolume = DEFAULT_VOLUME;
        //Sound stuff
        soundbank = new IdentityMap<>();
        activeSounds = new IdentityMap<>();
        collection = new Array<>();
        cooldown = DEFAULT_COOL;
        timeLimit = DEFAULT_LIMIT;
        frameLimit = DEFAULT_FRAME;
        currentSounds = 0;
        soundVolume = DEFAULT_VOLUME;

        //Foley
        foleyVariable = 0;
        foleyBuffer = audioEngine.newMusicBuffer(false, DEFAULT_SAMPLE_RATE);
        foleyVolCenter = soundVolume * FOLEY_VOLUME_MODIFIER * masterVolume;
        foleyFadeOut = false;

        //Music Stuff
        musicBuffer = audioEngine.newMusicBuffer(false, DEFAULT_SAMPLE_RATE);
        crossfadeBuffer = audioEngine.newMusicBuffer(false, DEFAULT_SAMPLE_RATE);
        musicBuffer.setLoopBehavior(true);
        audioSourcePosTracker = new ObjectIntMap<>();
        filenameTracker = new ObjectMap<>();
        audioSourcesLoaded = 0;
        loadingFilepaths = new Array<>();
        currentBufferPos = -1;
        musicVolume = DEFAULT_VOLUME;
    }

    /**
     * Returns the single instance for the SoundController
     *
     * The first time this is called, it will construct the SoundController.
     *
     * @return the single instance for the SoundController
     */
    public static AudioController getInstance() {
        if (controller == null) {
            controller = new AudioController();
        }
        return controller;
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**
     * Returns the number of frames before a key can be reused
     *
     * If a key was used very recently, then an attempt to use the key
     * again means that the sound will be stopped and restarted. This
     * can cause undesirable artifacts.  So we limit how fast a key
     * can be reused.
     *
     * @return the number of frames before a key can be reused
     */
    public long getCoolDown() {
        return cooldown;
    }

    /**
     * Sets the number of frames before a key can be reused
     *
     * If a key was used very recently, then an attempt to use the key
     * again means that the sound will be stopped and restarted. This
     * can cause undesirable artifacts.  So we limit how fast a key
     * can be reused.
     *
     * @param value	the number of frames before a key can be reused
     */
    public void setCoolDown(long value) {
        cooldown = value;
    }

    /**
     * Returns the maximum amount of animation frames a sound can run
     *
     * Eventually we want to garbage collect sound instances.  Since we cannot
     * do this, we set an upper bound on all sound effects (default is 2
     * seconds) and garbage collect when time is up.
     *
     * Sounds on a loop with NEVER be garbage collected.  They must be stopped
     * manually via stop().
     *
     * @return the maximum amount of animation frames a sound can run
     */
    public long getTimeLimit() {
        return timeLimit;
    }

    /**
     * Sets the maximum amount of animation frames a sound can run
     *
     * Eventually we want to garbage collect sound instances.  Since we cannot
     * do this, we set an upper bound on all sound effects (default is 2
     * seconds) and garbage collect when time is up.
     *
     * Sounds on a loop with NEVER be garbage collected.  They must be stopped
     * manually via stop().
     *
     * @param value the maximum amount of animation frames a sound can run
     */
    public void setTimeLimit(long value) {
        timeLimit = value;
    }

    /**
     * Returns the maximum amount of sounds per animation frame
     *
     * Because of Box2d limitations in LibGDX, you might get a lot of simultaneous
     * sounds if you try to play sounds on collision.  This in turn can cause
     * distortions.  We fix that by putting an upper bound on the number of
     * simultaneous sounds per animation frame.  If you exceed the number, then
     * you should wait another frame before playing a sound.
     *
     * @return the maximum amount of sounds per animation frame
     */
    public int getFrameLimit() {
        return frameLimit;
    }

    /**
     * Sets the maximum amount of sounds per animation frame
     *
     * Because of Box2d limitations in LibGDX, you might get a lot of simultaneous
     * sounds if you try to play sounds on collision.  This in turn can cause
     * distortions.  We fix that by putting an upper bound on the number of
     * simultaneous sounds per animation frame.  If you exceed the number, then
     * you should wait another frame before playing a sound.
     *
     * @param value the maximum amount of sounds per animation frame
     */
    public void setFrameLimit(int value) {
        frameLimit = value;
    }

    /**
     * Returns true if the sound instance is currently active
     *
     * @param key The sound instance identifier
     *
     * @return true if the sound instance is currently active
     */
    public boolean isActive(String key) {
        return activeSounds.containsKey(key);
    }

    /**
     * Returns the volume of sound effects.
     * @return the volume of sound effects
     */
    public float getSoundVolume() {
        return soundVolume;
    }

    /**
     * Updates the volume of sound effects.
     * @param soundVolume the new volume for sound effects.
     */
    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
        foleyVolCenter = soundVolume * FOLEY_VOLUME_MODIFIER * masterVolume;
    }

    /**
     * Returns the volume of music.
     * @return the volume of music.
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Sets the volume of the music.
     * @param musicVolume the new volume of the music.
     */
    public void setMusicVolume(float musicVolume) {
        //NOTE: may have strange interactions with sidechain and crossfade
        this.musicVolume = clamp(musicVolume);
        if (musicBuffer.isPlaying())
            musicBuffer.setVolume(clamp(this.musicVolume * masterVolume));
    }

    /**
     * Returns the volume of everything.
     * @return the volume of everything.
     */
    public float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Sets the volume of the master track.
     * @param masterVolume the new volume of the master track
     */
    public void setMasterVolume(float masterVolume) {
        this.masterVolume = masterVolume;
        if (musicBuffer.isPlaying())
            musicBuffer.setVolume(clamp(musicVolume * masterVolume));
        foleyVolCenter = soundVolume * FOLEY_VOLUME_MODIFIER * masterVolume;
    }

    /**
     * Set the asset manager for the project. Important, as the asset manager is used for all of the
     * music methods. If not set, will inevitably lead to a null pointer exception somewhere else in the
     * code.
     *
     * @param manager The asset manager for this project
     */
    public void setManager(AssetManager manager) {
        this.manager = manager;
    }

    /**
     * Returns the path of the currently playing track. This is a new string, not the original string object passed
     * in to start the music.
     *
     * @return The path of the currenly playing music track
     */
    public String getCurrentlyPlayingTrack() {
        if (!musicBuffer.isPlaying() || musicBuffer.getCurrent() == null) {
            return null;
        } else {
            return musicBuffer.getCurrent().getFile().path();
        }
    }

    /**
     * Whether the music Buffer is playing.
     *
     * @return Whether the music Buffer is playing.
     */
    public boolean isMusicPlaying() {
        return musicBuffer.isPlaying();
    }

    /**  */
    public boolean isFoleyPlaying() {
        return foleyBuffer.isPlaying();
    }

    public void setSoundActive(boolean soundActive) {
        this.soundActive = soundActive;
        setMasterVolume(masterVolume);
    }

    public boolean isSoundActive() {
        return soundActive;
    }
    //#endregion
    //=================================

    //================================================================================
    //#region Sound Management
    /**
     * Uses the asset manager to allocate a sound.
     *
     * @param manager A reference to the asset manager loading the sound
     * @param filename The filename for the sound asset
     */
    public void allocateSound(AssetManager manager, String filename) {
        SoundBuffer sound = (SoundBuffer) manager.get(filename,Sound.class);
        soundbank.put(filename,sound);
    }

    /**
     * Plays the an instance of the given sound
     *
     * @param key The identifier for this sound instance
     * @param filename The filename of the sound asset
     * @param loop Whether to loop the sound
     *
     */
    public void playSound(String key, String filename, boolean loop) {
        playSound(key, filename, loop, false, soundVolume);
    }

    /**
     * Plays the an instance of the given sound
     *
     * @param key The identifier for this sound instance
     * @param filename The filename of the sound asset
     * @param loop Whether to loop the sound
     *
     */
    public void playSound(String key, String filename, boolean loop, boolean sidechain) {
        playSound(key, filename, loop, sidechain, soundVolume);
    }

    /**
     * Plays the an instance of the given sound
     *
     *  @param key The identifier for this sound instance
     * @param filename The filename of the sound asset
     * @param loop Whether to loop the sound
     * @param volume The sound volume in the range [0,1]
     *
     */
    public void playSound(String key, String filename, boolean loop, boolean sidechain, float volume) {
        // Get the sound for the file
        if (!soundbank.containsKey(filename) || currentSounds >= frameLimit) {
            return;
        }

        // If there is a sound for this key, stop it
        SoundBuffer sound = soundbank.get(filename);
        stopSound(key);

        // Play the new sound and add it
        long id = sound.play(clamp(volume * masterVolume));
        if (id == -1) {
            return;
        } else if (loop) {
            sound.setLooping(id, true);
        }

        activeSounds.put(key, new ActiveSound(sound, id, loop));
        currentSounds++;

        if (sidechain)
            sidechain();
    }

    /**
     * Stops the sound, allowing its key to be reused.
     *
     * @param key The sound instance to stop.
     */
    public void stopSound(String key) {
        // Get the active sound for the key
        if (!activeSounds.containsKey(key)) {
            return;
        }

        ActiveSound snd = activeSounds.get(key);

        // This is a workaround for the OS X sound bug
        //snd.sound.stop(snd.id);
        snd.sound.setLooping(snd.id,false); // Will eventually garbage collect
        snd.sound.setVolume(snd.id, 0.0f);
        activeSounds.remove(key);
    }
    //#endregion
    //=================================

    //================================================================================
    //#region Music Management
    /**
     * Plays the music file. If a music file is already playing, the previous music file is stopped and
     * the new music file is introduced at the previous volume. If the Music files are still loading,
     * we finish asset loading until it is available.
     *
     * @param filepath Reference to the music file.
     */
    public void playMusic(String filepath) {
        //Load new music file
        if (!manager.isLoaded(filepath)) {
            if (manager.contains(filepath)) {
                manager.finishLoadingAsset(filepath);
            } else {
                manager.load(filepath, AudioSource.class);
                manager.finishLoading();
            }
        }
        playLoadedMusic(filepath);
    }

    /**
     * Plays the music from the spot of the currently playing music.
     * If there is no music currently playing, this starts the music from the beginning.
     *
     * @param filename Reference to the music file.
     */
    public void playMusicFromSpot(String filename) {
        playLoadedMusicFromSpot = true;
        playMusic(filename);
    }

    /**
     * Plays the music file, given that this file has already been loaded in the AssetManager.
     * If playMusicFromCurrentSpot is set, we resolve it here. May have possible issues due to scrumming
     * through the audio buffer.
     *
     * @param filepath Reference to the music file.
     */
    private void playLoadedMusic(String filepath) {
        //TODO: Fix scrumming issue with audio buffer not clearing before setting position
        // reproduce by spamming explosion and reset
        //TODO: Fix Music issue with the Flyer dying
        if (crossfaded) {
            crossfadeBuffer.stop();
            crossfaded = false;
            if (musicBuffer.isPlaying()) {
                musicBuffer.stop();
            }
            clearMusicBuffer();
        }

        if (filenameTracker.containsKey(filepath)) {
            //Get buffer position of next track to play
            int pos = audioSourcePosTracker.get(filenameTracker.get(filepath), -1);
            if (pos == currentBufferPos) { return; }
            //Prepare for playing music from current spot for blending
            float scrumPos = musicBuffer.getPosition();
            if (!musicBuffer.isPlaying()) {
                //Start music buffer if not playing yet
                musicBuffer.play();
                musicBuffer.jumpToSource(pos);
                musicBuffer.setVolume(clamp(musicVolume * masterVolume));
            } else if (playLoadedMusicFromSpot) {
                //If doing seamless playing, fix scrum position
                if (pos < currentBufferPos) {
                    for (int i = pos; i < currentBufferPos; i++) {
                        scrumPos -= musicBuffer.getSource(i).getDuration();
                    }
                } else {
                    for (int i = currentBufferPos; i < pos && i >= 0; i++) {
                        scrumPos += musicBuffer.getSource(i).getDuration();
                    }
                }
                //Set position to proper scrum position
                try {
                    musicBuffer.setPosition(scrumPos);
                } catch (Throwable e) {
                    e.printStackTrace();
                    musicBuffer.stop();
                }
            } else {
                //If not doing seamless playing, start source from beginning
                musicBuffer.jumpToSource(pos);
            }
            //Update our current buffer position
            currentBufferPos = pos;
        } else {
            //Get music to be added to the buffer
            AudioSource loadedMusic = manager.get(filepath, AudioSource.class);
            enqueueMusic(filepath, loadedMusic);
            if (!musicBuffer.isPlaying()) {
                //Start music buffer if not playing yet
                int pos = audioSourcePosTracker.get(filenameTracker.get(filepath), -1);
                musicBuffer.play();
                musicBuffer.jumpToSource(pos);
                musicBuffer.setVolume(clamp(musicVolume * masterVolume));
            } else if (playLoadedMusicFromSpot) {
                //If doing seamless playing, fix scrum position
                float scrumPos = musicBuffer.getPosition();
                scrumPos += musicBuffer.getSource(audioSourcesLoaded - 1).getDuration();
                //Set position to proper scrum position
                musicBuffer.setPosition(scrumPos);
            } else {
                //If not doing seamless playing, start source from beginning
                musicBuffer.advanceSource();
            }
            //Update our current buffer position
            currentBufferPos = audioSourcesLoaded - 1;
        }

        musicBuffer.setLooping(true);
        musicBuffer.setLoopBehavior(true);

        //Unset boolean flags
        playLoadedMusicFromSpot = false;
    }

    /**
     * Stops playing the music buffer.
     */
    public void stopMusic() {
        musicBuffer.stop();
        duckMusic = false;
        crossfaded = false;
        sidechained = false;
    }

    /**
     * Sidechains the current music track by a default amount, compared to it's original volume.
     * Sidechain curve is linear.
     *
     * If the music is already being sidechained, sidechain volume reductions are stacked and resolved within
     * the same amount of time as if the music had only been sidechained once.
     */
    public void sidechain() {
        sidechained = true;
        musicBuffer.setVolume(clamp(musicBuffer.getVolume() * SIDECHAIN_VOLUME));
        sidechainStep = (1 - musicBuffer.getVolume()) / (float) SIDECHAIN_FRAMES;
        sidechainCounter = SIDECHAIN_FRAMES;
    }

    /**
     * Crossfades the current music track to the next music track "seamlessly" over a default amount.
     * If there is no track currently playing, crossfade works the same as {@link #playMusic(String filepath)}.
     * Does not work as intended if buffer is cleared before function call.
     *
     * @param filepath Reference to the music file.
     */
    public void crossfade(String filepath) {
        if (!manager.isLoaded(filepath)) {
            manager.load(filepath, AudioSource.class);
            manager.finishLoading();
        }
        if (!musicBuffer.isPlaying() || musicBuffer.getCurrent() == null || musicBuffer.getNumberOfSources() == 0) {
            playMusic(filepath);
        } else {
            //If already crossfading either:
            if (crossfaded) {
                if (!musicBuffer.isPlaying()) {
                    //Swap out music to be crossfaded to
                    clearMusicBuffer();
                    enqueueMusic(filepath, manager.get(filepath, AudioSource.class));
                    return;
                } else {
                    //Remove previous music, and do a crossfade from the current position
                    if (crossfadeBuffer.isPlaying())
                        crossfadeBuffer.stop();
                    crossfadeBuffer.clearSources();
                }
            }

            //Set crossfade buffer to currently playing track
            float pos = musicBuffer.getPosition() % (musicBuffer.getDuration() / musicBuffer.getNumberOfSources());
            float vol = clamp(musicVolume * masterVolume);
            crossfadeBuffer.addSource(musicBuffer.getCurrent());
            crossfadeBuffer.play();
            crossfadeBuffer.setPosition(pos);

            //Add new track to main music buffer
            clearMusicBuffer();
            enqueueMusic(filepath, manager.get(filepath, AudioSource.class));

            //Set volumes and looping
            crossfadeBuffer.setVolume(vol);
            crossfadeBuffer.setLoopBehavior(true);
            crossfadeBuffer.setLooping(true);
            musicBuffer.setVolume(0f);
            musicBuffer.setLooping(true);
            musicBuffer.setLoopBehavior(true);

            //Set flags for update loop
            crossfaded = true;
            crossfadeStep = ((musicVolume * masterVolume) / CROSSFADE_FRAMES) * (CROSSFADE_SECTIONS/ (float) SECTIONS_TO_FADE);
            crossfadeCounter = CROSSFADE_FRAMES;
            duckMusic = false;
        }
    }

    /**
     * Ducks (lowers the volume of) music by a set percentage from the current music volume
     * @param duckMusic whether or not to duck the music
     */
    public void setDuckMusic(boolean duckMusic) {
        if (!crossfaded) {
            this.duckMusic = duckMusic;
            if (duckMusic) {
                if (sidechained) {
                    sidechainCounter *= DUCK_MUSIC_VOLUME;
                    musicBuffer.setVolume(clamp(musicBuffer.getVolume() * DUCK_MUSIC_VOLUME));
                } else {
                    musicBuffer.setVolume(clamp(musicVolume * DUCK_MUSIC_VOLUME * masterVolume));
                }
            } else {
                musicBuffer.setVolume(clamp(musicVolume * masterVolume));
            }
        }
    }

    /**
     * Loads an audio source behind the scenes, with little impact on running the game.
     * Should only be called if the audio is not needed immediately
     *
     * @param filepath Reference to the music file.
     */
    public void loadSyncAudio(String filepath) {
        manager.load(filepath, AudioSource.class);
        loadingFilepaths.add(filepath);
    }

    /**
     * Loads an audio source, halting progress until the music file is loaded.
     * If the audio file is already loaded, nothing is halted.
     *
     * @param filepath Reference to the music file.
     */
    public void loadAsyncAudio(String filepath) {
        if (!manager.isLoaded(filepath)) {
            if (!manager.contains(filepath))
                manager.load(filepath, AudioSource.class);
            manager.finishLoadingAsset(filepath);
        }
    }

    /**
     * Enqueues an audio source to the music buffer if it has been previously loaded by the AssetManager.
     * If the audio source has not already been added to the manager, this method will do nothing.
     * If the audio source is currently being loaded to the manager, this method will fish loading it before adding.
     *
     * @param filepath Reference to the music file.
     */
    public void enqueueMusicToBuffer(String filepath) {
       if (manager != null && manager.contains(filepath)) {
           if (!manager.isLoaded(filepath)) {
               manager.finishLoadingAsset(filepath);
           }
           AudioSource loadedMusic = manager.get(filepath, AudioSource.class);
           enqueueMusic(filepath, loadedMusic);
       }
    }

    /**
     * Central location of audio enqueuing code. All enqueuing to the music buffer should go through here.
     * If not, there is a chance of not updating the position trackers and the filenames.
     *
     * @param filepath Reference to the music file.
     * @param music The AudioSource to be loaded into the music buffer.
     */
    private void enqueueMusic(String filepath, AudioSource music) {
        musicBuffer.addSource(music);
        audioSourcePosTracker.put(music, audioSourcesLoaded++);
        filenameTracker.put(filepath, music);
    }

    /**
     * Clears the current music buffer of all loaded sources.
     */
    public void clearMusicBuffer() {
        musicBuffer.clearSources();
        audioSourcePosTracker.clear();
        filenameTracker.clear();
        audioSourcesLoaded = 0;
        currentBufferPos = -1;
    }

    //#endregion
    //=================================

    //================================================================================
    //#region Foley
    /**
     * Plays foley audio if it has been previously loaded by the AssetManager.
     * If the audio source has not already been added to the manager, this method will do nothing.
     * If the audio source is currently being loaded to the manager, this method will fish loading it before adding.
     *
     * @param filepath Reference to the music file.
     */
    public void playFoley(String filepath) {
        if (manager != null && manager.contains(filepath)) {
            if (!manager.isLoaded(filepath)) {
                manager.finishLoadingAsset(filepath);
            }
            AudioSource loadedFoley = manager.get(filepath, AudioSource.class);
            if (foleyBuffer.getNumberOfSources() > 0) {
                stopFoley();
            }
            foleyBuffer.addSource(loadedFoley);
            foleyBuffer.setLooping(true);
            foleyBuffer.play();
            foleyBuffer.setVolume(clamp(foleyVolCenter * masterVolume));
            foleyFadeOut = false;
            foleyVariable = 0;
        }
    }

    /**
     *
     */
    public void fadeOutFoley() {
        if (foleyBuffer.isPlaying()) {
            foleyFadeOut = true;
            foleyFadeCounter = FOLEY_FADE_FRAMES;
            foleyFadeStep = foleyBuffer.getVolume() / (float) FOLEY_FADE_FRAMES;
            foleyVariable = 0;
        }
    }

    /**
     * Clears the current foley buffer of the loaded foley effects.
     */
    public void stopFoley() {
        if (foleyBuffer.isPlaying())
            foleyBuffer.stop();
        foleyBuffer.clearSources();
        foleyVariable = 0;
    }
    //#endregion
    //=================================

    //================================================================================
    //#region Just the update code
    /**
     * Updates the current frame of the sound controller.
     *
     * This method serves two purposes for Sounds. First, it allows us to limit the number
     * of sounds per animation frame.  In addition it allows us some primitive
     * garbage collection.
     *
     * For music, this method is primarily used for keeping track of synchronous music loading,
     * sidechaining, and crossfading between tracks.
     */
    public void update() {
        updateSounds();
        updateFoley();
        updateMusic();
    }

    /**
     * This method serves two purposes for Sounds. First, it allows us to limit the number
     * of sounds per animation frame. In addition it allows us some primitive
     * garbage collection.
     */
    private void updateSounds() {
        // Update Sounds
        for(String key : activeSounds.keys()) {
            ActiveSound snd = activeSounds.get(key);
            snd.frameLifespan++;
            if (snd.frameLifespan > timeLimit) {
                collection.add(key);
                snd.sound.setLooping(snd.id,false); // Will eventually garbage collect
                snd.sound.setVolume(snd.id, 0.0f);
            }
        }

        // Remove Sounds that have lived past the frame limit
        for(String key : collection) {
            activeSounds.remove(key);
        }
        collection.clear();
        currentSounds = 0;
    }

    /**
     * For the foley effects, we update where the foley volume is in its curve.
     */
    private void updateFoley() {
        if (foleyBuffer.isPlaying()) {
            if (foleyFadeOut) {
                foleyBuffer.setVolume(clamp(foleyBuffer.getVolume() - foleyFadeStep));
                foleyFadeCounter--;
                if (foleyFadeCounter <= 0) {
                    foleyBuffer.stop();
                    foleyFadeOut = false;
                }
            } else {
                foleyVariable = (foleyVariable + 1) % FOLEY_FRAME_PERIOD;
                float newVolume = foleyVolCenter + (float) (FOLEY_VOL_AMP *
                        Math.sin(foleyVariable * ((2 * Math.PI) / (float) FOLEY_FRAME_PERIOD)));
                newVolume *= masterVolume;
                foleyBuffer.setVolume(clamp(Math.min(newVolume, soundVolume * masterVolume)));
            }
        }
    }

    /**
     * For music, this method is primarily used for keeping track of synchronous music loading,
     * sidechaining, and crossfading between tracks.
     */
    private void updateMusic() {
        //Check on synchronous loading status for each filepath
        for (String loadingFilePath : loadingFilepaths) {
            if (manager.isLoaded(loadingFilePath)) {
                loadingFilepaths.removeValue(loadingFilePath, true);
            }
        }
        //Synchronous loading
        if (!loadingFilepaths.isEmpty()) {
            manager.update(3);
        }

        //Sidechaining and crossfading
        if (sidechained) {
            //Update volume to next volume on sidechain curve
            musicBuffer.setVolume(clamp(Math.min(musicBuffer.getVolume() + sidechainStep, musicVolume * masterVolume)));
            if (sidechainCounter-- <= 0) {
                if (duckMusic)
                    musicBuffer.setVolume(clamp(musicVolume * masterVolume * DUCK_MUSIC_VOLUME));
                else
                    musicBuffer.setVolume(clamp(musicVolume * masterVolume));
                sidechained = false;
            }
        } else if (crossfaded) {
            float crossfadeThreshold = (CROSSFADE_FRAMES / (float) CROSSFADE_SECTIONS) * SECTIONS_TO_FADE;
            if (crossfadeCounter > crossfadeThreshold) {
                //Fade previous out
                crossfadeBuffer.setVolume(clamp(crossfadeBuffer.getVolume() - crossfadeStep));
            }
            if (crossfadeCounter < CROSSFADE_FRAMES - crossfadeThreshold) {
                //Start track once ready
                if (!musicBuffer.isPlaying()) {
                    musicBuffer.play();
                    musicBuffer.setVolume(0f);
                }
                //Fade new Track in
                musicBuffer.setVolume(clamp(Math.min(musicBuffer.getVolume() + crossfadeStep, musicVolume * masterVolume)));
            }

            // Check for crossfade finished
            if (crossfadeCounter-- <= 0) {
                crossfadeBuffer.stop();
                crossfadeBuffer.clearSources();
                musicBuffer.setVolume(clamp(musicVolume * masterVolume));
                crossfaded = false;
            }
        }
    }

    private float clamp(float volume) {
        return soundActive ? Math.max(Math.min(volume, 1), 0) : 0;
    }
    //#endregion
    //=================================
}