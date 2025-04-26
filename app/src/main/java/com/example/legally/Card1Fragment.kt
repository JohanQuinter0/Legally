package com.example.legally

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class Card1Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.card1, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val siguiente = view.findViewById<ImageView>(R.id.siguiente)
        siguiente.setOnClickListener {
            (activity as? Intro)?.let {
                it.findViewById<ViewPager2>(R.id.viewPager).currentItem = 1
            }
        }
    }
}