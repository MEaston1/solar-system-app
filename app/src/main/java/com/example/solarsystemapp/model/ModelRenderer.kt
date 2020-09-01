package com.example.solarsystemapp.model

import android.opengl.GLES20
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.engine.solar_system_app_engine.animation.Animator
import com.example.engine.solar_system_app_engine.drawer.DrawerFactory
import com.example.engine.solar_system_app_engine.model.AnimatedModel
import com.example.engine.solar_system_app_engine.model.Camera
import com.example.engine.solar_system_app_engine.model.Object3D
import com.example.engine.solar_system_app_engine.model.Object3DData
import com.example.engine.solar_system_app_engine.services.Object3DBuilder
import com.example.engine.util.android.GLUtil
import com.example.solarsystemapp.R

import com.example.solarsystemapp.scene.SceneLoader

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class ModelRenderer(// 3D window (parent component)
    private var main: ModelSurfaceView) :
    GLSurfaceView.Renderer {
    // width of the screen
    var width = 0
        private set
    // height of the screen
    var height = 0
        private set
    /**
     * Drawer factory to get right renderer/shader based on object attributes
     */
    private var drawer: DrawerFactory
    /**
     * 3D Axis (to show if needed)
     */
    private val axis: Object3DData = Object3DBuilder.buildAxis().setId("axis")
    // The wireframe associated shape (it should be made of lines only)
    private val wireframes: MutableMap<Object3DData?, Object3DData?> =
        HashMap<Object3DData?, Object3DData?>()
    // The loaded textures
    private val textures: MutableMap<Any, Int?> =
        HashMap()
    // The corresponding opengl bounding boxes and drawer
    private val boundingBoxes: MutableMap<Object3DData?, Object3DData?> =
        HashMap<Object3DData?, Object3DData?>()
    // The corresponding opengl bounding boxes
    private val normals: MutableMap<Object3DData?, Object3DData?> =
        HashMap<Object3DData?, Object3DData?>()
    private val skeleton: MutableMap<Object3DData?, Object3DData?> =
        HashMap<Object3DData?, Object3DData?>()
    // 3D matrices to project our 3D world
    private val viewMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    val modelProjectionMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val lightPosInEyeSpace = FloatArray(4)
    // 3D stereoscopic matrix (left & right camera)
    private val viewMatrixLeft = FloatArray(16)
    private val projectionMatrixLeft = FloatArray(16)
    private val viewProjectionMatrixLeft = FloatArray(16)
    private val viewMatrixRight = FloatArray(16)
    private val projectionMatrixRight = FloatArray(16)
    private val viewProjectionMatrixRight = FloatArray(16)
    /**
     * Whether the info of the model has been written to console log
     */
    private val infoLogged: MutableMap<Object3DData, Boolean?> =
        HashMap<Object3DData, Boolean?>()
    /**
     * Switch to akternate drawing of right and left image
     */
    private var anaglyphSwitch = false
    /**
     * Skeleton Animator
     */
    private val animator: Animator = Animator()
    /**
     * Did the application explode?
     */
    private var fatalException = false

    @Throws(IllegalAccessException::class, IOException::class)
    fun ModelRenderer(modelSurfaceView: ModelSurfaceView) {
        main = modelSurfaceView
        // This component will draw the actual models using OpenGL
        drawer = DrawerFactory(modelSurfaceView.context)
    }

    fun getNear(): Float {
        return near
    }

    fun getFar(): Float {
        return far
    }

    override fun onSurfaceCreated(
        unused: GL10,
        config: EGLConfig
    ) { // Set the background frame color
        val backgroundColor: FloatArray = main.modelActivity.backgroundColor
        GLES20.glClearColor(
            backgroundColor[0],
            backgroundColor[1],
            backgroundColor[2],
            backgroundColor[3]
        )

        // Use culling to remove back faces.
// Don't remove back faces so we can see them
// GLES20.glEnable(GLES20.GL_CULL_FACE);
// Enable depth testing for hidden-surface elimination.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        // Enable not drawing out of view port
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        this.width = width
        this.height = height
        // Adjust the viewport based on geometry changes, such as screen rotation
        GLES20.glViewport(0, 0, width, height)
        // the projection matrix is the 3D virtual space (cube) that we want to project
        val ratio = width.toFloat() / height
        Log.d(
            TAG,
            "projection: [" + -ratio + "," + ratio + ",-1,1]-near/far[1,10]"
        )
        Matrix.frustumM(
            modelProjectionMatrix,
            0,
            -ratio,
            ratio,
            -1f,
            1f,
            near,
            far
        )
        Matrix.frustumM(
            projectionMatrixRight,
            0,
            -ratio,
            ratio,
            -1f,
            1f,
            near,
            far
        )
        Matrix.frustumM(
            projectionMatrixLeft,
            0,
            -ratio,
            ratio,
            -1f,
            1f,
            near,
            far
        )
    }

    override fun onDrawFrame(unused: GL10) {
        if (fatalException) {
            return
        }
        try {
            GLES20.glViewport(0, 0, width, height)
            GLES20.glScissor(0, 0, width, height)
            // Draw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            val scene: SceneLoader = main.modelActivity.scene
                ?: // scene not ready
                return
            if (scene.isBlendingEnabled) { // Enable blending for combining colors when there is transparency
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            } else {
                GLES20.glDisable(GLES20.GL_BLEND)
            }
            // animate scene
            scene.onDrawFrame()
            // recalculate mvp matrix according to where we are looking at now
            val camera: Camera? = scene.getCamera()
            if (camera != null) {
                if (camera.hasChanged()) { // INFO: Set the camera position (View matrix)
        // The camera has 3 vectors (the position, the vector where we are looking at, and the up position (sky)
        // the projection matrix is the 3D virtual space (cube) that we want to project
                    val ratio = width.toFloat() / height
                    // Log.v(TAG, "Camera changed: projection: [" + -ratio + "," + ratio + ",-1,1]-near/far[1,10], ");
                    if (!scene.isStereoscopic) {
                        Matrix.setLookAtM(
                            viewMatrix,
                            0,
                            camera.xPos,
                            camera.yPos,
                            camera.zPos,
                            camera.xView,
                            camera.yView,
                            camera.zView,
                            camera.xUp,
                            camera.yUp,
                            camera.zUp
                        )
                        Matrix.multiplyMM(
                            viewProjectionMatrix,
                            0,
                            modelProjectionMatrix,
                            0,
                            viewMatrix,
                            0
                        )
                    } else {
                        val stereoCamera: Array<Camera> =
                            camera.toStereo(EYE_DISTANCE)
                        val leftCamera: Camera = stereoCamera[0]
                        val rightCamera: Camera = stereoCamera[1]
                        // camera on the left for the left eye
                        Matrix.setLookAtM(
                            viewMatrixLeft,
                            0,
                            leftCamera.xPos,
                            leftCamera.yPos,
                            leftCamera.zPos,
                            leftCamera.xView,
                            leftCamera.yView,
                            leftCamera.zView,
                            leftCamera.xUp,
                            leftCamera.yUp,
                            leftCamera.zUp
                        )
                        // camera on the right for the right eye
                        Matrix.setLookAtM(
                            viewMatrixRight,
                            0,
                            rightCamera.xPos,
                            rightCamera.yPos,
                            rightCamera.zPos,
                            rightCamera.xView,
                            rightCamera.yView,
                            rightCamera.zView,
                            rightCamera.xUp,
                            rightCamera.yUp,
                            rightCamera.zUp
                        )

                        // Calculate the projection and view transformation
                        Matrix.multiplyMM(
                            viewProjectionMatrixLeft,
                            0,
                            projectionMatrixLeft,
                            0,
                            viewMatrixLeft,
                            0
                        )
                        Matrix.multiplyMM(
                            viewProjectionMatrixRight,
                            0,
                            projectionMatrixRight,
                            0,
                            viewMatrixRight,
                            0
                        )
                    }
                    camera.setChanged(false)
                }
            }
            if (!scene.isStereoscopic) {
                this.onDrawFrame(
                    viewMatrix,
                    modelProjectionMatrix,
                    viewProjectionMatrix,
                    lightPosInEyeSpace,
                    null
                )
                return
            }
        } catch (ex: Exception) {
            Log.e("ModelRenderer", "Fatal exception: " + ex.message, ex)
            fatalException = true
        }
    }

    private fun onDrawFrame(
        viewMatrix: FloatArray,
        projectionMatrix: FloatArray,
        viewProjectionMatrix: FloatArray,
        lightPosInEyeSpace: FloatArray,
        colorMask: FloatArray?
    ) {
        val scene: SceneLoader? = main.modelActivity.scene
        // draw light
        if (scene != null) {
            if (scene.isDrawLighting) {
                val lightBulbDrawer: Object3D = drawer.pointDrawer
                Matrix.multiplyMM(
                    modelViewMatrix,
                    0,
                    viewMatrix,
                    0,
                    scene.lightBulb.modelMatrix,
                    0
                )
                // Calculate position of the light in eye space to support lighting
                Matrix.multiplyMV(
                    lightPosInEyeSpace,
                    0,
                    modelViewMatrix,
                    0,
                    scene.lightPosition,
                    0
                )
                // Draw a point that represents the light bulb
                lightBulbDrawer.draw(
                    scene.lightBulb, projectionMatrix, viewMatrix, -1, lightPosInEyeSpace,
                    colorMask
                )
            }
        }
        // draw axis
        if (scene != null) {
            if (scene.isDrawAxis) {
                val basicDrawer: Object3D = drawer.getPointDrawer()
                basicDrawer.draw(
                    axis, projectionMatrix, viewMatrix, axis.getDrawMode(), axis
                        .getDrawSize(), -1, lightPosInEyeSpace, colorMask
                )
            }
        }
        // is there any object?
        if (scene != null) {
            if (scene.getObjects().isEmpty()) {
                return
            }
        }
        // draw all available objects
        val objects: List<Object3DData> = scene!!.getObjects()
        for (i in objects.indices) {
            var objData: Object3DData? = null
            try {
                objData = objects[i]
                if (!objData.isVisible) continue
                var drawerObject: Object3D = drawer.getDrawer(
                    objData, scene!!.isDrawTextures, scene.isDrawLighting,
                    scene.isDoAnimation, scene.isDrawColors
                )
                    ?: continue
                if (!infoLogged.containsKey(objData)) {
                    Log.v("ModelRenderer", "Drawing model: " + objData.getId())
                    infoLogged[objData] = true
                }
                val changed: Boolean = objData.isChanged
                // load model texture
                var textureId = textures[objData.textureData]
                if (textureId == null && objData.textureData != null) { //Log.i("ModelRenderer","Loading texture '"+objData.getTextureFile()+"'...");
                    val textureIs =
                        ByteArrayInputStream(objData.textureData)
                    textureId = GLUtil.loadTexture(textureIs)
                    textureIs.close()
                    textures[objData.textureData] = textureId
                    //Log.i("GLUtil", "Loaded texture ok");
                }
                if (textureId == null) {
                    textureId = -1
                }
                // draw points
                if (objData.getDrawMode() === GLES20.GL_POINTS) {
                    val basicDrawer: Object3D = drawer.getPointDrawer()
                    basicDrawer.draw(
                        objData,
                        projectionMatrix,
                        viewMatrix,
                        GLES20.GL_POINTS,
                        lightPosInEyeSpace
                    )
                } else if (scene.isDrawWireframe && objData.getDrawMode() !== GLES20.GL_POINTS && objData.getDrawMode() !== GLES20.GL_LINES && objData.getDrawMode() !== GLES20.GL_LINE_STRIP && objData.getDrawMode() !== GLES20.GL_LINE_LOOP
                ) { // Log.d("ModelRenderer","Drawing wireframe model...");
                    try { // Only draw wireframes for objects having faces (triangles)
                        var wireframe: Object3DData? = wireframes[objData]
                        if (wireframe == null || changed) {
                            Log.i("ModelRenderer", "Generating wireframe model...")
                            wireframe = Object3DBuilder.buildWireframe(objData)
                            wireframes[objData] = wireframe
                        }
                        drawerObject.draw(
                            wireframe, projectionMatrix, viewMatrix, wireframe!!.getDrawMode(),
                            wireframe!!.getDrawSize(), textureId, lightPosInEyeSpace,
                            colorMask
                        )
                        animator.update(wireframe, scene.isShowBindPose)
                    } catch (e: Error) {
                        Log.e("ModelRenderer", e.message, e)
                    }
                } else if (scene.isDrawPoints || objData.getFaces() == null || !objData.getFaces().loaded()) {
                    drawerObject.draw(
                        objData, projectionMatrix, viewMatrix
                        , GLES20.GL_POINTS, objData.getDrawSize(),
                        textureId, lightPosInEyeSpace, colorMask
                    )
                } else if (scene.isDrawSkeleton && objData is AnimatedModel && (objData as AnimatedModel)
                        .getAnimation() != null
                ) {
                    var skeleton: Object3DData? = skeleton[objData]
                    if (skeleton == null) {
                        skeleton = Object3DBuilder.buildSkeleton(objData as AnimatedModel?)
                        this.skeleton[objData] = skeleton
                    }
                    animator.update(skeleton, scene.isShowBindPose)
                    drawerObject = drawer.getDrawer(
                        skeleton, false, scene.isDrawLighting, scene
                            .isDoAnimation, scene.isDrawColors
                    )
                    drawerObject.draw(
                        skeleton,
                        projectionMatrix,
                        viewMatrix,
                        -1,
                        lightPosInEyeSpace,
                        colorMask
                    )
                } else {
                    drawerObject.draw(
                        objData, projectionMatrix, viewMatrix,
                        textureId, lightPosInEyeSpace, colorMask
                    )
                }

                // TODO: enable this only when user wants it
// obj3D.drawVectorNormals(result, viewMatrix);
            } catch (ex: Exception) {
                Log.e(
                    "ModelRenderer",
                    "There was a problem rendering the object '" + objData!!.getId().toString() + "':" + ex.message,
                    ex
                )
            }
        }
    }

    fun getModelViewMatrix(): FloatArray {
        return viewMatrix
    }

    companion object {
        private val TAG = ModelRenderer::class.java.name
        // frustrum - nearest pixel
        private const val near = 1f
        // frustrum - fartest pixel
        private const val far = 100f
        // stereoscopic variables
        private const val EYE_DISTANCE = 0.64f
        private val COLOR_RED = floatArrayOf(1.0f, 0.0f, 0.0f, 1f)
        private val COLOR_BLUE = floatArrayOf(0.0f, 1.0f, 0.0f, 1f)
    }

    /**
     * Construct a new renderer for the specified surface view
     *
     * @param modelSurfaceView
     * the 3D window
     */
    init {
        // This component will draw the actual models using OpenGL
        drawer = DrawerFactory(main.context)
    }

}