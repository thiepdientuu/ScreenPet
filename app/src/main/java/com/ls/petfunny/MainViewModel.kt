package com.ls.petfunny

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ls.petfunny.data.model.Mascots
import com.ls.petfunny.data.model.ShimejiGif
import com.ls.petfunny.data.model.ShimejiListing
import com.ls.petfunny.di.ApiService
import com.ls.petfunny.di.repository.MascotsRepository
import com.ls.petfunny.di.repository.TeamListingService
import com.ls.petfunny.utils.AppLogger
import com.ls.petfunny.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel()