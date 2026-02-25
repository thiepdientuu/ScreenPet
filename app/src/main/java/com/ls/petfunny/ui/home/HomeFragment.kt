package com.ls.petfunny.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.ls.petfunny.MainActivity
import com.ls.petfunny.R
import com.ls.petfunny.base.BaseFragment
import com.ls.petfunny.data.AppPreferencesHelper
import com.ls.petfunny.databinding.FragHomeBinding
import com.ls.petfunny.utils.AppConstants
import com.ls.petfunny.utils.AppLogger
import com.tp.ads.base.AdManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragHomeBinding, HomeViewModel>() {

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var adManager: AdManager

    private val activeMascotAdapter by lazy {
        ActiveMascotAdapter { shimejiGif ->

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
                (activity as? MainActivity)?.startShimeijService()
                viewModel.loadActiveMascot()
            }
        }
        context?.getSharedPreferences(AppConstants.MY_PREFS, Context.MODE_MULTI_PROCESS)?.registerOnSharedPreferenceChangeListener(prefListener)
    }

    override fun bindingAction() {

    }

    override fun viewCreated() {
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