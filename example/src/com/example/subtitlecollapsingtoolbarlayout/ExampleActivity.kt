package com.example.subtitlecollapsingtoolbarlayout

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import com.google.android.material.appbar.SubtitleCollapsingToolbarLayout
import com.hendraanggrian.prefy.BindPreference
import com.hendraanggrian.prefy.PreferencesSaver
import com.hendraanggrian.prefy.Prefy
import com.hendraanggrian.prefy.android.AndroidPreferences
import com.hendraanggrian.prefy.android.get
import com.hendraanggrian.prefy.bind
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.android.synthetic.main.activity_example.*

class ExampleActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {
    @JvmField @BindPreference("theme") var theme2 = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    @JvmField @BindPreference var showSubtitle = false
    @JvmField @BindPreference var statusBarScrim = Color.TRANSPARENT
    @JvmField @BindPreference var contentScrim = Color.TRANSPARENT
    @JvmField @BindPreference var marginLeft = 0
    @JvmField @BindPreference var marginTop = 0
    @JvmField @BindPreference var marginRight = 0
    @JvmField @BindPreference var marginBottom = 0
    @JvmField @BindPreference var collapsedTitleColor = Color.TRANSPARENT
    @JvmField @BindPreference var collapsedSubtitleColor = Color.TRANSPARENT
    @JvmField @BindPreference @ColorInt var expandedTitleColor = Color.TRANSPARENT
    @JvmField @BindPreference @ColorInt var expandedSubtitleColor = Color.TRANSPARENT

    private lateinit var preferences: AndroidPreferences
    private lateinit var saver: PreferencesSaver

    @Px private var marginScale = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        setSupportActionBar(toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, ExampleFragment())
            .commitNow()
        preferences = Prefy[this]
        marginScale = resources.getDimensionPixelSize(R.dimen.margin_scale)

        fab.setOnClickListener { appbar.setExpanded(true) }
        onSharedPreferenceChanged(preferences, "")
    }

    override fun onResume() {
        super.onResume()
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_example, menu)
        menu.findItem(
            when (theme2) {
                AppCompatDelegate.MODE_NIGHT_NO -> R.id.lightThemeItem
                AppCompatDelegate.MODE_NIGHT_YES -> R.id.darkThemeItem
                else -> R.id.defaultThemeItem
            }
        ).isChecked = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.defaultThemeItem, R.id.lightThemeItem, R.id.darkThemeItem -> {
                theme2 = when (item.itemId) {
                    R.id.lightThemeItem -> AppCompatDelegate.MODE_NIGHT_NO
                    R.id.darkThemeItem -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                saver.save()
                AppCompatDelegate.setDefaultNightMode(theme2)
            }
            R.id.resetItem -> {
                preferences.edit { clear() }
                ProcessPhoenix.triggerRebirth(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(p: SharedPreferences, key: String) {
        saver = preferences.bind(this)
        toolbarLayout.subtitle = if (showSubtitle) SubtitleCollapsingToolbarLayout::class.java.simpleName else null
        toolbarLayout.statusBarScrim = if (statusBarScrim.isConfigured()) ColorDrawable(statusBarScrim) else null
        toolbarLayout.contentScrim = if (contentScrim.isConfigured()) ColorDrawable(contentScrim) else null
        toolbarLayout.collapsedTitleGravity =
            preferences.getGravity("collapsedGravity", GravityCompat.START or Gravity.CENTER_VERTICAL)
        toolbarLayout.expandedTitleGravity =
            preferences.getGravity("expandedGravity", GravityCompat.START or Gravity.BOTTOM)
        if (marginLeft != 0) toolbarLayout.expandedTitleMarginStart = marginLeft * marginScale
        if (marginTop != 0) toolbarLayout.expandedTitleMarginTop = marginTop * marginScale
        if (marginRight != 0) toolbarLayout.expandedTitleMarginEnd = marginRight * marginScale
        if (marginBottom != 0) toolbarLayout.expandedTitleMarginBottom = marginBottom * marginScale
        collapsedTitleColor.ifConfigured { toolbarLayout.setCollapsedTitleTextColor(it) }
        collapsedSubtitleColor.ifConfigured { toolbarLayout.setCollapsedSubtitleTextColor(it) }
        expandedTitleColor.ifConfigured { toolbarLayout.setExpandedTitleTextColor(it) }
        expandedSubtitleColor.ifConfigured { toolbarLayout.setExpandedSubtitleTextColor(it) }
    }

    private companion object {
        fun AndroidPreferences.getGravity(key: String, def: Int): Int {
            val iterator = getStringSet(key, emptySet())!!.iterator()
            var gravity: Int? = null
            while (iterator.hasNext()) {
                val next = iterator.next().toInt()
                gravity = when (gravity) {
                    null -> next
                    else -> gravity or next
                }
            }
            return gravity ?: def
        }

        fun @receiver:ColorInt Int.isConfigured(): Boolean = this != Color.TRANSPARENT

        fun @receiver:ColorInt Int.ifConfigured(action: (Int) -> Unit) {
            if (isConfigured()) action(this)
        }
    }
}