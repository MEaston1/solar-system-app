package com.example.solarsystemapp

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.engine.util.android.ContentUtils
import com.example.solarsystemapp.fragments.*
import com.google.android.material.navigation.NavigationView


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        updateTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //loads in Assets
        ContentUtils.provideAssets(this)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)


        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView =
            findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        displaySelectedScreen(R.id.nav_home)

    }

    override fun onBackPressed() {
        val drawer =
            findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val positionOfMenuItem = 0 // or whatever...

        val item = menu!!.getItem(positionOfMenuItem)
        val s = SpannableString("Settings")
        s.setSpan(ForegroundColorSpan(Color.BLACK), 0, s.length, 0)
        item.title = s
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Handle action bar item clicks here. The action bar will
        val id = item.itemId
        return if (id == R.id.action_settings) {
            displaySelectedScreen(item.itemId)
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(item.itemId)
        return true
    }

    private fun displaySelectedScreen(itemId: Int) { //creating fragment object
        var fragment: Fragment? = null
        when (itemId) {
            R.id.nav_home -> fragment = Home()
            R.id.nav_mercury -> fragment = Mercury()
            R.id.nav_venus -> fragment = Venus()
            R.id.nav_earth -> fragment = Earth()
            R.id.nav_mars -> fragment = Mars()
            R.id.nav_jupiter -> fragment = Jupiter()
            R.id.nav_saturn -> fragment = Saturn()
            R.id.nav_uranus -> fragment = Uranus()
            R.id.nav_neptune -> fragment = Neptune()
            R.id.nav_sun -> fragment = Sun()
            R.id.nav_moon -> fragment = Moon()
            R.id.action_settings -> fragment = Settings()
        }
        //replacing the fragment
        if (fragment != null) {
            val ft =
                supportFragmentManager.beginTransaction()
            ft.replace(R.id.content_frame, fragment)
            ft.commit()
        }
        val drawer =
            findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
    }
}

