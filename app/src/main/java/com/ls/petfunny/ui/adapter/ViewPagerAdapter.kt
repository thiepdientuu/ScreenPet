package com.ls.petfunny.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ls.petfunny.ui.home.HomeFragment
import com.ls.petfunny.ui.pet.PetFragment
import com.ls.petfunny.ui.setting.SettingFragment

class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment.newInstances()
            1 -> PetFragment.newInstances()
            2 -> SettingFragment.newInstances()
            else -> HomeFragment.newInstances()
        }
    }
}