package com.tp.ads.pref

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST", "unused")
@SuppressLint("ApplySharedPref")
abstract class Preferences(context: Context) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("Ads_Preferences", Context.MODE_PRIVATE)
    }

    abstract class PrefDelegate<T>(val prefKey: String?) {
        abstract operator fun getValue(thisRef: Any?, property: KProperty<*>): T
        abstract operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
    }

    enum class StorableType {
        String,
        Int,
        Float,
        Boolean,
        Long,
        StringSet
    }

    inner class GenericPrefDelegate<T>(
        prefKey: String? = null,
        private val defaultValue: T?,
        val type: StorableType
    ) :
        PrefDelegate<T?>(prefKey) {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            try {
                when (type) {
                    StorableType.String ->
                        prefs.getString(prefKey ?: property.name, defaultValue as String?) as T

                    StorableType.Int ->
                        prefs.getInt(prefKey ?: property.name, defaultValue as Int) as T

                    StorableType.Float ->
                        prefs.getFloat(prefKey ?: property.name, defaultValue as Float) as T

                    StorableType.Boolean ->
                        prefs.getBoolean(prefKey ?: property.name, defaultValue as Boolean) as T

                    StorableType.Long ->
                        prefs.getLong(prefKey ?: property.name, defaultValue as Long) as T

                    StorableType.StringSet ->
                        prefs.getStringSet(
                            prefKey ?: property.name,
                            defaultValue as Set<String>
                        ) as T
                }
            } catch (e: ClassCastException) {
                prefs.edit().remove(prefKey).commit()
                defaultValue as T
            }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            when (type) {
                StorableType.String -> {
                    prefs.edit().putString(prefKey ?: property.name, value as String?).commit()
                }

                StorableType.Int -> {
                    prefs.edit().putInt(prefKey ?: property.name, value as Int).commit()
                }

                StorableType.Float -> {
                    prefs.edit().putFloat(prefKey ?: property.name, value as Float).commit()
                }

                StorableType.Boolean -> {
                    prefs.edit().putBoolean(prefKey ?: property.name, value as Boolean).commit()
                }

                StorableType.Long -> {
                    prefs.edit().putLong(prefKey ?: property.name, value as Long).commit()
                }

                StorableType.StringSet -> {
                    prefs.edit().putStringSet(prefKey ?: property.name, value as Set<String>)
                        .commit()
                }
            }
        }

    }

    fun stringPref(prefKey: String? = null, defaultValue: String? = null) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.String)

    fun intPref(prefKey: String? = null, defaultValue: Int = 0) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.Int)

    fun floatPref(prefKey: String? = null, defaultValue: Float = 0f) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.Float)

    fun booleanPref(prefKey: String? = null, defaultValue: Boolean = false) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.Boolean)

    fun longPref(prefKey: String? = null, defaultValue: Long = 0L) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.Long)

    fun stringSetPref(prefKey: String? = null, defaultValue: Set<String> = HashSet()) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.StringSet)

    fun stringMutableSetPref(
        prefKey: String? = null,
        defaultValue: MutableSet<String> = HashSet()
    ) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.StringSet)

}