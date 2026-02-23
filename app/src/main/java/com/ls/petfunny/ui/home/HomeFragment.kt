package com.ls.petfunny.ui.home

import android.os.Bundle
import com.ls.petfunny.MainActivity
import com.ls.petfunny.R
import com.ls.petfunny.base.BaseFragment
import com.ls.petfunny.data.AppPreferencesHelper
import com.ls.petfunny.databinding.FragHomeBinding
import com.tp.ads.base.AdManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragHomeBinding, HomeViewModel>() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var appPreferences: AppPreferencesHelper

    override fun getLayoutId() = R.layout.frag_home

    override fun observersSomething() {

    }

    override fun bindingAction() {

    }

    override fun viewCreated() {
        setUpView()
    }

    private fun setUpView() {
        binding.switchEnable.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                (activity as? MainActivity)?.startShimeijService()
            } else {
                (activity as? MainActivity)?.stopShimejiService()
            }
        }
    }



    companion object {

        fun newInstances(): HomeFragment {
            val frag = HomeFragment()
            val bundle = Bundle()
            frag.arguments = bundle
            return frag
        }
    }
}