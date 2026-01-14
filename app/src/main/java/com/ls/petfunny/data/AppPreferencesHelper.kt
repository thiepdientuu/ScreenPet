package com.ls.petfunny.data

import android.content.Context
import com.ls.entertainment.player.data.pref.Preferences

import com.ls.petfunny.utils.Constants.PREF_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppPreferencesHelper @Inject constructor(
    @ApplicationContext context: Context
) : Preferences(context, PREF_NAME) {
    var country by stringPref("setting_country", "OT")
    var firstOpenTime by longPref("FirstOpenTime", 0L)
    var didChooseLanguage by booleanPref("didChooseLanguage", false)
    var firstOpenApp by booleanPref("FirstOpenApp", true)
    var userNeed by stringPref("userNeed", "")

    //
    companion object {
        const val EU_SERVER_REGION =
            ",al,ad,at,by,be,ba,bg,hr,cy,cz,dk,ee,fo,fi,fr,de,gi,gr,hu,is,ie,im,in,it,rs,lv,li,lt,lu,mk,mt,md,mc,me,nl,no,pl,pt,ro,ru,sm,rs,sk,si,es,se,ch,ua,gb,va,rs,ml,so,ng,ci,uz,au,ye,mr,bf,ly,sn,za,"
        const val ASIA_SERVER_REGION =
            ",af,am,az,bh,bd,bt,bn,kh,cx,cc,io,ge,id,ir,iq,il,jo,kz,kw,kg,la,lb,mo,my,mv,mn,mm,np,kp,om,ps,ph,qa,sa,sg,lk,sy,tj,th,tr,tm,ae,vn,"
        const val EAST_ASIA_REGION = ",tw,jp,kr,hk,cn,"
        const val DEFAULT_NOTIFY_SALE_OFF_COUNTRY = "US"
    }
}