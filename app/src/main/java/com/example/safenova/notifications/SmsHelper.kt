package com.example.safenova.notifications

import android.content.Context
import android.telephony.SmsManager
import android.os.Build

object SmsHelper {

    fun sendBackgroundEmergencySms(
        context: Context,
        phoneNumber: String,
        message: String
    ): Boolean {
        return try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
