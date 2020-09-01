package com.example.solarsystemapp.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.solarsystemapp.R
import kotlinx.android.synthetic.main.fragment_settings.*

class Settings : Fragment() {
    private val PREFS_NAME = "prefs"                //constants for sharedPreferences
    private val PREF_DARK_THEME = "dark_theme"
    private val PREF_TEXT_SIZE = "text_size"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //returning our layout file
        val preferences = activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)        //sets up a sharedPreference
        val useDarkTheme = preferences?.getBoolean(PREF_DARK_THEME, false)
        val textSize = preferences?.getInt(PREF_TEXT_SIZE, 1)
        updateTheme()

        val view: View = inflater.inflate(R.layout.fragment_settings, container, false)
        val textSizeSeekBar = view.findViewById<SeekBar>(R.id.textSizeSeekBar)
        val darkThemeSwitch = view.findViewById<Switch>(R.id.darkThemeSwitch)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        darkThemeSwitch.isChecked = useDarkTheme!!
        darkThemeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->           //on click listener for darkThemeSwitch
                                                                  //calls toggleTheme function passing true or false
            if (isChecked){
                view.setBackgroundColor(Color.parseColor("#303030"))
                smallText.setTextColor(Color.parseColor("#FFFFFF"))
                normalText.setTextColor(Color.parseColor("#FFFFFF"))
                largeText.setTextColor(Color.parseColor("#FFFFFF"))
                darkThemeSwitch.setTextColor(Color.parseColor("#FFFFFF"))
                darkThemeSwitch.thumbDrawable.setColorFilter(Color.parseColor("#2aac4b"), PorterDuff.Mode.MULTIPLY)
                darkThemeSwitch.trackDrawable.setColorFilter(Color.parseColor("#2aac4b"), PorterDuff.Mode.MULTIPLY)
                textSizeSeekBar.progressDrawable.setColorFilter(Color.parseColor("#2aac4b"), PorterDuff.Mode.MULTIPLY)
                saveButton.background.setColorFilter(Color.parseColor("#1e2756"), PorterDuff.Mode.MULTIPLY)

            } else {
                view.setBackgroundColor(Color.parseColor("#FAFAFA"))
                smallText.setTextColor(Color.parseColor("#000000"))
                normalText.setTextColor(Color.parseColor("#000000"))
                largeText.setTextColor(Color.parseColor("#000000"))
                darkThemeSwitch.setTextColor(Color.parseColor("#000000"))
                darkThemeSwitch.thumbDrawable.setColorFilter(Color.parseColor("#03DAC5"), PorterDuff.Mode.MULTIPLY)
                darkThemeSwitch.trackDrawable.setColorFilter(Color.parseColor("#03DAC5"), PorterDuff.Mode.MULTIPLY)
                textSizeSeekBar.progressDrawable.setColorFilter(Color.parseColor("#03DAC5"), PorterDuff.Mode.MULTIPLY)
                saveButton.background.setColorFilter(Color.parseColor("#6200EE"), PorterDuff.Mode.MULTIPLY)
            }

        }

        textSizeSeekBar.progress = textSize!!
        textSizeSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //here we can write some code to do something when progress is changed
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //here we can write some code to do something whenever the user touche the seekbar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // show some message after user stopped scrolling the seekbar
                when (seekBar.progress){
                    0 -> {
                        smallText.textSize = 10F
                        normalText.textSize = 10F
                        largeText.textSize = 10F
                        darkThemeSwitch.textSize = 10F
                        saveButton.textSize = 10F
                    }
                    1 -> {
                        smallText.textSize = 14F
                        normalText.textSize = 14F
                        largeText.textSize = 14F
                        darkThemeSwitch.textSize = 14F
                        saveButton.textSize = 14F
                    }
                    2 -> {
                        smallText.textSize = 20F
                        normalText.textSize = 20F
                        largeText.textSize = 20F
                        darkThemeSwitch.textSize = 20F
                        saveButton.textSize = 20F
                    }
                }
                //putTextSize(seekBar.progress)
            }
        })

        saveButton.setOnClickListener {
            putDarkTheme(darkThemeSwitch.isChecked)
            putTextSize(textSizeSeekBar.progress)
            activity?.recreate()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.title = "Settings"
    }

    private fun putDarkTheme(darkTheme: Boolean) {
        val editor = activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
        editor?.putBoolean(PREF_DARK_THEME, darkTheme)
        editor?.apply()                                                                  //pushes either true or false onto the sharedPreference         //restarts activity
    }
    private fun putTextSize(textSize: Int) {
        val editor = activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
        editor?.putInt(PREF_TEXT_SIZE, textSize)
        editor?.apply()
    }

    private fun updateTheme() : Boolean{
        val preferences = activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)            //sets up a sharedPreference
        val useDarkTheme = preferences?.getBoolean(PREF_DARK_THEME, false)                   //sharedPreference's are used to store information between activitys...
        val textSize = preferences?.getInt(PREF_TEXT_SIZE, 1)
        //also saves when app is fully closed for next use
        if (useDarkTheme!!) {
            when (textSize){
                0 -> activity?.setTheme(R.style.AppTheme_Dark_SmallText)
                1 -> activity?.setTheme(R.style.AppTheme_Dark)
                2 -> activity?.setTheme(R.style.AppTheme_Dark_LargeText)
            }
        } else {
            when (textSize){
                0 -> activity?.setTheme(R.style.AppTheme_SmallText)
                1 -> activity?.setTheme(R.style.AppTheme)
                2 -> activity?.setTheme(R.style.AppTheme_LargeText)
            }
        }
        return useDarkTheme                                                                 //the apps default theme is the light theme that is set in the manifest
    }
}