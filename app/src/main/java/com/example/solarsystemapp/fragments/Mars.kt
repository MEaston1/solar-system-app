package com.example.solarsystemapp.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.solarsystemapp.R
import com.example.solarsystemapp.model.ModelActivity

class Mars : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //returning our layout file
        val view: View = inflater.inflate(R.layout.fragment_mars, container, false)
        val path = activity?.packageName
        val btn = view.findViewById<Button>(R.id.marsModelButton)
        val infoTxt = view.findViewById<TextView>(R.id.marsInfoText)
        val factsTxt = view.findViewById<TextView>(R.id.marsFactsText)
        btn.setOnClickListener {
            launchModelRendererActivity(Uri.parse("assets://$path/models/Mars.obj"))
        }
        infoTxt.movementMethod = ScrollingMovementMethod()
        factsTxt.movementMethod = ScrollingMovementMethod()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.title = "Mars"
    }

    private fun launchModelRendererActivity(uri: Uri) {
        Log.i("Menu", "Launching renderer for '$uri'")
        val intent = Intent(context, ModelActivity::class.java)
        intent.putExtra("uri", uri.toString())
        intent.putExtra("immersiveMode", "true")

        startActivity(intent)
    }
}