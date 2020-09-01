package com.example.solarsystemapp.scene

import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.example.engine.solar_system_app_engine.animation.Animator
import com.example.engine.solar_system_app_engine.collision.CollisionDetection
import com.example.engine.solar_system_app_engine.model.Camera
import com.example.engine.solar_system_app_engine.model.Object3DData
import com.example.engine.solar_system_app_engine.services.LoaderTask
import com.example.engine.solar_system_app_engine.services.Object3DBuilder
import com.example.engine.solar_system_app_engine.services.collada.ColladaLoaderTask
import com.example.engine.solar_system_app_engine.services.stl.STLLoaderTask
import com.example.engine.solar_system_app_engine.services.wavefront.WavefrontLoaderTask
import com.example.engine.util.android.ContentUtils
import com.example.engine.util.io.IOUtils

import com.example.solarsystemapp.model.ModelActivity
import com.example.solarsystemapp.model.ModelRenderer


import java.io.IOException
import java.util.*

class SceneLoader(
    /**
     * Parent component
     */
    protected val parent: ModelActivity
) : LoaderTask.Callback {
    /**
     * List of data objects containing info for building the opengl objects
     */
    private var objects: List<Object3DData> = ArrayList<Object3DData>()
    /**
     * Show axis or not
     */
    var isDrawAxis = false
    /**
     * Point of view camera
     */
    private var camera: Camera? = null
    /**
     * Enable or disable blending (transparency)
     */
    var isBlendingEnabled = true
        private set
    /**
     * Force transparency
     */
    var isBlendingForced = false
        private set
    /**
     * Whether to draw objects as wireframes
     */
    var isDrawWireframe = false
        private set
    /**
     * Whether to draw using points
     */
    var isDrawPoints = false
        private set
    /**
     * Whether to draw bounding boxes around objects
     */
    var isDrawBoundingBox = false
        private set
    /**
     * Whether to draw face normals. Normally used to debug models
     */
// TODO: toggle feature this
    val isDrawNormals = false
    /**
     * Whether to draw using textures
     */
    var isDrawTextures = true
        private set
    /**
     * Whether to draw using colors or use default white color
     */
    var isDrawColors = true
        private set
    /**
     * Light toggle feature: we have 3 states: no light, light, light + rotation
     */
    var isRotatingLight = true
        private set
    /**
     * Light toggle feature: whether to draw using lights
     */
    var isDrawLighting = false
        private set
    /**
     * Animate model (dae only) or not
     */
    var isDoAnimation = true
        private set
    /**
     * show bind pose only
     */
    var isShowBindPose = false
        private set
    /**
     * Draw skeleton or not
     */
    var isDrawSkeleton = false
        private set
    /**
     * Toggle collision detection
     */
    var isCollision = false
        private set
    /**
     * Toggle 3d
     */
    var isStereoscopic = false
        private set
    /**
     * Toggle 3d anaglyph (red, blue glasses)
     */
    var isAnaglyph = false
        private set
    /**
     * Toggle 3d VR glasses
     */
    var isVRGlasses = false
        private set
    /**
     * Object selected by the user
     */
    var selectedObject: Object3DData? = null
        private set
    /**
     * Initial light position
     */
    val lightPosition = floatArrayOf(0f, 0f, 6f, 1f)
    /**
     * Light bulb 3d data
     */
    private val lightPoint: Object3DData = Object3DBuilder.buildPoint(lightPosition).setId("light")
    /**
     * Animator
     */
    private val animator: Animator = Animator()
    /**
     * Did the user touched the model for the first time?
     */
    private var userHasInteracted = false
    /**
     * time when model loading has started (for stats)
     */
    private var startTime: Long = 0

    fun init() { // Camera to show a point of view
        camera = Camera()
        camera!!.setChanged(true) // force first draw
        if (parent.paramUri == null) {
            return
        }
        startTime = SystemClock.uptimeMillis()
        val uri: Uri = parent.paramUri!!
        Log.i("Object3DBuilder", "Loading model $uri. async and parallel..")
        if (uri.toString().toLowerCase().endsWith(".obj") || parent.paramType === 0) {
            WavefrontLoaderTask(parent, uri, this).execute()
        } else if (uri.toString().toLowerCase().endsWith(".stl") || parent.paramType === 1) {
            Log.i("Object3DBuilder", "Loading STL object from: $uri")
            STLLoaderTask(parent, uri, this).execute()
        } else if (uri.toString().toLowerCase().endsWith(".dae") || parent.paramType === 2) {
            Log.i("Object3DBuilder", "Loading Collada object from: $uri")
            ColladaLoaderTask(parent, uri, this).execute()
        }
    }

    fun getCamera(): Camera? {
        return camera
    }

    private fun makeToastText(text: String, toastDuration: Int) {
        parent.runOnUiThread {
            Toast.makeText(parent.applicationContext, text, toastDuration).show()
        }
    }

    val lightBulb: Object3DData
        get() = lightPoint

    /**
     * Hook for animating the objects before the rendering
     */
    fun onDrawFrame() {
        animateLight()
        // smooth camera transition
        camera!!.animate()
        // initial camera animation. animate if user didn't touch the screen
        if (!userHasInteracted) {
            animateCamera()
        }
        if (objects.isEmpty()) return
        if (isDoAnimation) {
            for (i in objects.indices) {
                val obj: Object3DData = objects[i]
                animator.update(obj, isShowBindPose)
            }
        }
    }

    private fun animateLight() {
        if (!isRotatingLight) return
        // animate light - Do a complete rotation every 5 seconds.
        val time = SystemClock.uptimeMillis() % 5000L
        val angleInDegrees = 360.0f / 5000.0f * time.toInt()
        lightPoint.setRotationY(angleInDegrees)
    }

    private fun animateCamera() {
        camera!!.translateCamera(0.0025f, 0f)
    }

    @Synchronized
    fun addObject(obj: Object3DData) {
        val newList: MutableList<Object3DData> =
            ArrayList<Object3DData>(objects)
        newList.add(obj)
        objects = newList
        requestRender()
    }

    private fun requestRender() { // request render only if GL view is already initialized
        if (parent.gLView != null) {
            parent.gLView!!.requestRender()
        }
    }

    @Synchronized
    fun getObjects(): List<Object3DData> {
        return objects
    }

    override fun onStart() {
        ContentUtils.setThreadActivity(parent)
    }

    override fun onLoadComplete(datas: List<Object3DData>) { // TODO: move texture load to LoaderTask
        for (data in datas) {
            if (data.getTextureData() == null && data.getTextureFile() != null) {
                Log.i("LoaderTask", "Loading texture... " + data.getTextureFile())
                try {
                    ContentUtils.getInputStream(data.getTextureFile()).use({ stream ->
                        if (stream != null) {
                            data.setTextureData(IOUtils.read(stream))
                        }
                    })
                } catch (ex: IOException) {
                    data.addError("Problem loading texture " + data.getTextureFile())
                }
            }
        }
        // TODO: move error alert to LoaderTask
        val allErrors: MutableList<String> =
            ArrayList()
        for (data in datas) {
            addObject(data)
            allErrors.addAll(data.getErrors())
        }
        if (!allErrors.isEmpty()) {
            makeToastText(allErrors.toString(), Toast.LENGTH_LONG)
        }
        val elapsed: String =
            ((SystemClock.uptimeMillis() - startTime) / 1000).toString() + " secs"
        makeToastText("Build complete ($elapsed)", Toast.LENGTH_LONG)
        ContentUtils.setThreadActivity(null)
    }

    override fun onLoadError(ex: Exception) {
        Log.e("SceneLoader", ex.message, ex)
        makeToastText("There was a problem building the model: " + ex.message, Toast.LENGTH_LONG)
        ContentUtils.setThreadActivity(null)
    }

    @Throws(IOException::class)
    fun loadTexture(obj: Object3DData?, uri: Uri?) {
        var obj: Object3DData? = obj
        if (obj == null && objects.size != 1) {
            makeToastText("Unavailable", Toast.LENGTH_SHORT)
            return
        }
        obj = if (obj != null) obj else objects[0]
        obj.setTextureData(IOUtils.read(ContentUtils.getInputStream(uri)))
        isDrawTextures = true
    }

    fun processTouch(x: Float, y: Float) {
        val mr: ModelRenderer = parent.gLView!!.modelRenderer
        val objectToSelect = CollisionDetection.getBoxIntersection(
            getObjects(),
            mr.width,
            mr.height,
            mr.getModelViewMatrix(),
            mr.modelProjectionMatrix,
            x,
            y
        )
        if (objectToSelect != null) {
            if (selectedObject === objectToSelect) {
                Log.i("SceneLoader", "Unselected object " + objectToSelect.id)
                selectedObject =null
            } else {
                Log.i("SceneLoader", "Selected object " + objectToSelect.id)
                selectedObject = objectToSelect
            }
            if (isCollision) {
                Log.d("SceneLoader", "Detecting collision...")
                val point = CollisionDetection.getTriangleIntersection(
                    getObjects(),
                    mr.width,
                    mr.height,
                    mr.getModelViewMatrix(),
                    mr.modelProjectionMatrix,
                    x,
                    y
                )
                if (point != null) {
                    Log.i(
                        "SceneLoader",
                        "Drawing intersection point: " + Arrays.toString(point)
                    )
                    addObject(
                        Object3DBuilder.buildPoint(point).setColor(
                            floatArrayOf(
                                1.0f,
                                0f,
                                0f,
                                1f
                            )
                        )
                    )
                }
            }
        }
    }

    fun processMove(dx1: Float, dy1: Float) {
        userHasInteracted = true
    }

    companion object {
        /**
         * Default model color: yellow
         */
        private val DEFAULT_COLOR = floatArrayOf(1.0f, 1.0f, 0f, 1.0f)
    }

}