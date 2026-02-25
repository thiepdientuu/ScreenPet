package com.ls.petfunny.di.repository

import android.graphics.Bitmap
import com.ls.petfunny.data.database.MascotsDao
import com.ls.petfunny.data.model.Mascots
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MascotsRepository @Inject constructor(
    private val mascotsDao: MascotsDao,
    private val helper: Helper
) {
    fun addMascotToDatabase(id: Int, name: String, frames: List<Bitmap>) = mascotsDao.addMascotToDatabase2(id, name, frames, helper)


    fun getAllMascots(): Flow<List<Mascots>> = mascotsDao.getMascotsFlow()

    suspend fun getAllMascotsSuspend(): List<Mascots> = mascotsDao.getMascotsSuspend()

    suspend fun getMascotsByIds(idList: List<Int>): List<Mascots> = mascotsDao.getMascotsByIds(idList)
}