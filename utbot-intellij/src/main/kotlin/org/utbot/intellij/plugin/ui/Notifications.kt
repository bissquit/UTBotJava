package org.utbot.intellij.plugin.ui

import com.intellij.notification.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import javax.swing.event.HyperlinkEvent

abstract class Notifier {
    protected abstract val notificationType: NotificationType
    protected abstract val displayId: String
    protected abstract fun content(project: Project?, module: Module?, info: String): String

    open fun notify(info: String, project: Project? = null, module: Module? = null) {
        notificationGroup
                .createNotification(content(project, module, info), notificationType)
                .notify(project)
    }

    protected val notificationGroup: NotificationGroup
        get() = NotificationGroup(displayId, NotificationDisplayType.BALLOON)
}

abstract class WarningNotifier : Notifier() {
    override val notificationType: NotificationType = NotificationType.WARNING
    final override fun notify(info: String, project: Project?, module: Module?) {
        super.notify(info, project, module)
    }
}

abstract class ErrorNotifier : Notifier() {
    final override val notificationType: NotificationType = NotificationType.ERROR

    final override fun notify(info: String, project: Project?, module: Module?) {
        super.notify(info, project, module)
        error(content(project, module, info))
    }
}

object CommonErrorNotifier : ErrorNotifier() {
    override val displayId: String = "UTBot plugin errors"
    override fun content(project: Project?, module: Module?, info: String): String = info
}

object UnsupportedJdkNotifier : ErrorNotifier() {
    override val displayId: String = "Unsupported JDK"
    override fun content(project: Project?, module: Module?, info: String): String =
            "JDK versions older than 8 are not supported. This project's JDK version is $info"
}

object MissingLibrariesNotifier : WarningNotifier() {
    override val displayId: String = "Missing libraries"
    override fun content(project: Project?, module: Module?, info: String): String =
            "Library $info missing on the test classpath of module ${module?.name}"
}

@Suppress("unused")
object UnsupportedTestFrameworkNotifier : ErrorNotifier() {
    override val displayId: String = "Unsupported test framework"
    override fun content(project: Project?, module: Module?, info: String): String =
            "Test framework $info is not supported yet"
}

abstract class UrlNotifier : Notifier() {

    protected abstract val titleText: String
    protected abstract val urlOpeningListener: NotificationListener

    override fun notify(info: String, project: Project?, module: Module?) {
        notificationGroup
            .createNotification(
                titleText,
                content(project, module, info),
                notificationType,
                urlOpeningListener,
            ).notify(project)
    }
}

abstract class InformationUrlNotifier : UrlNotifier() {
    override val notificationType: NotificationType = NotificationType.INFORMATION
}

object SarifReportNotifier : InformationUrlNotifier() {

    override val displayId: String = "SARIF report"

    override val titleText: String = "" // no title

    override val urlOpeningListener: NotificationListener = NotificationListener.UrlOpeningListener(false)

    override fun content(project: Project?, module: Module?, info: String): String = info
}

object TestsReportNotifier : InformationUrlNotifier() {
    override val displayId: String = "Generated unit tests report"

    override val titleText: String = "Report of the unit tests generation via UtBot"

    override val urlOpeningListener: TestReportUrlOpeningListener = TestReportUrlOpeningListener()

    override fun content(project: Project?, module: Module?, info: String): String {
        // Remember last project and module to use them for configurations.
        urlOpeningListener.project = project
        urlOpeningListener.module = module
        return info
    }
}

/**
 * Listener that handles URLs starting with [prefix], like "#utbot/configure-mockito".
 *
 * Current implementation
 */
class TestReportUrlOpeningListener: NotificationListener.Adapter() {
    companion object {
        const val prefix = "#utbot/"
        const val mockitoSuffix = "configure-mockito"
    }
    private val defaultListener = NotificationListener.UrlOpeningListener(false)

    // Last project and module to be able to use them when activated for configuration tasks.
    var project: Project? = null
    var module: Module? = null

    override fun hyperlinkActivated(notification: Notification, e: HyperlinkEvent) {
        val description = e.description
        if (description.startsWith(prefix)) {
            handleDescription(description.removePrefix(prefix))
        }
        else {
            return defaultListener.hyperlinkUpdate(notification, e)
        }
    }

    private fun handleDescription(descriptionSuffix: String) {
        when {
            descriptionSuffix.startsWith(mockitoSuffix) -> {
                project?.let { module?.let {
                        if (createMockFrameworkNotificationDialog("Configure mock framework") == Messages.YES) {
                            configureMockFramework(project!!, module!!)
                        }
                    } ?: error("Could not configure mock framework: null in module ")
                } ?: error("Could not configure mock framework: null in project ")
            }
            else -> error("No such command with #utbot prefix: $descriptionSuffix")
        }
    }
}
