package com.ls.petfunny.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ls.petfunny.base.BaseViewModel
import com.ls.petfunny.data.model.Mascots
import com.ls.petfunny.di.ApiService
import com.ls.petfunny.di.repository.Helper
import com.ls.petfunny.di.repository.MascotsRepository
import com.ls.petfunny.di.repository.TeamListingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val teamListingService: TeamListingService,
    private val repository: MascotsRepository,
    private val helper: Helper
) : BaseViewModel() {

    val mascotUiState = MutableLiveData<List<Mascots>>()

    fun loadActiveMascot(){
       viewModelScope.launch(Dispatchers.IO) {
           val listIdActive = teamListingService.getActivePetId()
           val listMascots = repository.getMascotsByIds(listIdActive)
           mascotUiState.postValue(listMascots)
       }
    }

    fun inActiveMascot(id : Int){
        viewModelScope.launch(Dispatchers.IO) {
           teamListingService.inActiveMascotReal(id)
        }
    }
}