package com.ls.petfunny.di.repository

import android.graphics.Bitmap
import com.ls.petfunny.data.database.ShimejiDao
import com.ls.petfunny.data.model.ShimejiListing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShimejiRepository @Inject constructor(
    private val shimejiDao: ShimejiDao, private val shimejiListing: ShimejiListing
) {
    suspend fun getShimejis(id: Int, usedFrames: HashSet<Int>): HashMap<Int, Bitmap> {
        return withContext(Dispatchers.IO) { shimejiDao.getMascotAssets22(id, usedFrames) }
    }


    suspend fun mascotThumbnails(): ArrayList<ShimejiListing> {
        return withContext(Dispatchers.IO) {
            shimejiDao.getmascotThumbnails22(shimejiListing)
        }
    }
}