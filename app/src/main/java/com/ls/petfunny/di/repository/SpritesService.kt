package com.ls.petfunny.di.repository

import com.ls.petfunny.data.model.Sprites
import com.ls.petfunny.utils.AppLogger
import com.ls.petfunny.utils.SpriteUtil
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class SpritesService @Inject constructor(
    private val helper: Helper, private val db: ShimejiRepository
) {
    fun setSizeMultiplier(multiplier: Double, shimejiId: Int) {
        runBlocking {
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
    }

    fun getSpritesById(id: Int): Sprites {
        if (!cachedSprites.containsKey(id) || cachedSprites[id] == null) {
            addMascot(db, id)
        }
        return cachedSprites[id]!! //[id?.let { Integer.valueOf(it) }]!!
    }

    fun loadSpritesForMascots(ids: List<Int>) {
        invalidateSprites(ids)
        for (id: Int in ids) {
            if (!cachedSprites.containsKey(id)) {
                addMascot(db, id)
            }
        }
    }

    private fun addMascot(db: ShimejiRepository, id: Int) {
        runBlocking {
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
    }

    private fun invalidateSprites(activeShimejis: List<Int>) {
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
        internal var sizeMultiplier: Double = 1.0

    }
}