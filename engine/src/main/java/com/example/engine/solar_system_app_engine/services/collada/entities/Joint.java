package com.example.engine.solar_system_app_engine.services.collada.entities;

import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Joint {

    private final JointData data;

    // descendants
    private final List<Joint> children = new ArrayList<>();

    // this is the animated final matrix used when drawing in opengl
    private final float[] animatedTransform = new float[16];

    /**
     * This is called during set-up, after the joints hierarchy has been
     * created. This calculates the model-space bind transform of this joint
     * like so: </br>
     * </br>
     * {@code bindTransform = parentBindTransform * bindLocalTransform}</br>
     * </br>
     * where "bindTransform" is the model-space bind transform of this joint,
     * "parentBindTransform" is the model-space bind transform of the parent
     * joint, and "bindLocalTransform" is the bone-space bind transform of this
     * joint. It then calculates and stores the inverse of this model-space bind
     * transform, for use when calculating the final animation transform each
     * frame. It then recursively calls the method for all of the children
     * joints, so that they too calculate and store their inverse bind-pose
     * transform.
     */
    public Joint(JointData data) {
        this.data = data;
        Matrix.setIdentityM(animatedTransform,0);
    }

    public int getIndex() {
        return data.index;
    }

    public String getName() {
        return data.getId();
    }

    public List<Joint> getChildren() {
        return children;
    }

    // FIXME: why is not this used?
    public float[] getBindTransform() {
        return data.getBindTransform();
    }

    public float[] getBindLocalTransform() {
        return data.getBindLocalTransform();
    }

    /**
     * Adds a child joint to this joint. Used during the creation of the joint
     * hierarchy. Joints can have multiple children, which is why they are
     * stored in a list (e.g. a "hand" joint may have multiple "finger" children
     * joints).
     *
     * @param child - the new child joint of this joint.
     */
    public void addChild(Joint child) {
        this.children.add(child);
    }

    /**
     * The animated transform is the transform that gets loaded up to the shader
     * and is used to deform the vertices of the "skin". It represents the
     * transformation from the joint's bind position (original position in
     * model-space) to the joint's desired animation pose (also in model-space).
     * This matrix is calculated by taking the desired model-space transform of
     * the joint and multiplying it by the inverse of the starting model-space
     * transform of the joint.
     *
     * @return The transformation matrix of the joint which is used to deform
     * associated vertices of the skin in the shaders.
     */
    public float[] getAnimatedTransform() {
        return animatedTransform;
    }

    /**
     * This returns the inverted model-space bind transform. The bind transform
     * is the original model-space transform of the joint (when no animation is
     * applied). This returns the inverse of that, which is used to calculate
     * the animated transform matrix which gets used to transform vertices in
     * the shader.
     *
     * @return The inverse of the joint's bind transform (in model-space).
     */
    public float[] getInverseBindTransform() {
        return data.getInverseBindTransform();
    }

    /**
     * This is called during set-up, after the joints hierarchy has been
     * created. This calculates the model-space bind transform of this joint
     * like so: </br>
     * </br>
     * {@code bindTransform = parentBindTransform * bindLocalTransform}</br>
     * </br>
     * where "bindTransform" is the model-space bind transform of this joint,
     * "parentBindTransform" is the model-space bind transform of the parent
     * joint, and "bindLocalTransform" is the bone-space bind transform of this
     * joint. It then calculates and stores the inverse of this model-space bind
     * transform, for use when calculating the final animation transform each
     * frame. It then recursively calls the method for all of the children
     * joints, so that they too calculate and store their inverse bind-pose
     * transform.
     *
     * @param parentBindTransform - the model-space bind transform of the parent joint.
     */
    public void calcInverseBindTransform(float[] parentBindTransform, boolean override) {

        float[] bindTransform = new float[16];
        Matrix.multiplyMM(bindTransform, 0, parentBindTransform, 0, data.getBindLocalTransform(), 0);
        if (data.index >= 0 && (override)) {
            // when model has inverse bind transforms available, don't overwrite it
            // this way we calculate only the joints with no animations which has no inverse bind transform available
            float[] inverseBindTransform = new float[16];
            if (!Matrix.invertM(inverseBindTransform, 0, bindTransform, 0)) {
                Log.w("Joint", "Couldn't calculate inverse matrix for " + data.getId());
            }
            data.setInverseBindTransform(inverseBindTransform);
        }
        for (Joint child : children) {
            child.calcInverseBindTransform(bindTransform, override);
        }
    }

    @Override
    public Joint clone() {
        final Joint ret = new Joint(data);
        for (final Joint child : this.children){
            ret.addChild(child.clone());
        }
        return ret;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    public JointData find(String id) {
        return data.find(id);
    }
}
