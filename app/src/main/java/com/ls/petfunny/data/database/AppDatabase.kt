package com.ls.petfunny.data.database

import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.ls.petfunny.data.model.Mascots
import com.ls.petfunny.data.model.RoomShimeji
import com.ls.petfunny.data.model.ShimejiListing
import com.ls.petfunny.di.repository.Helper
import com.ls.petfunny.utils.AppLogger
import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayInputStream

@Dao
abstract class MascotsDao {

    //private val helper: Helper by inject()

    /*@Transaction
    @Query("Select M.id,M.name,B.bitmap from mascots as M INNER JOIN shimeji as B ON M.id=B.mascot WHERE B.frame=0 ORDER BY lastModifiedTime ASC")
    abstract fun getMascotsRecentlyAdd2(): LiveData<List<Mascots>>*/

    @Query(" SELECT * FROM mascots ORDER BY lastModifiedTime DESC")
    abstract fun getMascotsRecentlyAdd(): LiveData<List<Mascots?>?>?

    @Query("SELECT * FROM mascots ORDER BY lastModifiedTime DESC")
    abstract fun getMascotsFlow(): Flow<List<Mascots>>

    //antes de que los frames sean enviados deben convertirse a HelperT2.bitmapToByteArray(frames[i])
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun addMascotToDatabase(mascots: Mascots /*mascotId: Int, name: String*/)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun addShimejiToDatabase(shimeji: RoomShimeji /*frames: List<Bitmap>*/)

    fun addMascotToDatabase2(mascotId: Int, name: String, frames: List<Bitmap>, helper: Helper) {
        AppLogger.e("DB .... $mascotId | $name | $frames")
        try {
            val megaframe = helper.bitmapToByteArray(frames[0])
            val gardenPlanting = megaframe?.let { Mascots(mascotId, name, it) }
            if (gardenPlanting != null) {
                addMascotToDatabase(gardenPlanting)
            }
            AppLogger.d("HIHI ShimejiService ---> Add mascot to database: id=%s name=%s frames=%s", mascotId, name, frames.size)
            for (i in frames.indices) {
                val roomShimeji2 = helper.bitmapToByteArray(frames[i])?.let {
                    RoomShimeji(
                        null, // auto generado
                        it,
                        Integer.valueOf(i),
                        Integer.valueOf(mascotId)
                    )
                }
                if (roomShimeji2 != null) {
                    addShimejiToDatabase(roomShimeji2)
                }
            }
            AppLogger.d("HIHI ShimejiService ---> Add frame to database done for mascot id=$mascotId, frame size: ${frames.size}")
        } catch (e: Exception) {
            AppLogger.e(e.message.toString())
        }

    }
}

@Dao
interface ShimejiDao {

    @Query("SELECT bitmap FROM shimeji WHERE mascot = :id ORDER BY frame ASC")
    abstract fun getMascotAssets(id: Int): Cursor

    @Transaction
    @Query("Select M.id,M.name,B.bitmap from mascots as M INNER JOIN shimeji as B ON M.id=B.mascot WHERE B.frame=0")
    abstract fun getmascotThumbnails2(): Cursor

    suspend fun getmascotThumbnails22(shimejiListing: ShimejiListing): ArrayList<ShimejiListing> {
        val c = getmascotThumbnails2()
        val list = ArrayList<ShimejiListing>()
        if (c.moveToFirst()) {
            while (!c.isAfterLast) {
                val idCol = c.getColumnIndexOrThrow("id")
                val nameCol = c.getColumnIndexOrThrow("name")
                val bmpCol = c.getColumnIndexOrThrow("bitmap")
                // Skip if blob is null to avoid NPEs later
                if (c.isNull(bmpCol)) {
                    AppLogger.e("Thumbnail blob was null for mascot id=%s", c.getInt(idCol))
                } else {
                    val thumb = ShimejiListing()
                    thumb.id = c.getInt(idCol)
                    thumb.name = c.getString(nameCol)
                    thumb.thumbnailShimeji2 = c.getBlob(bmpCol)
                    AppLogger.e("getmascotThumbnails22 built thumb: id=%s name=%s bytes=%s", thumb.id, thumb.name, thumb.thumbnailShimeji2?.size)
                    list.add(thumb)
                }
                c.moveToNext()
            }
        }
        c.close()
        return list
    }

    suspend fun getMascotAssets22(id: Int, usedFrames: HashSet<Int>): HashMap<Int, Bitmap> {
        val map = HashMap<Int, Bitmap>(100)
        val c = getMascotAssets(id)
        var position: Int? = 0
        if (c.moveToFirst()) {
            while (!c.isAfterLast) {
                if (usedFrames.contains(position)) {
                    map.put(
                        position!!,
                        //helper.byteArrayToBitmap(c.getBlob(c.getColumnIndexOrThrow("bitmap")))
                        byteArrayToBitmap(c.getBlob(c.getColumnIndexOrThrow("bitmap")))
                    )
                }
                position = position!! + 1
                c.moveToNext()
            }
        }
        c.close()
        return map
    }
    fun byteArrayToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeStream(ByteArrayInputStream(bytes))
    }
}

@Database(
    entities = [RoomShimeji::class, Mascots::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shimejiDao(): ShimejiDao
    abstract fun mascotsDao(): MascotsDao
}