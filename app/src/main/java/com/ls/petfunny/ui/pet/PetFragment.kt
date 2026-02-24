package com.ls.petfunny.ui.pet

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.ls.petfunny.R
import com.ls.petfunny.base.BaseFragment
import com.ls.petfunny.data.AppPreferencesHelper
import com.ls.petfunny.databinding.FragPetsBinding
import com.ls.petfunny.ui.adapter.ShimejiAdapter
import com.ls.petfunny.utils.AppLogger
import com.tp.ads.base.AdManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PetFragment : BaseFragment<FragPetsBinding, PetViewModel>() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var appPreferences: AppPreferencesHelper

    private val viewModel: PetViewModel by viewModels()

    private val shimejiAdapter by lazy {
        ShimejiAdapter { shimejiGif ->
            viewModel.downloadShimejiV2(shimejiGif)
        }
    }

    override fun getLayoutId() = R.layout.frag_pets

    override fun observersSomething() {

    }

    override fun bindingAction() {

    }

    override fun viewCreated() {
        setUpListPet()
        setUpView()
        setUpObserver()
        viewModel.loadPack()
    }

    private fun setUpObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1. Lắng nghe trạng thái Loading
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        if (isLoading) {
                            // Hiện Loading (Có thể dùng ProgressBar hoặc Dialog tùy UI của bạn)
                            binding.loadingView.visibility = View.VISIBLE
                        } else {
                            // Ẩn Loading
                            binding.loadingView.visibility = View.GONE
                        }
                    }
                }

                // 2. Lắng nghe sự kiện Toast (Chỉ phát 1 lần)
                launch {
                    viewModel.toastEvent.collect { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.topPackCharacters.collect { characters ->
                    if (characters.isNotEmpty()) {
                        shimejiAdapter.submitList(characters)
                    } else {
                        AppLogger.d("Danh sách nhân vật trống")
                    }
                }
            }
        }
    }

    private fun setUpListPet() {
        binding.rvPet.apply {
            // Hiển thị 4 cột như yêu cầu của bạn
            layoutManager = GridLayoutManager(context, 4)
            adapter = shimejiAdapter
            setHasFixedSize(true)
        }
    }

    private fun setUpView() {

    }


    companion object {

        fun newInstances(): PetFragment {
            val frag = PetFragment()
            val bundle = Bundle()
            frag.arguments = bundle
            return frag
        }
    }
}