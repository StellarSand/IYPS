package com.iyps.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.CountDownTimer

class ClipboardUtils {
    
    companion object {
        
        fun manageClipboard(clipboardManager: ClipboardManager) {
            var clearClipboardTimer: CountDownTimer? = null
            
            // Clear clipboard after 1 minute if password is copied
            clipboardManager.addPrimaryClipChangedListener {
                clearClipboardTimer?.cancel()
                clearClipboardTimer = object : CountDownTimer(60000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    
                    // On timer finish, clear clipboard
                    override fun onFinish() {
                        clearClipboard(clipboardManager)
                    }
                }.start()
            }
        }
        
        // Clear password from clipboard
        fun clearClipboard(clipboardManager: ClipboardManager) {
            
            if (Build.VERSION.SDK_INT >= 28) {
                clipboardManager.clearPrimaryClip()
            }
            else {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, null))
            }
            
        }
        
    }
}