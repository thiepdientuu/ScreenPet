package com.ls.petfunny.di.repository

import com.ls.petfunny.data.model.Sprites
import com.ls.petfunny.utils.AppLogger
import com.ls.petfunny.utils.SpriteUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class SpritesService @Inject constructor(
    private val helper: Helper, private val db: ShimejiRepository
) {
    suspend fun setSizeMultiplier(multiplier: Double, shimejiId: Int) = withContext(Dispatchers.IO) {
        if (multiplier != sizeMultiplier) {
            sizeMultiplier = multiplier
            if (!cachedSprites.isEmpty()) {
                for (key in cachedSprites.keys()) {
                    AppLogger.e("setSizeMultiplier")
                    cachedSprites[key] = helper.resizeSprites(
                        Sprites(
                            db.getShimejis(
                                key.toInt(),
                                SpriteUtil.usedSprites(shimejiId)
                            )
                        ),
                        sizeMultiplier
                    )
                }
            } else {
                AppLogger.e("!cachedSprites.isEmpty() ?")
            }
        }
    }

    suspend fun getSpritesById(id: Int): Sprites {
        if (!cachedSprites.containsKey(id) || cachedSprites[id] == null) {
            addMascot(db, id)
            AppLogger.e("ShimejiService ---> getSpritesById id: $id from database")
        }
        AppLogger.e("ShimejiService ---> getSpritesById id: $id from cache")
        return cachedSprites[id]!!
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
                    sizeMultiplier
                )
        }
        AppLogger.e("ShimejiService ---> addMascot to Cache id: $id")
    }

    private suspend fun invalidateSprites(activeShimejis: List<Int>) {
        AppLogger.e("ShimejiService ---> invalidateSprites")
        val distinctIds = HashSet(activeShimejis)
        val cacheIds = HashSet(cachedSprites.keys)
        cacheIds.removeAll(distinctIds)
        for (id in cacheIds) {
            AppLogger.e("Invalidating id: $id")
            (cachedSprites[id] as Sprites).recycle()
            cachedSprites.remove(id)
        }
    }

    companion object {
        internal var cachedSprites = ConcurrentHashMap<Int, Sprites>(100)
        internal var instance: SpritesService? = null
        internal var sizeMultiplier: Double = 2.0

    }
}