package edu.cornell.gdiac.octoplasm.util;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.octoplasm.entity.*;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * TODO: documentation
 *
 * @author Jarrett Coleman
 */
public class LevelModel {

    //=========================================================================
    //#region Fields
    /** The walls */
    public LinkedList<float[]> walls;
    /** The wall texture */
    public LinkedList<WallEntity.WallTexture> wallTexture;
    /** The list of obstacle object sub types */
    public LinkedList<ObstacleEntity.ObstacleSubType> OT;
    /** The list of obstacle positions */
    public LinkedList<Vector2> objectPos;
    /** The list of obstacle Angle */
    public LinkedList<Float> objectAngle;
    /** The list of enemy object subtypes */
    public LinkedList<EnemyEntity.EnemySubType> ET;
    /** The list of enemy positions */
    public LinkedList<Vector2> enemyPos;
    /** The list of octopus object subtypes */
    public LinkedList<OctopusEntity.OctopusSubType> octT;
    /** The list of octopus positions */
    public LinkedList<Vector2> octopusPos;
    /** The list of tutorial types */
    public LinkedList<TutorialEntity.TutorialSubType> TT;
    /**  */
    public LinkedList<Float> tutorialAngle;
    /** The list of tutorial positions */
    public LinkedList<Vector2> tutorialPos;
    /** The boundary of the level*/
    public Rectangle bounds;
    public Vector2 initCamPos;
    //#endregion
    //=================================

    //=========================================================================
    //#region Constructors
    /**
     * Creates a new level model with nothing in it.
     */
    public LevelModel() {
        this.walls = new LinkedList<>();
        this.wallTexture = new LinkedList<>();
        this.OT = new LinkedList<>();
        this.objectPos = new LinkedList<>();
        this.objectAngle = new LinkedList<>();
        this.ET = new LinkedList<>();
        this.enemyPos = new LinkedList<>();
        this.octT = new LinkedList<>();
        this.octopusPos = new LinkedList<>();
        this.TT = new LinkedList<>();
        this.tutorialPos = new LinkedList<>();
        this.tutorialAngle = new LinkedList<>();

        this.initCamPos = new Vector2();
    }

    /**
     * Creates a new level model filled with the provided values.
     *
     * @param walls
     * @param wallTexture
     * @param OT
     * @param objectPos
     * @param objectAngle
     * @param ET
     * @param enemyPos
     * @param octT
     * @param octopusPos
     */
    public LevelModel(LinkedList<float[]> walls, LinkedList<WallEntity.WallTexture> wallTexture, LinkedList<ObstacleEntity.ObstacleSubType> OT,
                      LinkedList<Vector2> objectPos,
                      LinkedList<Float> objectAngle,
                      LinkedList<EnemyEntity.EnemySubType> ET,
                      LinkedList<Vector2> enemyPos,
                      LinkedList<OctopusEntity.OctopusSubType> octT,
                      LinkedList<Vector2> octopusPos,
                      LinkedList<TutorialEntity.TutorialSubType> tutorialSubTypes,
                      LinkedList<Vector2> tutorialPos,
                      LinkedList<Float> tutorialAngle,
                      Rectangle bounds,
                      Vector2 initCamPos){
        this.wallTexture = wallTexture;
        this.walls = walls;
        this.OT = OT;
        this.objectPos = objectPos;
        this.objectAngle = objectAngle;
        this.ET = ET;
        this.enemyPos = enemyPos;
        this.octT = octT;
        this.octopusPos = octopusPos;
        this.bounds = bounds;
        this.initCamPos = initCamPos;

        this.TT = tutorialSubTypes;
        this.tutorialPos = tutorialPos;
        this.tutorialAngle = tutorialAngle;
    }
    //#endregion
    //=================================
}
