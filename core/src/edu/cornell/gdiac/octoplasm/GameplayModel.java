package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.octoplasm.entity.Entity;
import edu.cornell.gdiac.octoplasm.util.PooledList;

public class GameplayModel {

    //=========================================================================
    //#region Fields
    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1/60.0f;
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;
    /** Reference to the circle texture */
    private static final String CIRCLE_FILE = "images/circle_small.png";
    /** Reference to the octopus selector texture */
    private static final String SELECTOR_TEXTURE = "images/octo_selector.png";
    /** Reference to the world pausing texture */
    private static final String PAUSE_TEXTURE = "images/pause.png";
    /** Reference to the world pausing texture */
    private static final String OCTOGOAL_TEXTURE = "images/octo_goal.png";
    /** Reference to the world pausing texture */
    private static final String GOALLINE_TEXTURE = "images/goalline.png";

    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** All the objects in the world. */
    protected PooledList<Entity> objects  = new PooledList<Entity>();
    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** keep track of if time is frozen */
    private boolean frozen;
    /** Collision controller for controlling collisions*/
    private CollisionController CC;
    /** OctopusController for doing octopus controlling*/
    private OctopusController OC;
    /** EnemyController for doing enemy controlling*/
    private EnemyController EC;
    //#endregion
    //=================================
}
