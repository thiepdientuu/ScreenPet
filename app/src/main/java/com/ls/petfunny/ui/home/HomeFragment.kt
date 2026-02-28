package com.ls.petfunny.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.nativead.NativeAdView
import com.ls.petfunny.MainActivity
import com.ls.petfunny.R
import com.ls.petfunny.base.BaseFragment
import com.ls.petfunny.data.AppPreferencesHelper
import com.ls.petfunny.databinding.FragHomeBinding
import com.ls.petfunny.utils.AllEvents
import com.ls.petfunny.utils.AppConstants
import com.ls.petfunny.utils.AppLogger
import com.ls.petfunny.utils.TrackingHelper
import com.tp.ads.base.AdManager
import com.tp.ads.utils.AdCommonUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragHomeBinding, HomeViewModel>() {

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var adManager: AdManager

    private val activeMascotAdapter by lazy {
        ActiveMascotAdapter { shimejiGif ->
            shimejiGif.id?.let { viewModel.inActiveMascot(it) }
        }
    }

    private var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    @Inject
    lateinit var appPreferences: AppPreferencesHelper

    override fun getLayoutId() = R.layout.frag_home

    override fun observersSomething() {
        viewModel.mascotUiState.observe(viewLifecycleOwner) { list ->
            AppLogger.d("HIHI ---> Home Flow nhận dữ liệu mới: ${list.size} items")
            activeMascotAdapter.submitList(list)
        }
        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { p0, p1 ->
            AppLogger.d("HIHI ---> HomeFragment onSharedPreferenceChanged")
            if (p1 == AppConstants.ACTIVE_SHIMEJI_IDS) {
                viewModel.loadActiveMascot()
            }
        }
        context?.getSharedPreferences(AppConstants.MY_PREFS, Context.MODE_MULTI_PROCESS)?.registerOnSharedPreferenceChangeListener(prefListener)
        adManager.nativeHome.observe(viewLifecycleOwner){
            if (activity == null || adManager.nativeHome.value == null ) return@observe

            lifecycleScope.launch(Dispatchers.Main) {
                binding.layoutAds.visibility = View.VISIBLE
                val adView = LayoutInflater.from(activity).inflate(
                    R.layout.native_home, null
                ) as NativeAdView
                adManager.nativeHome.value?.let { ad ->
                    adManager.showNativeAd(
                        adView,
                        binding.layoutAds,
                        ad
                    )
                    adManager.nativeHome.value = null
                }
            }
        }
    }

    override fun bindingAction() {

    }

    override fun onResume() {
        super.onResume()
        TrackingHelper.logEvent(AllEvents.VIEW_HOME)
    }

    override fun viewCreated() {
        adManager.loadNativeHomeHigh(
            adsUnitHigh = AdCommonUtils.NATIVE_HOME_HIGH_KEY,
            adUnitNormal = AdCommonUtils.NATIVE_HOME_NORMAL_KEY
        )
        setUpView()
        setUpListPet()
        viewModel.loadActiveMascot()
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.getSharedPreferences(AppConstants.MY_PREFS, Context.MODE_MULTI_PROCESS)?.unregisterOnSharedPreferenceChangeListener(prefListener)
    }

    private fun setUpListPet() {
        binding.rvPetSlots.apply {
            layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL,false)
            adapter = activeMascotAdapter
        }
    }

    private fun setUpView() {
        binding.switchEnable.setOnCheckedChangeListener { buttonView, isChecked ->
            TrackingHelper.logEvent(AllEvents.CLICK_ENABLE_SHOW_PET + if(isChecked) "on" else "off")
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