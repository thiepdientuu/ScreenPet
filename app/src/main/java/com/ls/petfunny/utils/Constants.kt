package com.ls.petfunny.utils

object Constants {
	const val SEARCH_IGNORE = "[-+^\\\\,\"*&^%$@!~=;:<>/?.()]"
	const val PREF_NAME = "petFunny"
	const val DB_NAME = "petfunny.db"
	var baseUrl = "https://www.google.com/"
	var storagePet = "https://space.akimeji.com/"

	val STORAGE_PERMISSION = arrayOf(
		android.Manifest.permission.READ_EXTERNAL_STORAGE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE
	)
}