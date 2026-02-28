package com.ls.petfunny.utils

object AllEvents {
    // other
    const val USER_FIRST_OPEN = "user_first_open"
    const val USER_REOPEN = "user_reopen"
    // view
    const val VIEW_SPLASH = "view_splash"
    const val VIEW_MAIN = "view_main"
    const val VIEW_HOME = "view_home"
    const val VIEW_PET = "view_pet"
    const val VIEW_SETTING = "view_setting"

    // event user
    const val PERMISSION = "permission_overlay_"
    const val CLICK_DOWNLOAD = "click_download"
    const val CLICK_SETTING = "click_setting"
    const val CLICK_ENABLE_SHOW_PET = "click_show_pet_"

    const val CLICK_GHOST = "click_ghost_"
    const val CLICK_REMOVE_PET = "click_remove_pet"

    const val CLICK_FEEDBACK = "click_feedback"
    const val CLICK_RATE = "click_rate"

    const val CLICK_CHANGE_SPEED = "click_change_speed"
    const val CLICK_CHANGE_SIZE = "click_change_size"

    // service
    const val SERVICE_START = "service_start"
    const val SERVICE_STOP = "service_destroy"

    // performance API
    const val LOAD_PET = "api_load_pest_"
    const val DOWN_PET = "api_down_pest_"
    const val CONFIG_LOAD = "config_load_"
}