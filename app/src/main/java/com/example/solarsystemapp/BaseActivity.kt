package com.example.solarsystemapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    private val PREFS_NAME = "prefs"                        //Constants for sharedPreference
    private val PREF_DARK_THEME = "dark_theme"
    private val PREF_TEXT_SIZE = "text_size"

    fun updateTheme() : Boolean{
        val preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)            //sets up a sharedPreference
        val useDarkTheme = preferences?.getBoolean(PREF_DARK_THEME, false)                   //sharedPreference's are used to store information between activitys...
        val textSize = preferences?.getInt(PREF_TEXT_SIZE, 1)
        //also saves when app is fully closed for next use
        if (useDarkTheme!!) {
            when (textSize){
                0 -> setTheme(R.style.AppTheme_Dark_SmallText)
                1 -> setTheme(R.style.AppTheme_Dark)
                2 -> setTheme(R.style.AppTheme_Dark_LargeText)
            }
        } else {
            when (textSize){
                0 -> setTheme(R.style.AppTheme_SmallText)
                1 -> setTheme(R.style.AppTheme)
                2 -> setTheme(R.style.AppTheme_LargeText)
            }
        }
        return useDarkTheme                                                                 //the apps default theme is the light theme that is set in the manifest
        //the apps default theme is the light theme that is set in the manifest
    }                                                                                       //the themes colors are defined in the styles.xml file

}