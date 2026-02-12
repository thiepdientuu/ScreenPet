package com.ls.petfunny.di.repository

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.ls.petfunny.data.database.MascotsDao
import com.ls.petfunny.data.model.Mascots
import com.ls.petfunny.utils.AppLogger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MascotsRepository @Inject constructor(
    private val mascotsDao: MascotsDao,
    private val helper: Helper
) {
    fun addMascotToDatabase(id: Int, name: String, frames: List<Bitmap>) = mascotsDao.addMascotToDatabase2(id, name, frames, helper)

    fun getLiveDataOfMascotsInDb(): LiveData<List<Mascots?>>? {
        return try {
            mascotsDao.getMascotsRecentlyAdd()
        } catch (e: Exception) {
            AppLogger.e(e.message)
            return null
        } as LiveData<List<Mascots?>>
    }


    fun getAllMascots(): Flow<List<Mascots>> = mascotsDao.getMascotsFlow()
}