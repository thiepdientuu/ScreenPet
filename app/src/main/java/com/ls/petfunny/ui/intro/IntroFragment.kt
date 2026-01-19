package com.ls.petfunny.ui.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.nativead.NativeAdView
import com.ls.petfunny.R
import com.ls.petfunny.SplashActivity
import com.ls.petfunny.base.BaseFragment
import com.ls.petfunny.data.AppPreferencesHelper
import com.ls.petfunny.databinding.FragIntroBinding
import com.ls.petfunny.utils.setSafeOnClickListener
import com.tp.ads.base.AdManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IntroFragment : BaseFragment<FragIntroBinding, IntroViewModel>() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var appPreferences: AppPreferencesHelper

    override fun getLayoutId() = R.layout.frag_intro

    override fun observersSomething() {
        val typeIntro = requireArguments().getInt(KEY_TYPE_INTRO, TYPE_INTRO_1)
        if (typeIntro == TYPE_INTRO_1) {
            adManager.nativeIntro1.observe(viewLifecycleOwner) {
                if (activity == null || adManager.nativeIntro1.value == null ) return@observe

                lifecycleScope.launch(Dispatchers.Main) {
                    binding.layoutAds.visibility = View.VISIBLE
                    val adView = LayoutInflater.from(activity).inflate(
                        R.layout.native_intro1, null
                    ) as NativeAdView
                    adManager.nativeIntro1.value?.let { ad ->
                        adManager.showNativeAd(
                            adView,
                            binding.layoutAds,
                            ad
                        )
                    }

                }

            }
        } else {
            adManager.nativeIntro2.observe(viewLifecycleOwner) {
                if (activity == null || adManager.nativeIntro2.value == null ) return@observe

                lifecycleScope.launch(Dispatchers.Main) {
                    binding.layoutAds.visibility = View.VISIBLE
                    val adView = LayoutInflater.from(activity).inflate(
                        R.layout.native_intro1, null
                    ) as NativeAdView
                    adManager.nativeIntro2.value?.let { ad ->
                        adManager.showNativeAd(
                            adView,
                            binding.layoutAds,
                            ad
                        )
                    }
                }

            }
        }

    }

    override fun bindingAction() {
            binding.background.setSafeOnClickListener {  }
    }

    override fun viewCreated() {
        setUpView()
    }

    private fun setUpView() {
        val typeIntro = requireArguments().getInt(KEY_TYPE_INTRO, TYPE_INTRO_1)
        if (typeIntro == TYPE_INTRO_1) {
            binding.imgViewOne.setAnimation(R.raw.gift)
            binding.imgViewOne.playAnimation()

            binding.btnNext.setSafeOnClickListener {
                (activity as? SplashActivity)?.addFragment(
                    newInstances(getString(R.string.msg_tittle_intro2),getString(R.string.msg_msg_intro2),
                    TYPE_INTRO_2)
                )
            }
        } else {
            binding.imgViewOne.setAnimation(R.raw.dog)
            binding.imgViewOne.playAnimation()

            binding.btnNext.setSafeOnClickListener {
                (activity as? SplashActivity)?.gotoHome()
            }

        }
        requireArguments().getString(KEY_TITLE)?.let {
            binding.tvTitle.text = it
        }
        requireArguments().getString(KEY_MESSAGE)?.let {
            binding.tvMessage.text = it
        }
        binding.btnNext.text = getString(R.string.msg_btn_continue)
    }



    companion object {

        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        private const val KEY_TYPE_INTRO = "key_intro"
        const val TYPE_INTRO_1 = 1
        const val TYPE_INTRO_2 = 2

        fun newInstances(title: String, description: String, typeIntro: Int): IntroFragment {
            val frag = IntroFragment()
            val bundle = Bundle()
            bundle.putString(KEY_TITLE, title)
            bundle.putString(KEY_MESSAGE, description)
            bundle.putInt(KEY_TYPE_INTRO, typeIntro)
            frag.arguments = bundle
            return frag
        }
    }
}