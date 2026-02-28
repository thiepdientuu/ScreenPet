package com.ls.petfunny.ui.pet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.viewModelScope
import com.ls.petfunny.R
import com.ls.petfunny.base.BaseViewModel
import com.ls.petfunny.data.model.ShimejiGif
import com.ls.petfunny.data.model.ShimejiListing
import com.ls.petfunny.di.ApiService
import com.ls.petfunny.di.repository.MascotsRepository
import com.ls.petfunny.di.repository.TeamListingService
import com.ls.petfunny.utils.AllEvents
import com.ls.petfunny.utils.AppLogger
import com.ls.petfunny.utils.Constants
import com.ls.petfunny.utils.TrackingHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    private val apiService: ApiService,
    private val teamListingService: TeamListingService,
    private val repository: MascotsRepository,
    @ApplicationContext val context: Context
) : BaseViewModel() {
    private val _topPackCharacters = MutableStateFlow<List<ShimejiGif>>(emptyList())
    val topPackCharacters = _topPackCharacters.asStateFlow()

    // 🔥 THÊM MỚI: Quản lý Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 🔥 THÊM MỚI: Quản lý sự kiện Show Toast (Dùng SharedFlow để bắn 1 lần, xoay màn hình không bị show lại)
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()



    fun loadPack() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                AppLogger.d("HIHI --> loadPack called")
                val response = apiService.getPacks()
                if (response.isSuccessful) {
                    val allPacks = response.body()?.packs // Giả sử model trả về object chứa list 'packs'
                    val result = mutableListOf<ShimejiGif>()
                    if (!allPacks.isNullOrEmpty()) {
                        val listMascots = repository.getAllMascotsSuspend()
                        allPacks.forEach { pack ->
                            pack.shimejigif.forEach { shimejiGif ->
                                if (listMascots.any { it.id == shimejiGif.id }) {
                                    shimejiGif.downloaded = true
                                    AppLogger.d("HIHI --> Shimeji đã download " + shimejiGif.name)
                                }
                            }
                            result.addAll(pack.shimejigif)
                        }
                        TrackingHelper.logEvent(AllEvents.LOAD_PET + "success_" + result.size)
                        _topPackCharacters.value = result
                    }
                } else {
                    TrackingHelper.logEvent(AllEvents.LOAD_PET + "fail")
                    AppLogger.e("HIHI --> API Error: ${response.code()}")
                }
            } catch (e : Exception){
                TrackingHelper.logEvent(AllEvents.LOAD_PET + "fail")
                AppLogger.e("HIHI --> loadPack error: ${e.message}")
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
                            _topPackCharacters.update { currentList ->
                                currentList.map { item ->
                                    if (item.id == mascot.id) {
                                        item.copy(downloaded = true)
                                    } else {
                                        item
                                    }
                                }
                            }
                            teamListingService.addMascot(mascot, thumbnails)
                        }
                        TrackingHelper.logEvent(AllEvents.DOWN_PET + "success")
                        // 2. Bắn sự kiện tải thành công
                        _toastEvent.emit(context.getString(R.string.download_success) + " " +  shimejiGif.name)
                    } else {
                        TrackingHelper.logEvent(AllEvents.DOWN_PET + "fail")
                        _toastEvent.emit(context.getString(R.string.download_error))
                    }
                } else {
                    TrackingHelper.logEvent(AllEvents.DOWN_PET + "fail")
                    _toastEvent.emit(context.getString(R.string.download_error))
                    AppLogger.e("HIHI MainViewModel --> Download API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                TrackingHelper.logEvent(AllEvents.DOWN_PET + "fail")
                AppLogger.e("HIHI MainViewModel --> download error: ${e.message}")
                _toastEvent.emit(context.getString(R.string.download_error) + e.localizedMessage )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun activeMascot(id : Int){
        teamListingService.activeMascotReal(id)
    }
}