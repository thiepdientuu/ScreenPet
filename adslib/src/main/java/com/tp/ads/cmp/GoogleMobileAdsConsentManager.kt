package com.tp.ads.cmp

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class GoogleMobileAdsConsentManager @Inject constructor(
    @ApplicationContext val context: Context,
) {
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /** Interface definition for a callback to be invoked when consent gathering is complete. */
    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    /** Helper variable to determine if the app can request ads. */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    private val isShownConsentForm= AtomicBoolean(false)

    /** Helper variable to determine if the privacy options form is required. */
    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Helper method to call the UMP SDK methods to request consent information and load/show a
     * consent form if necessary.
     */
    fun gatherConsent(
        activity: Activity,
        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener,
    ) {
        if (isShownConsentForm.get()) {
            Log.d(TAG ,"##### BAILS shown one time")
            onConsentGatheringCompleteListener.consentGatheringComplete(null)
            return
        }
        // For testing purposes, you can force a DebugGeography of EEA or NOT_EEA.
        val debugSettings =
            ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                // Check your logcat output for the hashed device ID e.g.
                // "Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("ABCDEF012345")" to use
                // the debug functionality.
                .addTestDeviceHashedId("252A960773744E5F836D423EB66E4B6A")
                .build()

        val params = ConsentRequestParameters.Builder()
            //.setConsentDebugSettings(debugSettings)
            .setTagForUnderAgeOfConsent(false)
            .build()

        // Requesting an update to consent information should be called on every app launch.
        Log.d(TAG ,"##### START requestConsentInfoUpdate")
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                Log.d(TAG ,"##### FINISH requestConsentInfoUpdate")
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    Log.d(TAG ,"##### FINISH show Consent Form")
                    isShownConsentForm.set(true)
                    // Consent has been gathered.
                    onConsentGatheringCompleteListener.consentGatheringComplete(formError)
                }
            },
            { requestConsentError ->
                Log.d(TAG ,"##### ERROR RequestConsentInfo")
                onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
            }
        )
    }

    /** Helper method to call the UMP SDK method to show the privacy options form. */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener,
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }

    companion object{
        private const val TAG = "GoogleMobileAdsConsentManager"
    }
}