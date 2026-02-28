package com.ls.petfunny.ui.setting

import com.ls.petfunny.base.BaseViewModel
import com.ls.petfunny.di.repository.TeamListingService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val teamListingService: TeamListingService,
) : BaseViewModel() {

    fun updateSpeedPet(speed : String){
        teamListingService.updateSpeedPet(speed)
    }

    fun updateSizePet(size : String){
        teamListingService.updateSizePet(size)
    }

}