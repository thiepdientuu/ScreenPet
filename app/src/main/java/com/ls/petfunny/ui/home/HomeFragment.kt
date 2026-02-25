package com.ls.petfunny.ui.home

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.ls.petfunny.MainActivity
import com.ls.petfunny.R
import com.ls.petfunny.base.BaseFragment
import com.ls.petfunny.data.AppPreferencesHelper
import com.ls.petfunny.databinding.FragHomeBinding
import com.ls.petfunny.utils.AppLogger
import com.tp.ads.base.AdManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var appPreferences: AppPreferencesHelper

    override fun getLayoutId() = R.layout.frag_home

    override fun observersSomething() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mascotUiState.collect { list ->
                    AppLogger.d("HIHI ---> Home Flow nhận dữ liệu mới: ${list.size} items")
                    activeMascotAdapter.submitList(list)
                }
            }
        }
    }

    override fun bindingAction() {

    }

    override fun viewCreated() {
        setUpView()
        setUpListPet()
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
                if (activeMascotAdapter.itemCount == 0) {
                    showToast(getString(R.string.need_down_pet))
                    (activity as? MainActivity)?.gotoPetStore()
                }
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