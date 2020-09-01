package com.example.solarsystemapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.solarsystemapp.QuizActivity
import com.example.solarsystemapp.R

class Home : Fragment() {
    lateinit var quizButton: Button
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //returning our layout file
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)
        quizButton = view.findViewById(R.id.quizButton)
        quizButton.setOnClickListener {
            startActivity(Intent(context, QuizActivity::class.java))
        }
        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.title = "Home"
    }
}