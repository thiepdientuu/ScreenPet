package com.ls.petfunny

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val apiService: ApiService,
    private val teamListingService: TeamListingService,
    private val repository: MascotsRepository
) : ViewModel() {

    private val _topPackCharacters = MutableStateFlow<List<ShimejiGif>>(emptyList())
    val topPackCharacters = _topPackCharacters.asStateFlow()

    val mascotUiState: StateFlow<List<Mascots>> = repository.getAllMascots()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    fun downloadShimeji(shimejiGif: ShimejiGif) {
        viewModelScope.launch {
            try {
                val zipUrl = Constants.storagePet.trimEnd('/') + "/" + (shimejiGif.shimejiGif )
                AppLogger.d("HIHI --> downloadShimeji called url: $zipUrl")
                // Giả sử có API để download shimeji
                val response = apiService.downloadImage(zipUrl)
                if (response.isSuccessful) {
                    AppLogger.d("HIHI --> Download thành công cho: ${shimejiGif.name ?: shimejiGif.nick}")
                    val body = response.body() ?: return@launch
                    val thumbnails = ArrayList<android.graphics.Bitmap>()
                    withContext(Dispatchers.IO) {
                        AppLogger.d("HIHI --> Start unzip image: ${shimejiGif.name ?: shimejiGif.nick}")
                        body.byteStream().use { stream ->
                            val byteArrayOut = ByteArrayOutputStream()
                            val buffer = ByteArray(4096)
                            var count: Int
                            while (true) {
                                count = stream.read(buffer)
                                if (count == -1) break
                                byteArrayOut.write(buffer, 0, count)
                            }
                            val zis = ZipInputStream(ByteArrayInputStream(byteArrayOut.toByteArray()))
                            while (zis.nextEntry != null) {
                                byteArrayOut.reset()
                                while (true) {
                                    count = zis.read(buffer)
                                    if (count == -1) break
                                    byteArrayOut.write(buffer, 0, count)
                                }
                                val bmp = BitmapFactory.decodeByteArray(byteArrayOut.toByteArray(), 0, byteArrayOut.size())
                                if (bmp != null) thumbnails.add(bmp)
                            }
                        }
                    }
                    val mascot = ShimejiListing().apply {
                        id = shimejiGif.id
                        name = shimejiGif.name
                        visibility = true
                        status = R.string.download_finish
                        setStatuss = R.string.download_finish
                    }
                    withContext(Dispatchers.IO) {
                        teamListingService.addMascot(mascot, thumbnails)
                    }
                    AppLogger.e("HIHI --> Success unzip list bitmap = " + thumbnails.size)
                } else {
                    AppLogger.e("HIHI --> Download API Error: ${response.code()}")
                }
            } catch (e : Exception){
                AppLogger.e("HIHI --> downloadShimeji error: ${e.message}")
            }
        }
    }
}