package com.ls.petfunny.data.model

import com.ls.petfunny.R
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class ShimejiListing() {

    var id: Int = 0
    var name: String? = ""
    var visibility: Boolean? = true
    var status: Int = R.string.app_name
    var thumbnailShimeji2: ByteArray? = null
    var setStatuss: Int
        get() = this.status
        set(value) {
            status = value
        }

}