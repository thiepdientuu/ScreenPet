package com.ls.petfunny.ui.pet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.viewModelScope
import com.ls.petfunny.R
import com.ls.petfunny.base.BaseViewModel
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
class PetViewModel @Inject constructor(
    private val apiService: ApiService,
    private val teamListingService: TeamListingService,
    private val repository: MascotsRepository
) : BaseViewModel() {
    private val _topPackCharacters = MutableStateFlow<List<ShimejiGif>>(emptyList())
    val topPackCharacters = _topPackCharacters.asStateFlow()

    // 🔥 THÊM MỚI: Quản lý Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 🔥 THÊM MỚI: Quản lý sự kiện Show Toast (Dùng SharedFlow để bắn 1 lần, xoay màn hình không bị show lại)
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

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
                AppLogger.d("HIHI ShimejiService --> downloadShimeji called url: $zipUrl")
                // Giả sử có API để download shimeji
                val response = apiService.downloadImage(zipUrl)
                if (response.isSuccessful) {
                    AppLogger.d("HIHI ShimejiService --> Download thành công cho: ${shimejiGif.name ?: shimejiGif.nick}")
                    val body = response.body() ?: return@launch
                    val thumbnails = ArrayList<Bitmap>()
                    withContext(Dispatchers.IO) {
                        AppLogger.d("HIHI ShimejiService --> Start unzip image: ${shimejiGif.name ?: shimejiGif.nick}")
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
                    AppLogger.e("HIHI ShimejiService --> Success unzip list bitmap = " + thumbnails.size)
                } else {
                    AppLogger.e("HIHI ShimejiService --> Download API Error: ${response.code()}")
                }
            } catch (e : Exception){
                AppLogger.e("HIHI ShimejiService --> downloadShimeji error: ${e.message}")
            }
        }
    }

    fun downloadShimejiV2(shimejiGif: ShimejiGif) {
        if (_isLoading.value) return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val zipUrl = Constants.storagePet.trimEnd('/') + "/" + (shimejiGif.shimejiGif)
                AppLogger.d("HIHI MainViewModel --> downloadShimeji called url: $zipUrl")

                val response = apiService.downloadImage(zipUrl)
                if (response.isSuccessful) {
                    val body = response.body() ?: return@launch
                    val thumbnails = mutableListOf<Bitmap>()

                    // [Senior Tip] Tách riêng tác vụ đọc/giải nén nặng xuống I/O Thread
                    withContext(Dispatchers.IO) {
                        AppLogger.d("HIHI MainViewModel --> Start stream & unzip directly")

                        // Cắm trực tiếp ống nước (Network Stream) vào máy giải nén (ZipInputStream)
                        // Bỏ qua bước lưu file zip vào RAM, chống OOM triệt để.
                        ZipInputStream(body.byteStream()).use { zis ->
                            val buffer = ByteArray(4096)
                            var entry = zis.nextEntry

                            while (entry != null) {
                                if (!entry.isDirectory) {
                                    // Tạo buffer mới VỪA ĐỦ cho từng bức ảnh nhỏ
                                    val outStream = ByteArrayOutputStream()
                                    var count: Int
                                    while (zis.read(buffer).also { count = it } != -1) {
                                        outStream.write(buffer, 0, count)
                                    }

                                    val imgBytes = outStream.toByteArray()
                                    val bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
                                    if (bmp != null) {
                                        thumbnails.add(bmp)
                                    }
                                    outStream.close()
                                }
                                zis.closeEntry()
                                entry = zis.nextEntry
                            }
                        }
                    }

                    AppLogger.d("HIHI MainViewModel --> Success extract bitmaps = ${thumbnails.size}")

                    if (thumbnails.isNotEmpty()) {
                        val mascot = ShimejiListing().apply {
                            id = shimejiGif.id
                            name = shimejiGif.name
                            visibility = true
                            status = R.string.download_finish
                            setStatuss = R.string.download_finish
                        }

                        // Lưu xuống DB (DB cũng là tác vụ IO)
                        withContext(Dispatchers.IO) {
                            teamListingService.addMascot(mascot, thumbnails)
                        }
                        // 2. Bắn sự kiện tải thành công
                        _toastEvent.emit("Tải xuống ${shimejiGif.name} thành công!")
                    } else {
                        _toastEvent.emit("Tải xuống thất bại: Không tìm thấy ảnh trong gói.")
                    }
                } else {

                    _toastEvent.emit("Lỗi máy chủ: ${response.code()}")
                    AppLogger.e("HIHI MainViewModel --> Download API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                AppLogger.e("HIHI MainViewModel --> download error: ${e.message}")
                _toastEvent.emit("Đã xảy ra lỗi khi tải: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}