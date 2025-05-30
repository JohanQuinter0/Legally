package com.example.legally

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class IntroPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Card1Fragment()
            1 -> Card2Fragment()
            2 -> Card3Fragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}