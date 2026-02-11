package com.ls.petfunny.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ls.petfunny.data.ShimejiGif
import com.ls.petfunny.di.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _topPackCharacters = MutableStateFlow<List<ShimejiGif>>(emptyList())
    val topPackCharacters = _topPackCharacters.asStateFlow()

    fun loadPack() {
        viewModelScope.launch {
            try {
                AppLogger.d("HIHI --> loadPack called")
                val response = apiService.getPacks()
                if (response.isSuccessful) {
                    val allPacks = response.body()?.packs // Giả sử model trả về object chứa list 'packs'

                    if (!allPacks.isNullOrEmpty()) {
                        // Tư duy Senior: Tìm pack có size lớn nhất
                        val maxPack = allPacks.maxByOrNull { it.shimejigif.size }

                        AppLogger.d("HIHI --> Pack lớn nhất là: ${maxPack?.title} với ${maxPack?.shimejigif?.size} nhân vật")

                        // Cập nhật list shimeji của pack đó vào State
                        _topPackCharacters.value = maxPack?.shimejigif ?: emptyList()
                    }
                } else {
                    AppLogger.e("HIHI --> API Error: ${response.code()}")
                }
            } catch (e : Exception){
                AppLogger.e("HIHI --> loadPack error: ${e.message}")
            }
        }
    }
}