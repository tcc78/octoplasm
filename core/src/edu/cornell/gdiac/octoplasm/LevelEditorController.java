package edu.cornell.gdiac.octoplasm;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.octoplasm.entity.*;
import edu.cornell.gdiac.octoplasm.util.*;

import javax.swing.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import static edu.cornell.gdiac.octoplasm.GameplayController.*;

/**
 * TODO documentation
 */
public class LevelEditorController {

    //=========================================================================
    //#region Fields
    // TODO make getters and setters for these
    /** TODO documentation */
//    LevelModel model;
    /** TODO documentation */
    GameCanvas canvas;
    /** TODO documentation */
    Rectangle bounds;
    /** TODO documentation */
    PooledList<Entity> objects;
    /** TODO documentation */
    Vector2 scale;
    /** TODO documentation */
    OctopusController OC;
    /** TODO documentation */
    EnemyController EC;
    /** TODO documentation */
    CollisionController CC;
    /** TODO documentation */
    ObstacleController BC;
    /** TODO documentation */
    Entity.EntityType typeToPut = Entity.EntityType.NULL;
    /** TODO documentation */
    Entity entityToPut;
    /** TODO documentation */
    int octCounter = 0;
    /** TODO documentation */
    boolean buildWall;
    /** TODO documentation */
    LinkedList<Float> walls = new LinkedList<>();
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** String indicating whether we are attempting to load a level */
    private String loadLevelFlag;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * TODO documentation
     *
     * @param canvas
     * @param bounds
     * @param objects
     * @param scale
     * @param OC
     * @param EC
     * @param CC
     * @param BC
     */
    public LevelEditorController(GameCanvas canvas, Rectangle bounds, PooledList<Entity> objects,
                                 Vector2 scale, OctopusController OC, EnemyController EC, CollisionController CC,
                                 ObstacleController BC){
        this.canvas = canvas;
        this.bounds = bounds;
        this.objects = objects;
        this.scale = scale;
        this.OC = OC;
        this.EC = EC;
        this.CC = CC;
        this.BC = BC;
//        model = new LevelModel(new LinkedList<float[]>(),null,
//                new LinkedList<ObstacleEntity.ObstacleSubType>(), new LinkedList<Vector2>(),
//                new LinkedList<EnemyEntity.EnemySubType>(), new LinkedList<Vector2>(), new LinkedList<OctopusEntity.OctopusSubType>(),
//                new LinkedList<Vector2>());
    }
    //#endregion
    //=================================

    //=========================================================================
    //#region Getters and Setters

    /**
     * returns whether we want to load a level
     * @return whether we want to load a level
     */
    public String getLoadLevelFlagged(){
        return loadLevelFlag;
    }
    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas,Rectangle bounds) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth() / bounds.getWidth();
        this.scale.y = canvas.getHeight() / bounds.getHeight();
//        if(canvas.getWidth()*9 < canvas.getHeight()*16){
//            //height should be kept, width should be bigger than canvas width
//            this.scale.x = this.scale.y;
//        } else if(canvas.getWidth()*9 > canvas.getHeight()*16){
//            this.scale.y = this.scale.x;
//        }
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
    //#endregion
    //=================================

    /**
     * TODO documentation
     *
     * @param selector
     * @param dt
     * @param world
     */
    public void update(BoxEntity selector,float dt, World world,LevelModel model) {
        canvas.updateCamera();
        if(loadLevelFlag != null) loadLevelFlag = null;
        InputController input = InputController.getInstance();

        //input.readInput(bounds,scale);
        Vector2 crosshair = input.getCrossHair();
        selector.setPosition(crosshair.x, crosshair.y);

        if(input.wIsHeld()){canvas.moveCameraUp((bounds.y + bounds.height)*scale.y);}
        if(input.sIsHeld()){canvas.moveCameraDown((bounds.y)*scale.y);}
        if(input.aIsHeld()){canvas.moveCameraLeft((bounds.x)*scale.x);}
        if(input.dIsHeld()){canvas.moveCameraRight((bounds.x + bounds.width)*scale.x);}
        Vector2 crshair = input.getCrossHair();
        selector.setPosition(crshair.x,crshair.y);
        // MOUSE CURSOR CODE
        if (input.didSelectWall()) {
            if (!buildWall) {
                listener.setCursor(ScreenListener.CursorState.WALL);
            }
        } else if (entityToPut != null) {
            listener.setCursor(ScreenListener.CursorState.TRANSPARENT);
            entityToPut.setPosition(crosshair.x, crosshair.y);
        } else if (input.didPressLeft() && !buildWall) {
            listener.setCursor(ScreenListener.CursorState.CLICKED);
        } else if (input.didReleaseLeft() || input.didReleaseRight() && !buildWall) {
            listener.setCursor(ScreenListener.CursorState.DEFAULT);
        }

        //TODO: make walls not collide
        //TODO: make undo for vertices
        //TODO: make boundary walls
//        if(!CC.isTouching() && checkOverlap != null){
////            switch(checkOverlap.getEntityType()){
////                case ENEMY:
////                    EnemyEntity enemy = (EnemyEntity)checkOverlap;
////                    enemy.setSensor(false);
////                    enemy.setName("enemy");
////                    objects.add(enemy);
////                    EC.addEnemy(enemy);
////                    checkOverlap = null;
////                    break;
////                case OCTOPUS:
////                    OctopusEntity oct = (OctopusEntity) checkOverlap;
////                    oct.setSensor(false);
////                    oct.setName("octopus");
////                    objects.add(oct);
////                    OC.addOctopus(oct);
////                    checkOverlap = null;
////                    break;
////                case OBSTACLE:
////                    ObstacleEntity obst = (ObstacleEntity) checkOverlap;
////                    obst.setSensor(false);
////                    obst.setName("obstacle");
////                    objects.add(obst);
////                    checkOverlap = null;
////                    break;
////            }
//
//        } else if(CC.isTouching() && checkOverlap != null){
//            checkOverlap.setActive(false);
//            checkOverlap = null;
//            System.out.println("Error: objects overlapping");
//        }

        // drawing walls
        if (input.didSelectWall()) { // || buildWall && (input.didSelectOctopus()||input.didSelectEnemy()||input.didSelectOctopus())) {
            if(entityToPut != null) entityToPut.setActive(false);
            entityToPut = null;
            if (typeToPut != Entity.EntityType.WALL) {
                buildWall = true;
                typeToPut = Entity.EntityType.WALL;
            } else {
                if (walls.size() > 5) {
                    float[] floats = new float[walls.size()];
                    int i = 0;
                    for (Float fl : walls){
                        floats[i++] = fl;
                    }

//                    LevelLoader.addWall(world, model,scale,objects,floats);
                    WallEntity wall1 = new WallEntity(floats, 0, 0);
                    wall1.setBodyType(BodyDef.BodyType.StaticBody);
                    wall1.setDrawScale(scale);
                    boolean error = true;
                    while(error) {
                        String tex = JOptionPane.showInputDialog("Enter a texture");
                        switch (tex) {
                            case "EARTH":
                                wall1.setTexture(WallEntity.WallTexture.EARTH);
                                error = false;
                                break;
                            case "PIRATE":
                                wall1.setTexture(WallEntity.WallTexture.PIRATE);
                                error = false;
                                break;
                            case "WOODEN":
                                wall1.setTexture(WallEntity.WallTexture.WOODEN);
                                error = false;
                                break;
                            default:
                                System.out.println("Error: unrecognized texture. Choose one of: EARTH, PIRATE, WOODEN");
                        }
                    }

                    //TODO: set edge texture
                    wall1.setName("wall");
                    wall1.activatePhysics(world);
                    wall1.setActive(true);
                    wall1.setSensor(true);
                    objects.add(wall1);
                    world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);
                    if (wall1.getCollidingWithSize() > 0) {
                        //Check if issue is a false alarm
                        boolean allTutorialIcons = true;
                        for (int j = 0; j < wall1.getCollidingWithSize() && allTutorialIcons; j++) {
                            Entity temp = selector.getCollidingWith(j);
                            allTutorialIcons = temp.getEntityType() == Entity.EntityType.TUTORIAL;
                        }
                        if (!allTutorialIcons) {
                            //todo figure out garbage collection
                            wall1.setActive(false);
                            objects.remove(wall1);
                            typeToPut = Entity.EntityType.WALL;
                            buildWall = true;
                        } else {
                            wall1.setSensor(false);
                            walls.clear();
                            typeToPut = Entity.EntityType.NULL;
                            buildWall = false;
                            listener.setCursor(ScreenListener.CursorState.DEFAULT);
                        }
                    } else {
                        wall1.setSensor(false);
                        walls.clear();
                        typeToPut = Entity.EntityType.NULL;
                        buildWall = false;
                        listener.setCursor(ScreenListener.CursorState.DEFAULT);
                    }
                } else{
                    System.out.println("Error: need at least three vertices");
                    buildWall = true;
                    listener.setCursor(ScreenListener.CursorState.WALL);
                }
            }
        }
        // picking up stuff
        if(input.didClickLeft() && selector.getCollidingWithSize() > 0 &&
                (selector.getCollidingWith(0) != entityToPut || selector.getCollidingWithSize() > 1)){
            //should delete the second object collided with in selector.collidingWith
            if(entityToPut != null) entityToPut.setActive(false);
            Entity colliding = selector.getCollidingWith(0);
            //Switch to any tutorial icons
            if (selector.getCollidingWithSize() >= 1) {
                for (int i = 1; i < selector.getCollidingWithSize(); i++) {
                    Entity temp = selector.getCollidingWith(i);
                    if (temp.getEntityType() == Entity.EntityType.TUTORIAL)
                        colliding = temp;
                }
            }

            switch(colliding.getEntityType()){
                case OCTOPUS:
                    objects.remove(colliding);
                    entityToPut = colliding;
                    entityToPut.setSensor(true);
                    typeToPut = Entity.EntityType.OCTOPUS;
                    octCounter--;
                    OC.removeOctopus((OctopusEntity)colliding);
                    break;
                case OBSTACLE:
                    objects.remove(colliding);
                    entityToPut = colliding;
                    entityToPut.setSensor(true);
                    typeToPut = Entity.EntityType.OBSTACLE;
                    BC.removeObstacle((ObstacleEntity)colliding);
                    break;
                case WALL:
                    objects.remove(colliding);
                    //todo garbage collection??
                    colliding.setActive(false);
                    typeToPut = Entity.EntityType.WALL;
                    walls.clear();
                    float[] verts = ((WallEntity)colliding).getOrigVertices();
                    for(int i = 0; i < verts.length; i++){
                        walls.add(verts[i]);
                    }
                    break;
                case ENEMY:
                    objects.remove(colliding);
                    entityToPut = colliding;
                    entityToPut.setSensor(true);
                    typeToPut = Entity.EntityType.ENEMY;
                    EC.removeEnemy((EnemyEntity)colliding);
                    break;
                case TUTORIAL:
                    objects.remove(colliding);
                    entityToPut = colliding;
                    entityToPut.setSensor(true);
                    typeToPut = Entity.EntityType.TUTORIAL;
                    BC.removeTutorial((TutorialEntity)colliding);
                    break;
                default:
                    break;
            }
        }
        // putting stuff down
        if(input.didClickRight()){
            if (selector.getCollidingWithSize() < 2 || typeToPut == Entity.EntityType.TUTORIAL) {
                switch(typeToPut) {
                    case OCTOPUS:
                        if (octCounter < 3) {
                            octCounter++;
                            OctopusEntity oct = (OctopusEntity) entityToPut;
                            oct.setSensor(false);
                            oct.setName("octopus");
                            objects.add(oct);
                            OC.addOctopus(oct);
                            entityToPut = null;
                            typeToPut = Entity.EntityType.NULL;
                        } else {
                            System.out.println("too many octopi, delete one before adding another (commit social distancing)");
                        }
                        break;
                    case ENEMY:
                        entityToPut.setSensor(false);
                        entityToPut.setName("enemy");
                        objects.add(entityToPut);
                        EC.addEnemy((EnemyEntity)entityToPut);
                        entityToPut = null;
                        typeToPut = Entity.EntityType.NULL;
                        break;
                    case OBSTACLE:
                        ObstacleEntity obst = (ObstacleEntity) entityToPut;
                        obst.setSensor(true);
                        obst.setName("obstacle");
                        objects.add(obst);
                        BC.addObstacle((ObstacleEntity) entityToPut);
                        entityToPut = null;
                        typeToPut = Entity.EntityType.NULL;
                        break;
                    case WALL:
                        walls.add(input.getCrossHair().x);
                        walls.add(input.getCrossHair().y);
                        break;
                    case TUTORIAL:
                        objects.add(entityToPut);
                        BC.addTutorial((TutorialEntity) entityToPut);
                        entityToPut = null;
                        typeToPut = Entity.EntityType.NULL;
                        break;
                    default:
                        System.out.println("Error: unidentified type added");
                }
            } else {
                System.out.println("Error: objects colliding, try another place");
            }
        }
        // pressed e
        else if(input.didSelectEnemy()){
            if(entityToPut != null) entityToPut.setActive(false);
            if(typeToPut == Entity.EntityType.ENEMY){
                switch(((EnemyEntity)entityToPut).getEnemySubType()){
                    case NORMAL_ENEMY:
                        entityToPut = new EnemyEntity(crosshair.x,crosshair.y,
                                EnemyEntity.getTextureWidth(EnemyEntity.EnemySubType.ARMORED_ENEMY)/scale.x,
                                EnemyEntity.getTextureHeight(EnemyEntity.EnemySubType.ARMORED_ENEMY)/scale.y,
                                EnemyEntity.EnemySubType.ARMORED_ENEMY);
                        entityToPut.setDrawScale(scale);
                        entityToPut.setSensor(true);
                        entityToPut.activatePhysics(world);
                        break;
                    case ARMORED_ENEMY:
                        entityToPut = new EnemyEntity(crosshair.x,crosshair.y,
                                EnemyEntity.getTextureWidth(EnemyEntity.EnemySubType.SPIKED_ENEMY)/scale.x,
                                EnemyEntity.getTextureHeight(EnemyEntity.EnemySubType.SPIKED_ENEMY)/scale.y,
                                EnemyEntity.EnemySubType.SPIKED_ENEMY);
                        entityToPut.setDrawScale(scale);
                        entityToPut.setSensor(true);
                        entityToPut.activatePhysics(world);
                        break;
                    case SPIKED_ENEMY:
                        entityToPut = new EnemyEntity(crosshair.x,crosshair.y,
                                EnemyEntity.getTextureWidth(EnemyEntity.EnemySubType.HOLE_ENEMY)/scale.x,
                                EnemyEntity.getTextureHeight(EnemyEntity.EnemySubType.HOLE_ENEMY)/scale.y,
                                EnemyEntity.EnemySubType.HOLE_ENEMY);
                        entityToPut.setDrawScale(scale);
                        entityToPut.setSensor(true);
                        entityToPut.activatePhysics(world);
                        break;
                    case HOLE_ENEMY:
                        entityToPut = new EnemyEntity(crosshair.x,crosshair.y,
                                EnemyEntity.getTextureWidth(EnemyEntity.EnemySubType.INVINCIBLE_ENEMY)/scale.x,
                                EnemyEntity.getTextureHeight(EnemyEntity.EnemySubType.INVINCIBLE_ENEMY)/scale.y,
                                EnemyEntity.EnemySubType.INVINCIBLE_ENEMY);
                        entityToPut.setDrawScale(scale);
                        entityToPut.setSensor(true);
                        entityToPut.activatePhysics(world);
                        break;
                    case INVINCIBLE_ENEMY:
                        entityToPut = new EnemyEntity(crosshair.x,crosshair.y,
                                EnemyEntity.getTextureWidth(EnemyEntity.EnemySubType.NORMAL_ENEMY)/scale.x,
                                EnemyEntity.getTextureHeight(EnemyEntity.EnemySubType.NORMAL_ENEMY)/scale.y,
                                EnemyEntity.EnemySubType.NORMAL_ENEMY);
                        entityToPut.setDrawScale(scale);
                        entityToPut.setSensor(true);
                        entityToPut.activatePhysics(world);
                        break;
                    default:
                }
            } else{
                typeToPut = Entity.EntityType.ENEMY;
                entityToPut = new EnemyEntity(crosshair.x,crosshair.y,
                        EnemyEntity.getTextureWidth(EnemyEntity.EnemySubType.NORMAL_ENEMY)/scale.x,
                        EnemyEntity.getTextureHeight(EnemyEntity.EnemySubType.NORMAL_ENEMY)/scale.y,
                        EnemyEntity.EnemySubType.NORMAL_ENEMY);
                entityToPut.setSensor(true);
                entityToPut.setDrawScale(scale);
                entityToPut.activatePhysics(world);
            }
        }
        // pressed b
        else if(input.didSelectObstacle()){
            if(entityToPut != null) entityToPut.setActive(false);
            if(typeToPut == Entity.EntityType.OBSTACLE){
                switch(((ObstacleEntity)entityToPut).getObstacleSubType()){
                    case FIGHT_WALL:
                        entityToPut = new ObstacleEntity(crosshair.x,crosshair.y, 0,
                                ObstacleEntity.getTextureWidth(ObstacleEntity.ObstacleSubType.FLIGHT_WALL)/scale.x,
                                ObstacleEntity.getTextureHeight(ObstacleEntity.ObstacleSubType.FLIGHT_WALL)/scale.y,
                                ObstacleEntity.ObstacleSubType.FLIGHT_WALL, GameWorld.SHIP);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case FLIGHT_WALL:
                        entityToPut = new ObstacleEntity(crosshair.x,crosshair.y, 0,
                                ObstacleEntity.getTextureWidth(ObstacleEntity.ObstacleSubType.FOLD_WALL)/scale.x,
                                ObstacleEntity.getTextureHeight(ObstacleEntity.ObstacleSubType.FOLD_WALL)/scale.y,
                                ObstacleEntity.ObstacleSubType.FOLD_WALL, GameWorld.SHIP);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case FOLD_WALL:
                        entityToPut = new ObstacleEntity(crosshair.x,crosshair.y, 0,
                                ObstacleEntity.getTextureWidth(ObstacleEntity.ObstacleSubType.RESPAWN)/scale.x,
                                ObstacleEntity.getTextureHeight(ObstacleEntity.ObstacleSubType.RESPAWN)/scale.y,
                                ObstacleEntity.ObstacleSubType.RESPAWN, GameWorld.SHIP);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case RESPAWN:
                        entityToPut = new ObstacleEntity(crosshair.x,crosshair.y, 0,
                                ObstacleEntity.getTextureWidth(ObstacleEntity.ObstacleSubType.GOAL)/scale.x,
                                ObstacleEntity.getTextureHeight(ObstacleEntity.ObstacleSubType.GOAL)/scale.y,
                                ObstacleEntity.ObstacleSubType.GOAL, GameWorld.SHIP);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case GOAL:
                        entityToPut = new ObstacleEntity(crosshair.x,crosshair.y, 0,
                                ObstacleEntity.getTextureWidth(ObstacleEntity.ObstacleSubType.FIGHT_WALL)/scale.x,
                                ObstacleEntity.getTextureHeight(ObstacleEntity.ObstacleSubType.FIGHT_WALL)/scale.y,
                                ObstacleEntity.ObstacleSubType.FIGHT_WALL, GameWorld.SHIP);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    default:
                }
            } else{
                typeToPut = Entity.EntityType.OBSTACLE;
                entityToPut = new ObstacleEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                        ObstacleEntity.getTextureWidth(ObstacleEntity.ObstacleSubType.FIGHT_WALL)/scale.x,
                        ObstacleEntity.getTextureHeight(ObstacleEntity.ObstacleSubType.FIGHT_WALL)/scale.y,
                        ObstacleEntity.ObstacleSubType.FIGHT_WALL, GameWorld.SHIP);
                entityToPut.setSensor(true);
                entityToPut.setDrawScale(scale);
                entityToPut.activatePhysics(world);
            }
        }
        // pressed o
        else if(input.didSelectOctopus()){
            if(entityToPut != null) entityToPut.setActive(false);
            if(typeToPut == Entity.EntityType.OCTOPUS){
                switch(((OctopusEntity)entityToPut).getOctopusSubType()){
                    case FLYER:
                        entityToPut = new OctopusEntity(crosshair.x*scale.x,crosshair.y*scale.y,
                                OctopusEntity.getTextureWidth(OctopusEntity.OctopusSubType.TELEPORTER)/scale.x,
                                OctopusEntity.getTextureHeight(OctopusEntity.OctopusSubType.TELEPORTER)/scale.y,
                                OctopusEntity.OctopusSubType.TELEPORTER);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TELEPORTER:
                        entityToPut = new OctopusEntity(crosshair.x*scale.x,crosshair.y*scale.y,
                                OctopusEntity.getTextureWidth(OctopusEntity.OctopusSubType.EXPLODER)/scale.x,
                                OctopusEntity.getTextureHeight(OctopusEntity.OctopusSubType.EXPLODER)/scale.y,
                                OctopusEntity.OctopusSubType.EXPLODER);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case EXPLODER:
                        entityToPut = new OctopusEntity(crosshair.x*scale.x,crosshair.y*scale.y,
                                OctopusEntity.getTextureWidth(OctopusEntity.OctopusSubType.FLYER)/scale.x,
                                OctopusEntity.getTextureHeight(OctopusEntity.OctopusSubType.FLYER)/scale.y,
                                OctopusEntity.OctopusSubType.FLYER);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    default:
                }
            } else{
                typeToPut = Entity.EntityType.OCTOPUS;
                entityToPut = new OctopusEntity(crosshair.x*scale.x,crosshair.y*scale.y,
                        OctopusEntity.getTextureWidth(OctopusEntity.OctopusSubType.FLYER)/scale.x,
                        OctopusEntity.getTextureHeight(OctopusEntity.OctopusSubType.FLYER)/scale.y,
                        OctopusEntity.OctopusSubType.FLYER);
                entityToPut.setSensor(true);
                entityToPut.setDrawScale(scale);
                entityToPut.activatePhysics(world);
            }
        }
        // pressed y
        else if(input.didTutorialIcon()) {
            if(entityToPut != null) entityToPut.setActive(false);
            if(typeToPut == Entity.EntityType.TUTORIAL) {
                switch(((TutorialEntity)entityToPut).getTutorialSubType()) {
                    //TODO: Implement
                    case CURSOR:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.MOUSE_LEFT)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.MOUSE_LEFT)/scale.y,
                                TutorialEntity.TutorialSubType.MOUSE_LEFT);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case MOUSE_LEFT:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.MOUSE_RIGHT)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.MOUSE_RIGHT)/scale.y,
                                TutorialEntity.TutorialSubType.MOUSE_RIGHT);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case MOUSE_RIGHT:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.E_KEY)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.E_KEY)/scale.y,
                                TutorialEntity.TutorialSubType.E_KEY);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case E_KEY:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.W_KEY)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.W_KEY)/scale.y,
                                TutorialEntity.TutorialSubType.W_KEY);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case W_KEY:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.A_KEY)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.A_KEY)/scale.y,
                                TutorialEntity.TutorialSubType.A_KEY);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case A_KEY:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.S_KEY)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.S_KEY)/scale.y,
                                TutorialEntity.TutorialSubType.S_KEY);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case S_KEY:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.D_KEY)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.D_KEY)/scale.y,
                                TutorialEntity.TutorialSubType.D_KEY);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case D_KEY:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.SPACE_KEY)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.SPACE_KEY)/scale.y,
                                TutorialEntity.TutorialSubType.SPACE_KEY);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case SPACE_KEY:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.ESC_KEY)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.ESC_KEY)/scale.y,
                                TutorialEntity.TutorialSubType.ESC_KEY);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case ESC_KEY:
                    case TEXT1:
                    case TEXT2:
                    case TEXT3:
                    case TEXT4:
                    case TEXT5:
                    case TEXT6:
                    case TEXT7:
                    case TEXT8:
                    case TEXT9:
                    case TEXT10:
                    case TEXT11:
                    case TEXT12:
                    case TEXT13:
                    case TEXT14:
                    case TEXT15:
                    case TEXT16:
                    default:
                        typeToPut = Entity.EntityType.TUTORIAL;
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.CURSOR)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.CURSOR)/scale.y,
                                TutorialEntity.TutorialSubType.CURSOR);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                }
            } else {
                typeToPut = Entity.EntityType.TUTORIAL;
                entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                        TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.CURSOR)/scale.x,
                        TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.CURSOR)/scale.y,
                        TutorialEntity.TutorialSubType.CURSOR);
                entityToPut.setSensor(true);
                entityToPut.setDrawScale(scale);
                entityToPut.activatePhysics(world);
            }
        }
        // pressed y
        else if(input.didTutorialText()) {
            if(entityToPut != null) entityToPut.setActive(false);
            if(typeToPut == Entity.EntityType.TUTORIAL) {
                switch(((TutorialEntity)entityToPut).getTutorialSubType()) {
                    //TODO: Implement
                    case CURSOR:
                    case MOUSE_LEFT:
                    case MOUSE_RIGHT:
                    case E_KEY:
                    case W_KEY:
                    case A_KEY:
                    case S_KEY:
                    case D_KEY:
                    case SPACE_KEY:
                    case ESC_KEY:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT1)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT1)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT1);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT1:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT2)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT2)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT2);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT2:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT3)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT3)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT3);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT3:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT4)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT4)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT4);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT4:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT5)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT5)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT5);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT5:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT6)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT6)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT6);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT6:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT7)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT7)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT7);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT7:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT8)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT8)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT8);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT8:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT9)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT9)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT9);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT9:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT10)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT10)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT10);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT10:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT11)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT11)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT11);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT11:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT12)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT12)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT12);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT12:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT13)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT13)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT13);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT13:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT14)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT14)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT14);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT14:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT15)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT15)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT15);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT15:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT16)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT16)/scale.y,
                                TutorialEntity.TutorialSubType.TEXT16);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    case TEXT16:
                        entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                                TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.CURSOR)/scale.x,
                                TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.CURSOR)/scale.y,
                                TutorialEntity.TutorialSubType.CURSOR);
                        entityToPut.setSensor(true);
                        entityToPut.setDrawScale(scale);
                        entityToPut.activatePhysics(world);
                        break;
                    default:
                }
            } else {
                typeToPut = Entity.EntityType.TUTORIAL;
                entityToPut = new TutorialEntity(crosshair.x*scale.x,crosshair.y*scale.y, 0,
                        TutorialEntity.getTextureWidth(TutorialEntity.TutorialSubType.TEXT1)/scale.x,
                        TutorialEntity.getTextureHeight(TutorialEntity.TutorialSubType.TEXT1)/scale.y,
                        TutorialEntity.TutorialSubType.TEXT1);
                entityToPut.setSensor(true);
                entityToPut.setDrawScale(scale);
                entityToPut.activatePhysics(world);
            }
        }
        // rotate obstacle left
        else if (input.didRotateLeft() && typeToPut == Entity.EntityType.OBSTACLE) {
            ObstacleEntity obstacle = (ObstacleEntity)entityToPut;
            obstacle.getBody().setTransform(obstacle.getBody().getWorldCenter(), obstacle.getAngle()+(float)Math.PI/12);
        }
        else if (input.didRotateLeft() && typeToPut == Entity.EntityType.TUTORIAL) {
            TutorialEntity tutorialEntity = (TutorialEntity) entityToPut;
            tutorialEntity.getBody().setTransform(tutorialEntity.getBody().getWorldCenter(), tutorialEntity.getAngle()+(float)Math.PI/12);
        }
        // rotate obstacle right
        else if (input.didRotateRight() && typeToPut == Entity.EntityType.OBSTACLE) {
            ObstacleEntity obstacle = (ObstacleEntity)entityToPut;
            obstacle.getBody().setTransform(obstacle.getBody().getWorldCenter(), obstacle.getAngle()-(float)Math.PI/12);
        }
        else if (input.didRotateLeft() && typeToPut == Entity.EntityType.TUTORIAL) {
            TutorialEntity tutorialEntity = (TutorialEntity) entityToPut;
            tutorialEntity.getBody().setTransform(tutorialEntity.getBody().getWorldCenter(), tutorialEntity.getAngle()-(float)Math.PI/12);
        }
        // delete last vertex of selected wall
        else if (input.didSelectBackspace()) {
            if (walls.size() > 1) {
                walls.removeLast();
                walls.removeLast();
            }
            typeToPut = Entity.EntityType.WALL;
        }
        // delete selected wall
        else if (input.didSelectEscape()) {
            walls.clear();
            typeToPut = Entity.EntityType.NULL;
        }
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
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }
        if(input.didSaveLevel()){
            saveToModel(model);
            try {
                String name = JOptionPane.showInputDialog("Enter a name");
                LevelLoader.generateJson(model,name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(input.didLoadLevel()){
            loadLevelFlag = JOptionPane.showInputDialog("Enter a name");
        }
        if(input.didSetBounds()){
            String x = JOptionPane.showInputDialog("Enter a width. Must be greater than 32");
            String y = JOptionPane.showInputDialog("Enter a height. Must be greater than 18");
            bounds.setHeight(Integer.parseInt(y));
            bounds.setWidth(Integer.parseInt(x));
        }
        AudioController.getInstance().update();
    }


    /**
     * draws all the objects
     *
     * @param dt delta
     * @param backgroundTexture background texture
     * @param lineTexture texure of the line
     * @param debug are we in debug mode
     */
    public void draw(float dt, TextureRegion crosshairTexture, TextureRegion backgroundTexture, TextureRegion lineTexture, boolean debug){
        InputController input = InputController.getInstance();
        canvas.clear();

        canvas.begin();
        canvas.draw(backgroundTexture, Color.WHITE, 0, 0,bounds.getWidth()*scale.x,bounds.getHeight()*scale.y);
        canvas.end();


        //Draws the wall lines when building walls
        canvas.begin();
        for(int i = 2; i < walls.size(); i = i + 2) {
            Vector2 prev = new Vector2(walls.get(i-2), walls.get(i-1));
            Vector2 curr = new Vector2(walls.get(i), walls.get(i+1));
            Vector2 cache2 = new Vector2(prev.x - curr.x, prev.y - curr.y);
            Vector2 cache = new Vector2((prev.x + curr.x)/2, (prev.y + curr.y)/2);
            lineTexture.setRegionHeight((int)(cache2.len()*scale.y));
            canvas.draw(lineTexture, com.badlogic.gdx.graphics.Color.WHITE, lineTexture.getRegionWidth() / 2f,
                    lineTexture.getRegionHeight() / 2f, cache.x * scale.x, cache.y * scale.y, cache2.rotate90(1).angleRad(), 1, 1);
        }
        canvas.end();

        canvas.begin();
        for(Entity obj : objects) {
            if (obj.isActive() && obj.getEntityType() != Entity.EntityType.OCTOPUS &&
                    obj.getEntityType() != Entity.EntityType.ENEMY && obj.getEntityType() != Entity.EntityType.OBSTACLE) {
                obj.draw(canvas);
            }
        }
        canvas.end();
        BC.draw(dt,true,OC);
        OC.draw(dt,true);
        EC.draw(dt,true,false);
        canvas.begin();
        if(entityToPut != null) {
            entityToPut.draw(canvas);
        }
        canvas.end();
        // Debug rectangle drawing
        if (debug) {
            canvas.beginDebug();
            for(Entity obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }

        canvas.begin();
        canvas.draw(crosshairTexture, Color.WHITE, crosshairTexture.getRegionWidth() / 2f, crosshairTexture.getRegionHeight() / 2f, input.getMouse().x + canvas.getCameraMoveX(),
                input.getMouse().y + canvas.getCameraMoveY(), scale.x, scale.y);
        canvas.end();
    }

    /**
     * saves all of the placed objects to model
     */
    public void saveToModel(LevelModel model){
        if(entityToPut != null){
            entityToPut.setActive(false);
            entityToPut = null;
            typeToPut = Entity.EntityType.NULL;
        }
        model.walls.clear();
        model.wallTexture.clear();
        model.enemyPos.clear();
        model.octopusPos.clear();
        model.objectPos.clear();
        model.objectAngle.clear();
        model.OT.clear();
        model.ET.clear();
        model.octT.clear();
        model.TT.clear();
        model.tutorialPos.clear();
        model.tutorialAngle.clear();
        model.bounds = bounds;
        model.initCamPos = canvas.getCameraPosInScreen();
        for(Entity ent : objects){
            Vector2 posDescaled;
            switch(ent.getEntityType()){
                case WALL:
                    float[] wallsDescaled = new float[((WallEntity)ent).getOrigVertices().length];
                    //descale walls so that they can be rescaled for various screen sizes
                    int i = 0;
                    for(float f : ((WallEntity)ent).getOrigVertices()){
                        wallsDescaled[i] = i/2 == 0 ? f/scale.x : f/scale.y;
                        i++;
                    }
                    model.walls.add(wallsDescaled);
                    model.wallTexture.add(((WallEntity)ent).getWallTexture());
                    //TODO: fix wallTexture
                    break;
                case OBSTACLE:
                    model.OT.add(((ObstacleEntity)ent).getObstacleSubType());
                    //descale positions so they can be rescaled for various screen sizes
                    posDescaled = new Vector2(ent.getPosition().x/scale.x,ent.getPosition().y/scale.y);
                    model.objectPos.add(posDescaled);
                    model.objectAngle.add(ent.getAngle());
                    break;
                case OCTOPUS:
                    model.octT.add(((OctopusEntity)ent).getOctopusSubType());
                    //descale positions so they can be rescaled for various screen sizes
                    posDescaled = new Vector2(ent.getPosition().x/scale.x,ent.getPosition().y/scale.y);
                    model.octopusPos.add(posDescaled);
                    break;
                case ENEMY:
                    model.ET.add(((EnemyEntity)ent).getEnemySubType());
                    //descale positions so they can be rescaled for various screen sizes
                    posDescaled = new Vector2(ent.getPosition().x/scale.x,ent.getPosition().y/scale.y);
                    model.enemyPos.add(posDescaled);
                    break;
                case TUTORIAL:
                    model.TT.add(((TutorialEntity)ent).getTutorialSubType());
                    //descale positions so they can be rescaled for various screen sizes
                    posDescaled = new Vector2(ent.getPosition().x/scale.x,ent.getPosition().y/scale.y);
                    model.tutorialPos.add(posDescaled);
                    model.tutorialAngle.add(ent.getAngle());
                    break;
                default:
            }
        }
    }
}
