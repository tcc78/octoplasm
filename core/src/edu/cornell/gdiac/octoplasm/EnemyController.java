package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.octoplasm.entity.*;
import edu.cornell.gdiac.octoplasm.util.AudioController;
import edu.cornell.gdiac.octoplasm.util.FilmStrip;
//import jdk.internal.util.xml.impl.Pair;

import java.util.*;
/**
 * Controller for enemies. Handles enemy AI with ray-casting as well as enemy effects.
 *
 * @author Tianlin Zhao
 */
public class EnemyController {
    //=========================================================================
    //#region Fields
    /** This class is used for counting the frame of attacks around a certain enemy */
    class animationObject {
        EnemyEntity launcher;
        OctopusEntity target;
        int frame;
        float range;
        animationObject(EnemyEntity launcher, int frame, float range) {
            this.launcher = launcher;
            this.frame = frame;
            this.range = range;
        }
        private Vector2 getPosition() {return launcher.getPosition();}
        private int getFrame() {return frame;}
        private void incFrame() {frame++;}
        private float getRange() {return range;}
        private boolean getAlive() {return target.isAlive();}
        private void setTarget(OctopusEntity octopus) {target = octopus;}
    }

    /** Texture filmstrip for the attack */
    private FilmStrip attack;
    /** Texture for the range circle */
    private Texture rangeIndicator;
    /** Texture for the enemy direction arrow */
    private TextureRegion directionArrow;
    /** Texture for the enemy detection indicator */
    private TextureRegion detectionIndicator;

    /** Reference to the attack texture */
    private static final String ATTACK_TEXTURE = "images/attack.png";
    /** Reference to the counting down texture */
    private static final String COUNTER_TEXTURE = "images/explosion.png";
    /** Reference to the enemy texture 1*/
    private static final String ENEMY_TEXTURE_1 = "static_sprites/enemy/basicGhostEnemy2.png";
    /** Reference to the enemy texture 2*/
    private static final String ENEMY_TEXTURE_2 = "static_sprites/enemy/enemyArmored.png";
    /** Reference to the enemy texture 3*/
    private static final String ENEMY_TEXTURE_3 = "static_sprites/enemy/enemySpiked.png";
    /** Reference to the enemy texture 3*/
    private static final String ENEMY_TEXTURE_4 = "static_sprites/enemy/holeEnemy.png";
    /** Reference to the enemy texture 3*/
    private static final String ENEMY_TEXTURE_5 = "static_sprites/enemy/superEnemy.png";
    /**  */
    private static final String RANGE_CIRCLE = "ui/gameplay/circle.png";
    /**  */
    private static final String DIRECTION_ARROW = "ui/gameplay/enemy/enemy_arrow.png";
    /**  */
    private static final String DETECTION_INDICATOR = "ui/gameplay/enemy/enemy_exclamation.png";
    /**  */
    private static final String ENEMY_DEATH_SOUND = "sounds/gameplay/enemy_death.wav";
    /**  */
    private static final String FLIGHT_DEATH_SOUND = "sounds/gameplay/flight_death.wav";
    /**  */
    private static final String FIGHT_DEATH_SOUND = "sounds/gameplay/boomer-explosion.mp3";
    /**  */
    private static final String FOLD_DEATH_SOUND = "";

    /** The number of frames for the attack */
    public static final int COUNTER_FRAMES = 25;
    /** The number of rays the ray-casting is going to take. */
    public static final int RAY_COUNT = 360;

    /** Color of the detector indicator, for drawing */
    private static final Color DETECTED_INDICATOR_COLOR = new Color(Color.RED);
    /** Color of the attack range indicator, for drawing */
    private static final Color RANGE_INDICATOR_COLOR = new Color(Color.LIGHT_GRAY).sub(0,0,0,0.4f);

    /** The number of frames for the attack */
    public static final int ATTACK_FRAMES = 25;

    /** Cache object for placing explosion */
    private ArrayList<animationObject> attackAnimation = new ArrayList<>();
    /** Cache object for placing explosion */
    private ArrayList<animationObject> countingAnimation = new ArrayList<>();
    /**  */
    private Array<String> assets;

    /** Texture filmstrip for the count down */
    private FilmStrip countdown;

    /** List that maintains the enemies */
    private ArrayList<EnemyEntity> enemyList;

    /** Cache that stores the detected octopus by ray-casting */
    private OctopusEntity rayDetectedEntityCache;
    /** Callback that handles a ray-casting intersection event. */
    private RayCastCallback callback;
    /**  */
    private GameCanvas canvas;
    /**  */
    private Vector2 scale;

    /**  */
    private Vector2 cache1;
    /**  */
    private Vector2 cache2;
    /**  */
    private Color colorCache;

    /**  */
    private float closestFraction;
    private float wall_fraction = 1.0f;
    private float current_fraction = 1.0f;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructor
    /** Creates a new empty set of Enemies
     *
     * @param scale the scale that the enemies will be drawn
     * @param canvas the canvas that these enemies will be drawn on
     * @param assets the art assets that will be passed in for handling textures
     * */
    public EnemyController(Vector2 scale, GameCanvas canvas, Array<String> assets) {
        rayDetectedEntityCache = null;
        closestFraction = 0f;
        this.canvas = canvas;
        this.scale = scale;
        this.assets = assets;
        this.wall_fraction = 1.0f;
        this.current_fraction = 1.0f;

        colorCache = new Color();
        cache1 = new Vector2();
        cache2 = new Vector2();
        enemyList = new ArrayList<>();
        callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if ( fraction < current_fraction ) {
                    current_fraction = fraction;
                    Entity entity = (Entity) fixture.getBody().getUserData();
                    if (!entity.isActive()) return 1;
                    switch (entity.getEntityType()) {
                        case OCTOPUS:
                            rayDetectedEntityCache = (OctopusEntity) entity;
                            return 1;
                        case WALL:
                            wall_fraction = Math.min(fraction,wall_fraction);
                            return 1;
                        case OBSTACLE:
                            if (((ObstacleEntity)entity).getObstacleSubType() == ObstacleEntity.ObstacleSubType.FIGHT_WALL
                            ||  ((ObstacleEntity)entity).getObstacleSubType() == ObstacleEntity.ObstacleSubType.FLIGHT_WALL
                            ||  ((ObstacleEntity)entity).getObstacleSubType() == ObstacleEntity.ObstacleSubType.FOLD_WALL)
                            wall_fraction = Math.min(fraction,wall_fraction);
                            return 1;
                        default:
                            return 1;
                    }
                }
                return 1;
            }
        };
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /** Add an enemy to the controller
     *
     * @param enemy the enemy that is going to be handled by the controller
     * */
    public void addEnemy(EnemyEntity enemy) {
        this.enemyList.add(enemy);
    }

    /**
     * removes enemy from enemylist
     * @param enemy to remove from enemylist
     */
    public void removeEnemy(EnemyEntity enemy){
        this.enemyList.remove(enemy);
    }

    /**
     * Empties the enemy list
     */
    public void emptyEnemyList() {
        enemyList.clear();
    }

    /** Get the list of enemies in the controller
     *
     * @return the list of the enemies
     * */
    public ArrayList<EnemyEntity> getEnemies() {
        return enemyList;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas, Rectangle bounds) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth()/bounds.getWidth();
        this.scale.y = canvas.getHeight()/bounds.getHeight();
//        if(canvas.getWidth()*9 < canvas.getHeight()*16){
//            //height should be kept, width should be bigger than canvas width
//            this.scale.x = this.scale.y;
//        } else if(canvas.getWidth()*9 > canvas.getHeight()*16){
//            this.scale.y = this.scale.x;
//        }
    }
    //#endregion
    //=====================================================================

    //=========================================================================
    //#region Asset Management
    /**
     * Returns a newly loaded filmstrip for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * the number of animation frames) after loading.
     *
     * @param manager 	Reference to global asset manager.
     * @param file		The texture (region) file
     * @param rows 		The number of rows in the filmstrip
     * @param cols 		The number of columns in the filmstrip
     * @param size 		The number of frames in the filmstrip
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
     * Returns a newly loaded texture region for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * whether or not the texture should repeat) after loading.
     *
     * @param manager 	Reference to global asset manager.
     * @param file		The texture (region) file
     * @param repeat	Whether the texture should be repeated
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
        // Explosion textures
        manager.load(ATTACK_TEXTURE, Texture.class);
        assets.add(ATTACK_TEXTURE);
        // Enemy textures
        manager.load(ENEMY_TEXTURE_1, Texture.class);
        assets.add(ENEMY_TEXTURE_1);
        manager.load(ENEMY_TEXTURE_2, Texture.class);
        assets.add(ENEMY_TEXTURE_2);
        manager.load(ENEMY_TEXTURE_3, Texture.class);
        assets.add(ENEMY_TEXTURE_3);
        manager.load(ENEMY_TEXTURE_4, Texture.class);
        assets.add(ENEMY_TEXTURE_4);
        manager.load(ENEMY_TEXTURE_5, Texture.class);
        assets.add(ENEMY_TEXTURE_5);
        // Counter textures
        manager.load(COUNTER_TEXTURE, Texture.class);
        assets.add(COUNTER_TEXTURE);
        // UI Textures
        manager.load(DIRECTION_ARROW, Texture.class);
        assets.add(DIRECTION_ARROW);
        manager.load(DETECTION_INDICATOR, Texture.class);
        assets.add(DETECTION_INDICATOR);
        manager.load(RANGE_CIRCLE, Texture.class);
        assets.add(RANGE_CIRCLE);
        //Sounds
        manager.load(ENEMY_DEATH_SOUND, Sound.class);
        assets.add(ENEMY_DEATH_SOUND);
        manager.load(FLIGHT_DEATH_SOUND, Sound.class);
        assets.add(FLIGHT_DEATH_SOUND);
        manager.load(FIGHT_DEATH_SOUND, Sound.class);
        assets.add(FIGHT_DEATH_SOUND);
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
        attack = createFilmStrip(manager,ATTACK_TEXTURE,8,10, ATTACK_FRAMES);
        countdown = createFilmStrip(manager,COUNTER_TEXTURE,8,10,COUNTER_FRAMES);
        rangeIndicator = manager.get(RANGE_CIRCLE, Texture.class);
        detectionIndicator = createTexture(manager, DETECTION_INDICATOR, false);
        directionArrow = createTexture(manager,DIRECTION_ARROW,false);
        EnemyEntity.normalTexture = createTexture(manager,ENEMY_TEXTURE_1,false);
        EnemyEntity.armoredTexture = createTexture(manager,ENEMY_TEXTURE_2,false);
        EnemyEntity.spikedTexture = createTexture(manager,ENEMY_TEXTURE_3,false);
        //TODO:: add hole texture
        EnemyEntity.holeTexture = createTexture(manager,ENEMY_TEXTURE_4,false);
        EnemyEntity.invincibleTexture = createTexture(manager,ENEMY_TEXTURE_5,false);

        audio.allocateSound(manager, ENEMY_DEATH_SOUND);
        audio.allocateSound(manager, FLIGHT_DEATH_SOUND);
        audio.allocateSound(manager, FIGHT_DEATH_SOUND);
    }

    //#endregion
    //=================================

    //=========================================================================
    //#region Gameplay Loop
    /**
     * resets EnemyController to factory settings
     */
    public void reset(){
        attackAnimation = new ArrayList();
        countingAnimation = new ArrayList<>();
        emptyEnemyList();
        closestFraction = 0f;
        rayDetectedEntityCache = null;
    }

    /**
     * Updates the enemies' states
     *
     * @param dt Number of seconds since last animation frame
     * @param octopusList the list that contains all octopus
     */
    public void update(float dt, ArrayList<OctopusEntity> octopusList, boolean frozen, World world) {
        AudioController audio = AudioController.getInstance();
        if (frozen) return;
        updateEnemies(dt,octopusList, world);
//        getInstructions(1); // 1 is charge in direction, 2 is go in shortest path.
        for (int i = 0; i < enemyList.size(); i++) {
            if (!enemyList.get(i).isGrabbed()) {
                enemyList.get(i).moveToGoal();
            }
            if (enemyList.get(i).getIsDead()) {
                enemyList.get(i).setState(BoxEntity.State.death);
                enemyList.get(i).setAlive(false);
                enemyList.get(i).setActive(false);
                enemyList.get(i).setIsDead(false);
                audio.playSound("enemyDeath", ENEMY_DEATH_SOUND, false); //EnemyDeath
            }
        }
    }

    /**
     * Updates the octopus' target as well as their goal position
     *
     * @param dt Number of seconds since last animation frame
     * @param octopusList the list that contains all octopus
     */
    private void updateEnemies(float dt, ArrayList<OctopusEntity> octopusList, World world) {
        AudioController audio = AudioController.getInstance();
        for (EnemyEntity enemyEntity : enemyList) {
            //Get ready to check on values
            rayDetectedEntityCache = null;
            enemyEntity.stateChanged = false;

            //If this enemy is not alive or active, we don't need to update anything anymore
            if (!enemyEntity.isAlive() || !enemyEntity.isActive()) {
                continue;
            }

            //Check for octopus on enemy collisions
            for (OctopusEntity oct : octopusList) {
                if (!oct.getFlying() && enemyEntity.isColliding(oct)) {
                    //In normal collision, kill octopus
                    oct.setState(BoxEntity.State.death);
                    oct.setAlive(false);
                    if (oct.getOctopusSubType() == OctopusEntity.OctopusSubType.FLYER)
                        audio.playSound("flightDeath", FLIGHT_DEATH_SOUND, false); //Octopus death
                    else if (oct.getOctopusSubType() == OctopusEntity.OctopusSubType.EXPLODER)
                        audio.playSound("fightDeath", FIGHT_DEATH_SOUND, false);
                    //else if (oct.getOctopusSubType() == OctopusEntity.OctopusSubType.TELEPORTER)
                    //audio.playSound("");
                } else if (oct.getFlying() && enemyEntity.isColliding(oct)){
                    if (enemyEntity.getEnemySubType() != EnemyEntity.EnemySubType.HOLE_ENEMY&&
                            enemyEntity.getEnemySubType() != EnemyEntity.EnemySubType.INVINCIBLE_ENEMY) {
                        // if is rammed by a flying one, the enemy is killed
                        enemyEntity.setState(BoxEntity.State.death);
                        enemyEntity.setAlive(false);
                        enemyEntity.setActive(false);
                        audio.playSound("enemyDeath", ENEMY_DEATH_SOUND, false); //EnemyDeath
                    } else {
                        // A hole or invincible enemy will still kill the flying octopus
                        oct.setState(BoxEntity.State.death);
                        oct.setAlive(false);
                        audio.playSound("flightDeath", FLIGHT_DEATH_SOUND, false); //Octopus death
                    }
                }

            }
            if (!enemyEntity.isAlive() || !enemyEntity.isActive()) {
                //If the enemy just died, no need to update anything else
                continue;
            }

            //Detect a new target
            detectTarget(enemyEntity,world);
            OctopusEntity target = enemyEntity.getTarget();
            if (target != null && target.isAlive()) {
                //If enemy has a new target, reset the countdown
                if (enemyEntity.getPreviousTarget() != target)
                    enemyEntity.resetCountDownClock();

                // Dec counters for attacking and detecting
                if (enemyEntity.isChasing()) {
                    enemyEntity.decrementClock();
                } else {
                    enemyEntity.decrementDetectionCounter();
                }


                // Either attack or set the goal to be the current target
                cache1.set(enemyEntity.getPosition());
//                if (enemyEntity.timeRemain() <= 0) {
//                    attackAnimation.add(new animationObject(enemyEntity, 0, enemyEntity.getRange()));
//                    //Kill all octopi within range of enemy
//                    for (OctopusEntity oct : octopusList) {
//                        cache1.set(enemyEntity.getPosition()).sub(oct.getPosition());
//                        if (cache1.len() <= enemyEntity.getRange()) {
//                            oct.setState(BoxEntity.State.death);
//                            oct.setAlive(false);
//                        }
//                    }
//                    enemyEntity.abortTarget();
//                } else
                if (target.isAlive() && cache1.sub(target.getPosition()).len() < enemyEntity.getRange()
                        && !enemyEntity.isColliding(target) && enemyEntity.isChasing()) {
                    // If the current enemy has and it is reachable, chase the target
                    cache1.set(target.getX(), target.getY());
                    enemyEntity.setGoal(cache1);
                    enemyEntity.setState(BoxEntity.State.idle);
                } else if (enemyEntity.isColliding(target)) {
                    // If the enemy kills the target
                    enemyEntity.setState(BoxEntity.State.idle);
                    enemyEntity.setGoal(new Vector2(-1, -1));
                }
            } else {
                // If the enemy no longer has a target
                enemyEntity.setState(BoxEntity.State.idle);
                enemyEntity.setGoal(new Vector2(-1,-1));
                enemyEntity.abortTarget();
            }
        }
    }

    /** Apply ray-casting for a particular enemy and assign its closest while
     * reachable octopus as target.
     *
     * @param enemy the enemy that is going to be handled by the controller
     * @param world the world that the ray-casting will be performed on
     * */
    private void detectTarget(EnemyEntity enemy, World world) {
        //Cache 1 Used to hold enemy position
        cache1.set(enemy.getPosition());
        //Prep values & store previous target
        float dist = enemy.getRange();
        float min_dist = Float.MAX_VALUE;
        enemy.setPreviousTarget(enemy.getTarget());
        enemy.setTarget(null);
        closestFraction = 1f;
        for (int i = 0; i < RAY_COUNT; i++) {
            current_fraction = 1.0f;
            wall_fraction = 1.0f;
            float angle = (float) (2*Math.PI/RAY_COUNT)*i;
            cache2.set(cache1).add((float)(dist*Math.cos(angle)), (float)(dist*Math.sin(angle)));
            world.rayCast(callback,cache1,cache2);

            if (current_fraction < closestFraction && wall_fraction > current_fraction) {
                if (rayDetectedEntityCache != null && rayDetectedEntityCache.isAlive()
                        && cache2.set(cache1).sub(rayDetectedEntityCache.getPosition()).len() < min_dist
                        && cache2.set(cache1).sub(rayDetectedEntityCache.getPosition()).len() < enemy.getRange()) {
                    enemy.setTarget(rayDetectedEntityCache);
                }
            }
            rayDetectedEntityCache = null;
        }
    }

    /**
     * Draw the enemies, attack animations
     *
     * @param delta The drawing context
     * @param frozen Whether the game is frozen
     */
    public void draw(float delta, boolean frozen, boolean complete) {
        // Draw non-active models
        canvas.begin();
        for (EnemyEntity enemy : enemyList) {
            //todo fix this hack, also make flyer death animation
            //if (enemy.isGrabbed()) enemy.drawSilhouette(canvas, scale);
            if (enemy.getTexture() != null && enemy.isAlive()) {
                enemy.draw(canvas);
            }
            enemy.characterView.skeleton.getColor().a = 1;
            enemy.drawSkeleton(canvas);
        }

        for (EnemyEntity e : enemyList) {
            if (e != null && e.isAlive()) {
                //e.draw(canvas);

                //Draw detection icon if enemy just found an octopus
                if (e.hasTarget() && !e.isChasing()) {
                    colorCache.set(DETECTED_INDICATOR_COLOR).sub(0,0,0,0.9f * (e.getDetectionCounter()/e.getMaxDetection()));
                    float xPos = (e.getPosition().x * scale.x);
                    float yPos = (e.getPosition().y * scale.y);
                    canvas.draw(detectionIndicator, colorCache, -e.getTexture().getRegionWidth()/8f, -e.getTexture().getRegionHeight()/8f,
                            xPos, yPos, e.getAngle(), e.getResizeScale()*2, e.getResizeScale()*2);
                }

                //Draw Range Indicators on frozen
                if (frozen && !complete) {
                    float rangePixels = (e.getRange() * scale.x) + 160f;
                    canvas.draw(rangeIndicator, RANGE_INDICATOR_COLOR, rangePixels / 2f, rangePixels / 2f,
                            e.getX() * scale.x, e.getY() * scale.y, rangePixels, rangePixels);
                    if (e.hasTarget()) {
                        float angle = (float) Math.atan2(e.getTarget().getPosition().y - e.getPosition().y,
                                e.getTarget().getPosition().x - e.getPosition().x);
                        float scalex = directionArrow.getRegionWidth() / (e.getTexture().getRegionWidth() * e.getResizeScale()) / 2f;
                        float scaley = scalex / 4f;
                        canvas.draw(directionArrow, Color.RED, 0, directionArrow.getRegionHeight()/2f,
                                e.getX() * scale.x, e.getY() * scale.y, angle, scalex, scaley);
                    }
                }
            }
        }
        // Attack Animations for all attaching enemies
        Iterator<animationObject> it = attackAnimation.iterator();
        while (it.hasNext()) {
            animationObject a = it.next();
            Vector2 pos = a.getPosition();
            int attackFrame = a.getFrame();
            float range = a.getRange();
            if (attackFrame >= 0 && attackFrame < ATTACK_FRAMES) {
                attack.setFrame(attackFrame);
                if (!frozen) a.incFrame();
                canvas.draw(attack, com.badlogic.gdx.graphics.Color.WHITE,attack.getRegionWidth()/2f,
                        attack.getRegionHeight()/2f,pos.x*scale.x,pos.y*scale.y,0f,scale.x/range,scale.y/range);
                //attack.getRegionWidth()/range,attack.getRegionHeight()/range);
            } else if (attackFrame > ATTACK_FRAMES) {
                it.remove();
            }
        }
        // Countdown Animations for all tracing enemies
        Iterator<animationObject> itCount = countingAnimation.iterator();
        while (itCount.hasNext()) {
            animationObject a = itCount.next();
            if (!a.getAlive()) itCount.remove(); // If the target is no longer alive, remove it.
            Vector2 pos = a.getPosition();
            int countFrame = a.getFrame();
            float range = a.getRange();
            if (countFrame >= 0 && countFrame < COUNTER_FRAMES) {
                countdown.setFrame(countFrame);
                if (!frozen) a.incFrame();
                // Incorporating range of attack to here.
                canvas.draw(countdown, com.badlogic.gdx.graphics.Color.WHITE,countdown.getRegionWidth()/2f,
                        countdown.getRegionHeight()/2f,pos.x*scale.x,pos.y*scale.y,0f,
                        range,range);
            } else if (countFrame > COUNTER_FRAMES) {
                itCount.remove();
            }
        }
        canvas.end();
    }
    //#endregion
    //=================================
}
