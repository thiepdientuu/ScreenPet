package com.ls.petfunny.ui.setting

import android.os.Bundle
import android.widget.SeekBar
import androidx.fragment.app.viewModels
import com.ls.petfunny.MainActivity
import com.ls.petfunny.R
import com.ls.petfunny.base.BaseFragment
import com.ls.petfunny.data.AppPreferencesHelper
import com.ls.petfunny.databinding.FragSettingBinding
import com.ls.petfunny.ui.pet.PetViewModel
import com.ls.petfunny.utils.AllEvents
import com.ls.petfunny.utils.AppLogger
import com.ls.petfunny.utils.TrackingHelper
import com.ls.petfunny.utils.openPlayStore
import com.ls.petfunny.utils.sendFeedbackEmail
import com.tp.ads.base.AdManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragSettingBinding, SettingViewModel>() {

    @Inject
    lateinit var adManager: AdManager

    private val viewModel: SettingViewModel by viewModels()

    @Inject
    lateinit var appPreferences: AppPreferencesHelper

    override fun getLayoutId() = R.layout.frag_setting

    override fun observersSomething() {

    }

    override fun bindingAction() {
        binding.switchGhost.setOnCheckedChangeListener { buttonView, isChecked ->
            TrackingHelper.logEvent(AllEvents.CLICK_GHOST + if(isChecked) "on" else "off")
        }

        binding.btnRateUs.setOnClickListener {
            TrackingHelper.logEvent(AllEvents.CLICK_RATE)
            context?.openPlayStore(context?.packageName)
        }
        binding.btnFeedback.setOnClickListener {
            TrackingHelper.logEvent(AllEvents.CLICK_FEEDBACK)
            context?.sendFeedbackEmail()
        }
        binding.seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val actualSpeed = 1.2f + (seekBar.progress / 10.0f)
                viewModel.updateSpeedPet(actualSpeed.toString())
                showToast(getString(R.string.update_speed_success) + " " + actualSpeed)
                TrackingHelper.logEvent(AllEvents.CLICK_CHANGE_SPEED)
                AppLogger.d("Hihi ---> update speed: $actualSpeed")
            }
        })

        binding.seekBarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val finalProgress = seekBar.progress
                val finalSize = 0.5f + (finalProgress * 0.5f)
                viewModel.updateSizePet(finalSize.toString())
                showToast(getString(R.string.update_size_success) + " " + finalSize)
                TrackingHelper.logEvent(AllEvents.CLICK_CHANGE_SIZE)
                AppLogger.d("Hihi ---> update size: $finalSize")
            }
        })


    }

    override fun viewCreated() {
        setUpView()
    }

    override fun onResume() {
        super.onResume()
        TrackingHelper.logEvent(AllEvents.VIEW_SETTING)
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