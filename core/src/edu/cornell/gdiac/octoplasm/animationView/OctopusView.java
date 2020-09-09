package edu.cornell.gdiac.octoplasm.animationView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.*;
import edu.cornell.gdiac.octoplasm.InputController;
import edu.cornell.gdiac.octoplasm.entity.BoxEntity;
import edu.cornell.gdiac.octoplasm.entity.EnemyEntity;
import edu.cornell.gdiac.octoplasm.entity.OctopusEntity;
import edu.cornell.gdiac.octoplasm.util.LevelModel;

import java.util.Dictionary;
import java.util.HashMap;

public class OctopusView extends CharacterView {

    private static final float ANIMATION_SCALE = 0.5f;
    ObjectMap<BoxEntity.State, InputController.StateView> octopusStates;
    OctopusEntity octopusEntity;
    Bone coreBone, headBone, armsBone, frontBone, front1Bone, front2Bone, front3Bone, front4Bone, front5Bone, backBone, back1Bone, back2Bone, back3Bone;
    Vector2 scale;

    public OctopusView(HashMap<OctopusEntity.OctopusSubType, SkeletonData> octopusSkeletonData,
                       HashMap<OctopusEntity.OctopusSubType, AnimationStateData> octopusAnimationStateData,
                       HashMap<OctopusEntity.OctopusSubType, ObjectMap<BoxEntity.State, InputController.StateView>> octopusStates,
                       OctopusEntity octopusEntity, Vector2 scale) {
        super();
        this.octopusStates = octopusStates.get(octopusEntity.getOctopusSubType());
        this.octopusEntity = octopusEntity;
        this.scale = scale;

        switch (octopusEntity.getOctopusSubType()) {
            case TELEPORTER:
                skeleton = new Skeleton(octopusSkeletonData.get(OctopusEntity.OctopusSubType.TELEPORTER));
                skeleton.setScaleX(scale.x);
                skeleton.setScaleY(scale.y);
                animationState = new AnimationState(octopusAnimationStateData.get(OctopusEntity.OctopusSubType.TELEPORTER));
                break;
            case EXPLODER:
                skeleton = new Skeleton(octopusSkeletonData.get(OctopusEntity.OctopusSubType.EXPLODER));
                skeleton.setScaleX(scale.x);
                skeleton.setScaleY(scale.y);
                animationState = new AnimationState(octopusAnimationStateData.get(OctopusEntity.OctopusSubType.EXPLODER));
                break;
            case FLYER:
                skeleton = new Skeleton(octopusSkeletonData.get(OctopusEntity.OctopusSubType.FLYER));
                skeleton.setScaleX(scale.x);
                skeleton.setScaleY(scale.y);
                animationState = new AnimationState(octopusAnimationStateData.get(OctopusEntity.OctopusSubType.FLYER));
                break;
            default:
                break;
        }
        // We'll allow any of the bones or animations to be null in case someone has swapped out spineboy for a different skeleton.
        coreBone = skeleton.findBone("core");
        headBone = skeleton.findBone("head");
        armsBone = skeleton.findBone("arms");
        frontBone = skeleton.findBone("front");
        front1Bone = skeleton.findBone("front1");
        front2Bone = skeleton.findBone("front2");
        front3Bone = skeleton.findBone("front3");
        front4Bone = skeleton.findBone("front4");
        front5Bone = skeleton.findBone("front5");
        backBone = skeleton.findBone("back");
        back1Bone = skeleton.findBone("back1");
        back2Bone = skeleton.findBone("back2");
        back3Bone = skeleton.findBone("back3");

        //Scale of the animation
        skeleton.getRootBone().setScale(ANIMATION_SCALE);
    }

    @Override
    public void update(float delta, boolean frozen) {
        float rotationAngle = octopusEntity.getAngle();
        skeleton.getRootBone().setRotation((float)Math.toDegrees(rotationAngle)%360);
        Vector2 size = new Vector2();
        skeleton.getBounds(new Vector2(),size,new FloatArray());
        skeleton.getRootBone().setScale(0.5f);
        skeleton.setX(octopusEntity.getPosition().x*scale.x);
        skeleton.setY(octopusEntity.getPosition().y*scale.y);
        if (!setAnimation(octopusStates.get(octopusEntity.state), octopusEntity.stateChanged) && (!frozen || octopusEntity.getState() == BoxEntity.State.win)) {
            animationState.update(delta);
            if (octopusEntity.state== BoxEntity.State.death && octopusEntity.getOctopusSubType()== OctopusEntity.OctopusSubType.EXPLODER) {
                skeleton.getRootBone().setScale(1);
            }
//            if (octopusEntity.state== BoxEntity.State.death) System.out.println("Updating death state animation");
//            if (octopusEntity.state== BoxEntity.State.move) System.out.println("Updating move state animation");
//            if (octopusEntity.state== BoxEntity.State.ability) System.out.println("Updating ability state animation");
//            if (octopusEntity.state== BoxEntity.State.idle) System.out.println("Updating idle state animation");
        } else {
            // If successfully set animation and the octopus entity is being revived, then set it back to false to mark the end of respawn
            if (octopusEntity.isRespawned) {
                octopusEntity.isRespawned = false;
            }
        }
        animationState.apply(skeleton);

        skeleton.updateWorldTransform();
    }
}
