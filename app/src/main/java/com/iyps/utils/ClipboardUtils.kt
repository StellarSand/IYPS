package com.iyps.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.iyps.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClipboardUtils {
    
    companion object {
        
        fun manageClipboard(clipboardManager: ClipboardManager, lifecycleScope: LifecycleCoroutineScope) {
            var job: Job? = null
            
            // Clear clipboard after 1 minute if password is copied
            clipboardManager.addPrimaryClipChangedListener {
                if (clipboardManager.hasPrimaryClip()
                    && clipboardManager.primaryClipDescription?.label == "IYPS") {
                    job?.cancel()
                    job = lifecycleScope.launch {
                        delay(60000)
                        clearClipboard(clipboardManager)
                    }
                }
            }
        }
        
        // Hide from revealing on copy
        // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#SensitiveContent
        fun hideSensitiveContent(clipData: ClipData) {
            clipData.apply {
                @RequiresApi(Build.VERSION_CODES.N)
                description.extras = PersistableBundle().apply {
                    if (Build.VERSION.SDK_INT >= 33) {
                        putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                    }
                    else {
                        putBoolean("android.content.extra.IS_SENSITIVE", true)
                    }
                }
            }
        }
    
        // Only show snackbar in 12L or lower to avoid duplicate notifications
        // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
        fun showCopiedSnackbar(context: Context,
                               coordinatorLayout: CoordinatorLayout,
                               anchorView: View) {
            Snackbar.make(coordinatorLayout,
                          context.getString(R.string.copied_to_clipboard),
                          BaseTransientBottomBar.LENGTH_SHORT)
                .setAnchorView(anchorView) // Above FAB, bottom bar etc.
                .show()
        }
        
        // Clear password from clipboard
        fun clearClipboard(clipboardManager: ClipboardManager) {
            when {
                Build.VERSION.SDK_INT >= 28 -> clipboardManager.clearPrimaryClip()
                else -> clipboardManager.setPrimaryClip(ClipData.newPlainText(null, null))
            }
            
        }
        
    }
}