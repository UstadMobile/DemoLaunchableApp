package org.openeel.demolaunchableapp

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Go through the context to find the parent activity context, or throw an exception
 */
fun Context.getActivityContext(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> this.baseContext.getActivityContext()
    else -> throw IllegalArgumentException("Not an activity context")
}
