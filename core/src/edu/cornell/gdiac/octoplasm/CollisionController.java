package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import edu.cornell.gdiac.octoplasm.entity.*;
import edu.cornell.gdiac.octoplasm.util.AudioController;

/**
 * The CollisionController class processes special physics collisions
 * that are detected by ContactListener.
 *
 * @author Tricia Park
 */
public class CollisionController implements ContactListener {

    //=========================================================================
    //#region Fields
    /** How many frames after winning/losing do we continue? */
    public static final int EXIT_COUNT = 120;

    /** Countdown active for winning or losing */
    private int countdown = -1;
    /** Whether we have completed this level */
    private boolean complete;
    /** the obstacle controller */
    private ObstacleController BC;
    /** the octopus controller */
    private OctopusController OC;
    /** Whether we are in reference mode */
    private boolean referenceMode;
    /** Octopus reference table */
    private Table refTable;
    /** the image for the ref table */
    private String refImage = "ui/gameplay/blank_ref.png";
    private ObstacleEntity goal;

    //Loaded in Gameplay Controller
    /**  */
    private static final String FLIGHT_WALL = "sounds/gameplay/flight_wall.wav";
    /**  */
    private static final String FOLD_WALL = "sounds/gameplay/fold_wall.wav";
//    /** The asset manager */
//    private AssetManager manager;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * creates a new collision controller
     *
     * @param BC
     */
    public CollisionController(ObstacleController BC, OctopusController OC) {
        this.BC = BC;
        this.OC = OC;
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters
    /**
     * Returns true if the level is completed
     *
     * @return true if the level is completed
     */
    public boolean isComplete( ) {
        return complete;
    }

    /**
     * Sets whether the level is completed
     *
     * If true, the level will advance after a countdown
     *
     * @param value whether the level is completed
     */
    public void setComplete(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        complete = value;
    }

    /**
     * TODO documentation
     *
     * @return
     */
    public int getCountdown() { return countdown; }

    public ObstacleEntity getGoalReached() {
        return goal;
    }

    //#endregion
    //=================================

    /**
     * TODO documentation
     */
    public void decrementCountdown() { countdown--; }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if an octopus made it to the win door, and to process some collisions
     * between the octopi and the obstacles.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        AudioController audio = AudioController.getInstance();
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        Entity entity1 = (Entity) body1.getUserData();
        Entity entity2 = (Entity) body2.getUserData();

        //selector collisons
        if(entity1.getEntityType() == Entity.EntityType.SELECTOR){
            entity1.addCollidingWith(entity2);
        }
        if(entity2.getEntityType() == Entity.EntityType.SELECTOR){
            entity2.addCollidingWith(entity1);
        }

        //wall sensor collisons for level editor
        if(entity1.getEntityType() == Entity.EntityType.WALL && entity2.getEntityType() != Entity.EntityType.SELECTOR){
            entity1.addCollidingWith(entity2);
//            System.out.println("hi");
        }
        if(entity2.getEntityType() == Entity.EntityType.WALL && entity1.getEntityType() != Entity.EntityType.SELECTOR){
            entity2.addCollidingWith(entity1);
//            System.out.println("hi");
        }
        // octopus and octopus collision
//        if (entity1.getEntityType() == Entity.EntityType.OCTOPUS && entity2.getEntityType() == Entity.EntityType.OCTOPUS) {
//            OctopusEntity oct1 = (OctopusEntity) entity1;
//            OctopusEntity oct2 = (OctopusEntity) entity2;

//            Entity tele1 = oct1.getTeleportEntity();
//            Entity tele2 = oct2.getTeleportEntity();
//            if (tele1 != null) {
//                tele1.setOctopusTeleportEntity(null);
//                tele1.setLinearVelocity(new Vector2(0,0));
//                oct1.setTeleportEntity(null);
//                oct1.setAlive(false);
//                oct1.setActive(false);
//            } else if (tele2 != null) {
//                tele2.setOctopusTeleportEntity(null);
//                oct2.setTeleportEntity(null);
//                oct2.setAlive(false);
//                oct2.setActive(false);
//            }
//        }
        // octopus and enemy collisions
        if (entity1.getEntityType() == Entity.EntityType.OCTOPUS
                && entity2.getEntityType() == Entity.EntityType.ENEMY) {
//            System.out.println("enemy");
            EnemyEntity enemy = (EnemyEntity) entity2;
            if (((OctopusEntity)entity1).getTeleportEntity() == null) {
                enemy.addCollisionOctopi((OctopusEntity)entity1);
            } else if (((OctopusEntity)entity1).getOctopusSubType() == OctopusEntity.OctopusSubType.EXPLODER) {
                entity1.setPosition(enemy.getPosition());
            }
        }
        else if (entity1.getEntityType() == Entity.EntityType.ENEMY
                && entity2.getEntityType() == Entity.EntityType.OCTOPUS) {
//            System.out.println("enemy");
            EnemyEntity enemy = (EnemyEntity) entity1;
            if (((OctopusEntity)entity2).getTeleportEntity() == null) {
                enemy.addCollisionOctopi((OctopusEntity)entity2);
            } else if (((OctopusEntity)entity2).getOctopusSubType() == OctopusEntity.OctopusSubType.EXPLODER) {
                entity2.setPosition(enemy.getPosition());
            }
        }
        // octopus and obstacle collisions
        else if (entity1.getEntityType() == Entity.EntityType.OCTOPUS
                && entity2.getEntityType() == Entity.EntityType.OBSTACLE) {
            ObstacleEntity obstacle = (ObstacleEntity) entity2;
            OctopusEntity octopus = (OctopusEntity) entity1;
            switch (obstacle.getObstacleSubType()) {
                case RESPAWN:
                    // TODO need to change to better way
                    if (OC.getDeadOctopi().size() != 0) {
                        BC.setToFreeze(true);
                        BC.setRespawnActive(true);
                        BC.createRespawnTable(obstacle);
                        Gdx.input.setCursorCatched(false);
                        obstacle.markRemoved(true);
                    }
                    break;
                case GOAL:
                    setComplete(true);
                    goal = obstacle;
                    break;
                case FLIGHT_WALL:
                    if (octopus.getOctopusSubType() == OctopusEntity.OctopusSubType.FLYER && octopus.isAbilityActive()) {
                        audio.playSound("flightWall", FLIGHT_WALL, false); //Flight Wall Broken
                        obstacle.setSensor(true);
                        obstacle.setIsSensor(true);
                    }
                    break;
                case FIGHT_WALL:
                    if (octopus.getOctopusSubType() == OctopusEntity.OctopusSubType.EXPLODER) {
                        OC.fightCollide = obstacle;
                    }
                    break;
            }
        }
        else if (entity1.getEntityType() == Entity.EntityType.OBSTACLE
                && entity2.getEntityType() == Entity.EntityType.OCTOPUS) {
            ObstacleEntity obstacle = (ObstacleEntity) entity1;
            OctopusEntity octopus = (OctopusEntity) entity2;
            switch (obstacle.getObstacleSubType()) {
                case RESPAWN:
                    // TODO need to change to better way
                    if (OC.getDeadOctopi().size() != 0) {
                        BC.setToFreeze(true);
                        BC.setRespawnActive(true);
                        BC.createRespawnTable(obstacle);
                        Gdx.input.setCursorCatched(false);
                        obstacle.markRemoved(true);
                    }
                    break;
                case GOAL:
                    setComplete(true);
                    goal = obstacle;
                    break;
                case FLIGHT_WALL:
                    if (octopus.getOctopusSubType() == OctopusEntity.OctopusSubType.FLYER && octopus.isAbilityActive()) {
                        audio.playSound("flightWall", FLIGHT_WALL, false); //Flight Wall Broken
//                        obstacle.setActive(false);
                        obstacle.setSensor(true);
                        obstacle.setIsSensor(true);
                    }
                    break;
                case FIGHT_WALL:
                    if (octopus.getOctopusSubType() == OctopusEntity.OctopusSubType.EXPLODER) {
                        OC.fightCollide = obstacle;
                    }
                    break;
            }
        }
//        // enemy and enemy collision (bounce, do not need to process)
//        else if (entity1.getEntityType() == Entity.EntityType.ENEMY
//                && entity2.getEntityType() == Entity.EntityType.ENEMY) {
//
//        }
//        // enemy and obstacle collisions (bounce, do not need to process)
//        else if (entity1.getEntityType() == Entity.EntityType.ENEMY
//                && entity2.getEntityType() == Entity.EntityType.OBSTACLE) {
//
//        }
//        else if (entity1.getEntityType() == Entity.EntityType.OBSTACLE
//                && entity2.getEntityType() == Entity.EntityType.ENEMY) {
//
//        }
//        // obstacle and obstacle collision
//        else if (entity1.getEntityType() == Entity.EntityType.OBSTACLE
//                && entity2.getEntityType() == Entity.EntityType.OBSTACLE) {
//
//        }

        if((entity1.getEntityType() == Entity.EntityType.SELECTOR) && (entity2.getEntityType() == Entity.EntityType.WALL)) {
            ((BoxEntity)entity1).incrementWallsTouched();
        }else if((entity1.getEntityType() == Entity.EntityType.WALL) && (entity2.getEntityType() == Entity.EntityType.SELECTOR)) {
            ((BoxEntity)entity2).incrementWallsTouched();
        }

        // fold collisions
        if (entity1.isGrabbed() || entity2.isGrabbed()) {
            Entity grabbed = entity1;
            Entity other = entity2;
            if (entity2.isGrabbed()) {
                grabbed = entity2;
                other = entity1;
            }
//            System.out.println("grabbed is colliding");
//            System.out.println(grabbed.getEntityType());
//            System.out.println(grabbed.getBodyType());
//            System.out.println(other.getEntityType());
            OctopusEntity fold = grabbed.getOctopusTeleportEntity();
            // TODO do we want octopi to be hurt when hit by grabbed? or vice versa?
            if (other == fold) {
                // grabbed collides with fold
//                System.out.println("fold colliding with grabbed");
                grabbed.setOctopusTeleportEntity(null);
                grabbed.setLinearVelocity(new Vector2(0,0));
                if (grabbed.getEntityType() == Entity.EntityType.OBSTACLE) {
                    // invariant: only obstacle that can be grabbed is fold wall
                    grabbed.setIsDead(true);
                    audio.playSound("foldWall", FOLD_WALL, false); // FOLD_WALL
                    // TODO add animation
                }
                fold.setTeleportEntity(null);
                fold.setAbilityActive(false);
                if (fold.getGrabCharges() < 1) {
                    fold.setIsDead(true);
                    if (!OC.getDeadOctopi().contains(fold)) OC.getDeadOctopi().add(fold);
                }
                fold.setIsGrab(false);

                // TODO add fold death animation
                grabbed.setGrabbed(false);
            } else if (other.getEntityType() == Entity.EntityType.ENEMY) {
                // grabbed collides with enemy
                if (grabbed.getEntityType() == Entity.EntityType.ENEMY) {
                    // grabbed is an enemy
                    if (((EnemyEntity)grabbed).getEnemySubType() != EnemyEntity.EnemySubType.INVINCIBLE_ENEMY) {
                        grabbed.setIsDead(true);
//                        fold.setTeleportEntity(null);
                        fold.setAbilityActive(false);
                        if (fold.getGrabCharges() < 1) {
                            fold.setIsDead(true);
                            if (!OC.getDeadOctopi().contains(fold)) OC.getDeadOctopi().add(fold);
                        }
                        fold.setIsGrab(false);
                    }
                    if (((EnemyEntity)other).getEnemySubType() != EnemyEntity.EnemySubType.INVINCIBLE_ENEMY && ((EnemyEntity)other).getEnemySubType() != EnemyEntity.EnemySubType.SPIKED_ENEMY) {
                        other.setIsDead(true);
                    }
                    // TODO add animation
                } else if (grabbed.getEntityType() == Entity.EntityType.OBSTACLE) {
                    // grabbed is an obstacle
                    // invariant: only obstacle that can be grabbed is fold wall
                    if (((EnemyEntity)other).getEnemySubType() != EnemyEntity.EnemySubType.INVINCIBLE_ENEMY) {
                        other.setIsDead(true);
                    }
                    // TODO add animation
                } else if (grabbed.getEntityType() == Entity.EntityType.OCTOPUS) {
                    // grabbed is an octopus
                    OctopusEntity oct = (OctopusEntity) grabbed;
                    if (oct.getOctopusSubType() == OctopusEntity.OctopusSubType.FLYER && oct.getFlying()) {
                        other.setIsDead(true);
                    } else {
                        grabbed.setIsDead(true);
                        fold.setTeleportEntity(null);
                        fold.setAbilityActive(false);
                        if (fold.getGrabCharges() < 1) {
                            fold.setIsDead(true);
                            if (!OC.getDeadOctopi().contains(fold)) OC.getDeadOctopi().add(fold);
                        }
                        fold.setIsGrab(false);
                    }
                    if (fold.getGrabCharges() < 1) {
                        fold.setIsDead(true);
                        if (!OC.getDeadOctopi().contains(fold)) OC.getDeadOctopi().add(fold);
                    }
                    fold.setIsGrab(false);
                    // TODO add animation
                }
            } else if (other.getEntityType() == Entity.EntityType.OCTOPUS) {
                // grabbed collides with octopus
//                System.out.println("grabbed colliding with octopus");
                OctopusEntity oct = (OctopusEntity) other;
                if (grabbed.getEntityType() == Entity.EntityType.ENEMY) {
                    // grabbed is an enemy
                    if (oct.getOctopusSubType() == OctopusEntity.OctopusSubType.FLYER && oct.getFlying()) {
                        grabbed.setIsDead(true);
                        fold.setTeleportEntity(null);
                        fold.setAbilityActive(false);
                        if (fold.getGrabCharges() < 1) {
                            fold.setIsDead(true);
                            if (!OC.getDeadOctopi().contains(fold)) OC.getDeadOctopi().add(fold);
                        }
                        fold.setIsGrab(false);
                    } else {
                        other.setIsDead(true);
                        if (!OC.getDeadOctopi().contains((OctopusEntity)other)) OC.getDeadOctopi().add((OctopusEntity)other);
                    }
                    // TODO add animation
                }
            }
        }

    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  We do not use it.
     */
    public void endContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        Entity entity1 = (Entity) body1.getUserData();
        Entity entity2 = (Entity) body2.getUserData();
        //selector collisons
        //TODO: we are setting collidingwith to null when we stop colliding with a second object
        if(entity1.getEntityType() == Entity.EntityType.SELECTOR){
            ((BoxEntity)entity1).removeCollidingWith(entity2);
            refImage = "ui/gameplay/blank_ref.png";
        }
        if(entity2.getEntityType() == Entity.EntityType.SELECTOR){
            ((BoxEntity)entity2).removeCollidingWith(entity1);
            refImage = "ui/gameplay/blank_ref.png";
        }
        // octopus and enemy collisions
        if (entity1.getEntityType() == Entity.EntityType.OCTOPUS
                && entity2.getEntityType() == Entity.EntityType.ENEMY) {
//            entity1.setHealth(entity1.getHealth()-ENEMY_DAMAGE);
//            entity1.setActive(false);
            EnemyEntity enemy = (EnemyEntity) entity2;
            enemy.removeCollisionOctopi((OctopusEntity) entity1);
        }
        else if (entity1.getEntityType() == Entity.EntityType.ENEMY
                && entity2.getEntityType() == Entity.EntityType.OCTOPUS) {
//            entity2.setHealth(entity2.getHealth()-ENEMY_DAMAGE);
//            entity1.setActive(false);
            EnemyEntity enemy = (EnemyEntity) entity1;
            //TODO: this is killing octopi that we don't want to kill (in level editor this causes the wrong octopi to
            //when selecting a placed enemy while entitytoput is an octopi
            enemy.removeCollisionOctopi((OctopusEntity) entity2);
        }
        else if (entity1.getEntityType() == Entity.EntityType.OCTOPUS
                && entity2.getEntityType() == Entity.EntityType.OBSTACLE) {
            OctopusEntity octopus = (OctopusEntity) entity1;
            ObstacleEntity obstacle = (ObstacleEntity) entity2;
            if (octopus.getOctopusSubType() == OctopusEntity.OctopusSubType.EXPLODER
                    && obstacle.getObstacleSubType() == ObstacleEntity.ObstacleSubType.FIGHT_WALL) {
                OC.fightCollide = null;
            }
        }
        else if (entity1.getEntityType() == Entity.EntityType.OBSTACLE
                && entity2.getEntityType() == Entity.EntityType.OCTOPUS) {
            OctopusEntity octopus = (OctopusEntity) entity2;
            ObstacleEntity obstacle = (ObstacleEntity) entity1;
            if (octopus.getOctopusSubType() == OctopusEntity.OctopusSubType.EXPLODER
                    && obstacle.getObstacleSubType() == ObstacleEntity.ObstacleSubType.FIGHT_WALL) {
                OC.fightCollide = null;
            }
        }

        if((entity1.getEntityType() == Entity.EntityType.SELECTOR) && (entity2.getEntityType() == Entity.EntityType.WALL)) {
            ((BoxEntity)entity1).decrementWallsTouched();
        }else if((entity1.getEntityType() == Entity.EntityType.WALL) && (entity2.getEntityType() == Entity.EntityType.SELECTOR)) {
            ((BoxEntity)entity2).decrementWallsTouched();
        }
    }

    /**
     * Handles any modifications necessary before collision resolution
     *
     * This method is called just before Box2D resolves a collision.
     *
     * @param  contact  	The two bodies that collided
     * @param  oldManifold  	The collision manifold before contact
     */
    public void preSolve(Contact contact, Manifold oldManifold) {}

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}
