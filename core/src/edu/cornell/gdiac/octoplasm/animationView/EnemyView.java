package edu.cornell.gdiac.octoplasm.animationView;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.*;
import edu.cornell.gdiac.octoplasm.InputController;
import edu.cornell.gdiac.octoplasm.entity.BoxEntity;
import edu.cornell.gdiac.octoplasm.entity.EnemyEntity;
import edu.cornell.gdiac.octoplasm.entity.EnemyEntity;

import java.util.Dictionary;
import java.util.HashMap;

public class EnemyView extends CharacterView {

    private static final float ANIMATION_SCALE = 0.5f;
    ObjectMap<BoxEntity.State, InputController.StateView> enemyStates;
    EnemyEntity enemyEntity;
    Bone coreBone, headBone, armsBone, frontBone, front1Bone, front2Bone, front3Bone, front4Bone, front5Bone, backBone, back1Bone, back2Bone, back3Bone;
    Vector2 temp1 = new Vector2(), temp2 = new Vector2();
    Vector2 scale;

    public EnemyView(HashMap<EnemyEntity.EnemySubType, SkeletonData> enemySkeletonData,
                     HashMap<EnemyEntity.EnemySubType, AnimationStateData> enemyAnimationStateData,
                     HashMap<EnemyEntity.EnemySubType, ObjectMap<BoxEntity.State, InputController.StateView>> enemyStates,
                     EnemyEntity enemyEntity, Vector2 scale) {
        super();
        this.enemyStates = enemyStates.get(enemyEntity.getEnemySubType());
        this.enemyEntity = enemyEntity;
        this.scale = scale;

        switch (enemyEntity.getEnemySubType()) {
            case NORMAL_ENEMY:
                skeleton = new Skeleton(enemySkeletonData.get(EnemyEntity.EnemySubType.NORMAL_ENEMY));
                skeleton.setScaleX(scale.x);
                skeleton.setScaleY(scale.y);
                animationState = new AnimationState(enemyAnimationStateData.get(EnemyEntity.EnemySubType.NORMAL_ENEMY));
                break;
            case ARMORED_ENEMY:
                skeleton = new Skeleton(enemySkeletonData.get(EnemyEntity.EnemySubType.ARMORED_ENEMY));
                skeleton.setScaleX(scale.x);
                skeleton.setScaleY(scale.y);
                animationState = new AnimationState(enemyAnimationStateData.get(EnemyEntity.EnemySubType.ARMORED_ENEMY));
                break;
            case SPIKED_ENEMY:
                skeleton = new Skeleton(enemySkeletonData.get(EnemyEntity.EnemySubType.SPIKED_ENEMY));
                skeleton.setScaleX(scale.x);
                skeleton.setScaleY(scale.y);
                animationState = new AnimationState(enemyAnimationStateData.get(EnemyEntity.EnemySubType.SPIKED_ENEMY));
                break;
            case HOLE_ENEMY:
                skeleton = new Skeleton(enemySkeletonData.get(EnemyEntity.EnemySubType.HOLE_ENEMY));
                skeleton.setScaleX(scale.x);
                skeleton.setScaleY(scale.y);
                animationState = new AnimationState(enemyAnimationStateData.get(EnemyEntity.EnemySubType.HOLE_ENEMY));
                break;
            case INVINCIBLE_ENEMY:
                skeleton = new Skeleton(enemySkeletonData.get(EnemyEntity.EnemySubType.INVINCIBLE_ENEMY));
                skeleton.setScaleX(scale.x);
                skeleton.setScaleY(scale.y);
                animationState = new AnimationState(enemyAnimationStateData.get(EnemyEntity.EnemySubType.INVINCIBLE_ENEMY));
                break;
            default:
                break;
        }

        //Scale of the animation
        skeleton.getRootBone().setScale(ANIMATION_SCALE);
    }

    @Override
    public void update(float delta, boolean frozen) {
        float rotationAngle = enemyEntity.getAngle();
        skeleton.getRootBone().setRotation((float)Math.toDegrees(rotationAngle)%360);
        Vector2 size = new Vector2();
        skeleton.getBounds(new Vector2(),size,new FloatArray());
        skeleton.getRootBone().setScale(0.5f);
        skeleton.setX(enemyEntity.getPosition().x*scale.x);
        skeleton.setY(enemyEntity.getPosition().y*scale.y);
//        Vector2 size = new Vector2();
//        skeleton.getBounds(new Vector2(),size,new FloatArray());
//        float dx = (float) Math.sin(rotationAngle)*size.y/2;
//        float dy = (float) (size.y/2*(1-Math.cos(rotationAngle)));
//        skeleton.getRootBone().setScale(0.5f);
////        skeleton.setX(enemyEntity.getPosition().x*scale.x);
////        skeleton.setY(enemyEntity.getPosition().y*scale.y);
//        skeleton.setX((enemyEntity.getPosition().x)*scale.x+dx);
//        skeleton.setY((enemyEntity.getPosition().y)*scale.y-size.y/2+dy);

        if (!setAnimation(enemyStates.get(enemyEntity.state), enemyEntity.stateChanged) && !frozen) {
            animationState.update(delta);
        }
        animationState.apply(skeleton);

        skeleton.updateWorldTransform();
    }
}
