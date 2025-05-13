package com.example.legally

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.core.graphics.toColorInt

class Intro : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnContinuar: Button
    private lateinit var dotsContainer: LinearLayout
    private lateinit var dots: Array<ImageView?>

    private val activeColor = "#30B0C7".toColorInt()
    private val inactiveColor = "#EDEEEF".toColorInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        viewPager = findViewById(R.id.viewPager)
        btnContinuar = findViewById(R.id.btnContinuar)
        dotsContainer = findViewById(R.id.dotsContainer)

        val adapter = IntroPagerAdapter(this)
        viewPager.adapter = adapter

        updateButtonState(0)

        setupDots()
        updateDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position)
                updateButtonState(position)
            }
        })

        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.page_margin)
        val offsetPx = resources.getDimensionPixelOffset(R.dimen.page_offset)

        viewPager.setPageTransformer { page, position ->
            val viewPager = page.parent.parent as ViewPager2
            val offset = position * +(1 * offsetPx + pageMarginPx)

            if (viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                page.translationX = offset
            } else {
                page.translationY = offset
            }
        }

        btnContinuar.setOnClickListener {
            startActivity(Intent(this, InicioSesion::class.java))
            finish()
        }
    }

    private fun updateButtonState(currentPosition: Int) {
        if (currentPosition == 2) {
            btnContinuar.isEnabled = true
            btnContinuar.setBackgroundColor(activeColor)
            btnContinuar.alpha = 1f
        } else {
            btnContinuar.isEnabled = false
            btnContinuar.setBackgroundColor(inactiveColor)
            btnContinuar.alpha = 0.7f
        }
    }

    private fun setupDots() {
        val pagerAdapter = viewPager.adapter ?: return
        dots = arrayOfNulls(pagerAdapter.itemCount)

        val screenWidth = resources.displayMetrics.widthPixels
        val horizontalMargin = (screenWidth * 0.01).toInt()
        val topMargin = (screenWidth * 0.015).toInt()

        for (i in dots.indices) {
            dots[i] = ImageView(this).apply {
                setImageResource(R.drawable.dot_indicator)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(horizontalMargin, topMargin, horizontalMargin, topMargin)
                }
                dotsContainer.addView(this)
            }
        }
    }

    private fun updateDots(selectedPosition: Int) {
        for (i in dots.indices) {
            dots[i]?.isSelected = i == selectedPosition
        }
    }
}