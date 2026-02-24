package com.ls.petfunny.ui.home

import androidx.lifecycle.viewModelScope
import com.ls.petfunny.base.BaseViewModel
import com.ls.petfunny.data.model.Mascots
import com.ls.petfunny.di.ApiService
import com.ls.petfunny.di.repository.MascotsRepository
import com.ls.petfunny.di.repository.TeamListingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService,
    private val teamListingService: TeamListingService,
    private val repository: MascotsRepository
) : BaseViewModel() {

    val mascotUiState: StateFlow<List<Mascots>> = repository.getAllMascots()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}