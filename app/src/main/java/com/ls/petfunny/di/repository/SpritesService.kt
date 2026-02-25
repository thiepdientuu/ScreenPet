package com.ls.petfunny.di.repository

import com.ls.petfunny.data.model.Sprites
import com.ls.petfunny.utils.AppLogger
import com.ls.petfunny.utils.SpriteUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class SpritesService @Inject constructor(
    private val helper: Helper, private val db: ShimejiRepository
) {
    var cachedSprites = ConcurrentHashMap<Int, Sprites>(100)
    private var currentSizeMultiplier: Double = 0.5
    suspend fun setSizeMultiplier(multiplier: Double) = withContext(Dispatchers.IO) {
        if (multiplier != currentSizeMultiplier) {
            currentSizeMultiplier = multiplier
            if (cachedSprites.isNotEmpty()) {
                AppLogger.e("setSizeMultiplier: Bắt đầu resize toàn bộ cache")
                for (key in cachedSprites.keys) {
                    val mascotAssets = db.getShimejis(key, SpriteUtil.usedSprites(key))
                    if (mascotAssets.isNotEmpty()) {
                        cachedSprites[key] = helper.resizeSprites(
                            Sprites(mascotAssets),
                            currentSizeMultiplier
                        )
                    }
                }
            }
        }
    }

    suspend fun getSpritesById(id: Int): Sprites? {
        if (!cachedSprites.containsKey(id) || cachedSprites[id] == null) {
            addMascot(db, id)
            AppLogger.e("ShimejiService ---> getSpritesById id: $id from database")
        }
        AppLogger.e("ShimejiService ---> getSpritesById id: $id from cache")
        return cachedSprites[id]
    }

    suspend fun loadSpritesForMascots(ids: List<Int>) {
        invalidateSprites(ids)
        for (id: Int in ids) {
            if (!cachedSprites.containsKey(id)) {
                addMascot(db, id)
            }
        }
    }

    private suspend fun addMascot(db: ShimejiRepository, id: Int) = withContext(Dispatchers.IO) {
        val mascotAssets = db.getShimejis(id, SpriteUtil.usedSprites(id))
        if (mascotAssets.isNotEmpty()) {
            cachedSprites[id] =
                helper.resizeSprites(
                    Sprites(mascotAssets),
                    currentSizeMultiplier
                )
        }
        AppLogger.e("ShimejiService ---> addMascot to Cache id: $id")
    }

    private suspend fun invalidateSprites(activeShimejis: List<Int>) {
        AppLogger.e("ShimejiService ---> invalidateSprites")
        val distinctIds = activeShimejis.toSet()
        val cacheIds = cachedSprites.keys.toMutableSet()
        cacheIds.removeAll(distinctIds)
        for (id in cacheIds) {
            AppLogger.e("Giải phóng bộ nhớ (recycle) cho id: $id")
            cachedSprites[id]?.recycle()
            cachedSprites.remove(id)
        }
    }

    companion object {
    }
}