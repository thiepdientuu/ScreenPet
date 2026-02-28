package com.ls.petfunny.di.repository

import android.graphics.Bitmap
import com.ls.petfunny.data.model.ShimejiListing
import com.ls.petfunny.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class TeamListingService @Inject constructor(
    private val db: ShimejiRepository,
    private val helper: Helper,
    private val provideMascotRepository: MascotsRepository
) : ArrayList<ShimejiListing?>() {
    internal var mascotLimit: Int = 2
    private var nextInsertPosition: Int = 0

    companion object {

        var cachedThumbs: ArrayList<ShimejiListing> = ArrayList()

    }

    // Sử dụng một Scope riêng cho Service (hoặc dùng ProcessLifecycleOwner)
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        serviceScope.launch {
            cachedThumbs = db.mascotThumbnails()
            AppLogger.e("cachedThumbs.size: ${cachedThumbs.size}")
            val ids = helper.getActiveTeamMembers()
            AppLogger.e("Active ids at startup: %s", ids)
            for (id in ids) {
                AppLogger.e("id: $id")
                getThumbById(id)?.let { super.add(it) }
            }
            nextInsertPosition = mascotLimit
        }
    }

    private val allMascotIds: List<Int?>
        get() {
            val list = ArrayList<Int>()
            for (m in cachedThumbs) {
                m.id.let { Integer.valueOf(it) }?.let { list.add(it) }
            }
            return list
        }

    val getAllThumbs: MutableList<ShimejiListing>
        get() = cachedThumbs

    fun getThumbById(id: Int): ShimejiListing? {
        AppLogger.e("getThumbById: $id")
        AppLogger.e("getThumbById cachedThumbs.size: ${cachedThumbs.size}")
        for (m: ShimejiListing in cachedThumbs) {
            AppLogger.e("1")
            AppLogger.e("${m.name}")
            if (m.id == id) {
                AppLogger.e("2")
                return m
            }
        }
        AppLogger.e("return null: $id")
        return null
    }

    fun hasTeamMember(id: Int): Boolean {
        for (m in cachedThumbs) {
            if (m.id == id) {
                AppLogger.e("hasTeamMember true")
                return true
            }
        }
        AppLogger.e("hasTeamMember false")
        return false
    }

    fun addMascot(listing: ShimejiListing, frames: List<Bitmap>) {
        try {
            val asde = helper.bitmapToByteArray(frames[0])
            if (asde != null && asde.isNotEmpty()) {
                listing.thumbnailShimeji2 = asde
            }
            cachedThumbs.add(listing)

            provideMascotRepository.addMascotToDatabase(
                listing.id,
                listing.name!!,
                frames
            )
        } catch (e: Exception) {
            AppLogger.e(e.message)
        }
    }

    fun activeMascotReal(id: Int) {
        val current = helper.getActiveTeamMembers().toMutableList()
        if (!current.contains(id)) {
            current.add(id)
        }
        helper.saveActiveTeamMembers(current)
    }

    fun inActiveMascotReal(id: Int) {
        val current = helper.getActiveTeamMembers().toMutableList()
        if (current.contains(id)) {
            current.remove(id)
        }
        helper.saveActiveTeamMembers(current)
    }

    fun activeMascot(id: Int) {
        val current = helper.getActiveTeamMembers().toMutableList()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        helper.saveActiveTeamMembers(current)
    }

    fun getActivePetId() = helper.getActiveTeamMembers()


    internal val mascotIDs: List<Int>
        get() {
            val list = ArrayList<Int>(10)
            var i = 0
            while (i < size && i < this.mascotLimit) {
                get(i)?.id?.let { Integer.valueOf(it) }?.let { list.add(it) }
                i++
            }
            return list
        }

    //lo llama el main activity o lo q sea si la compra fue exitosa
    fun setMascotLimit(limit: Int) {
        this.mascotLimit = limit
        this.nextInsertPosition = size
    }

    fun mascotExistsAt(position: Int): Boolean {
        return get(position) != null
    }

    internal fun isOutOfBounds(position: Int): Boolean {
        return position >= this.mascotLimit
    }

    override fun add(element: ShimejiListing?): Boolean {
        if (size < this.mascotLimit) {
            this.nextInsertPosition++
            return super.add(element)
        }
        if (this.nextInsertPosition >= this.mascotLimit) {
            this.nextInsertPosition = 0
        }
        set(this.nextInsertPosition, element)
        this.nextInsertPosition++
        return true
    }

    override fun get(index: Int): ShimejiListing? {
        if (index >= size) {
            return null
        }
        return super.get(index)

    }

    internal fun remove(position: Int): ShimejiListing? {
        if (position >= size) {
            return ShimejiListing()
        }
        this.nextInsertPosition = position
        return super.removeAt(position)
    }

    fun updateSpeedPet(speed : String){
        helper.setSpeedMultiplier(speed)
    }

    fun updateSizePet(size : String){
        helper.setSizeMultiplier(size)
    }

}