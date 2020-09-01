package com.example.solarsystemapp.model

import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.solarsystemapp.controller.TouchController

//This is the actual opengl view. From here we can detect touch gestures for example

class ModelSurfaceView(val modelActivity: ModelActivity) :
    GLSurfaceView(modelActivity) {
    val modelRenderer: ModelRenderer
    private val touchHandler: TouchController
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return touchHandler.onTouchEvent(event)
    }

    init {
        // parent component
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2)
        // This is the actual renderer of the 3D space
        modelRenderer = ModelRenderer(this)
        setRenderer(modelRenderer)
        // Render the view only when there is a change in the drawing data
// TODO: enable this?
// setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        touchHandler = TouchController(this, modelRenderer)
    }
}