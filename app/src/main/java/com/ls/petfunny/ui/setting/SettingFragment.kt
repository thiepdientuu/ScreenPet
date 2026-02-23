package com.ls.petfunny.ui.setting

import android.os.Bundle
import com.ls.petfunny.R
import com.ls.petfunny.base.BaseFragment
import com.ls.petfunny.data.AppPreferencesHelper
import com.ls.petfunny.databinding.FragSettingBinding
import com.tp.ads.base.AdManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragSettingBinding, SettingViewModel>() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var appPreferences: AppPreferencesHelper

    override fun getLayoutId() = R.layout.frag_setting

    override fun observersSomething() {

    }

    override fun bindingAction() {

    }

    override fun viewCreated() {
        setUpView()
    }

    private fun setUpView() {

    }



    companion object {

        fun newInstances(): SettingFragment {
            val frag = SettingFragment()
            val bundle = Bundle()
            frag.arguments = bundle
            return frag
        }
    }
}