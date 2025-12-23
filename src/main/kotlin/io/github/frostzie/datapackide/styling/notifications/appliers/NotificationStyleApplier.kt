package io.github.frostzie.datapackide.styling.notifications.appliers

import atlantafx.base.controls.Notification
import io.github.frostzie.datapackide.styling.notifications.NotificationStyle
import io.github.frostzie.datapackide.utils.LoggerProvider

/**
 * Applies a [NotificationStyle] to an AtlantaFX [Notification] control.
 */
object NotificationStyleApplier {

    private val logger = LoggerProvider.getLogger("NotificationStyleApplier")

    fun apply(notification: Notification, style: NotificationStyle) {
        logger.debug("Applying style to notification. Classes: {}, Icon: {}",
            style.styleClasses,
            style.iconSource
        )

        if (style.styleClasses.isNotEmpty()) {
            notification.styleClass.addAll(style.styleClasses)
        }
    }
}