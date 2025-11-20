package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.settings.annotations.*
import io.github.frostzie.datapackide.utils.OpenLinks
import javafx.beans.property.SimpleStringProperty

object MainConfig {

    @Expose
    @ConfigCategory(name = "UI")
    @ConfigOption(
        name = "Modified Indicator",
        desc = "Suffix added on a file in tab when it has unsaved changes."
    )
    @ConfigEditorTextField
    val dirtyIndicator = SimpleStringProperty(" ●")

    @Expose
    @ConfigCategory(name = "UI")
    @ConfigOption(
        name = "Modified File Color",
        desc = "Changes the color of the name of a file that has unsaved changes."
    )
    @ConfigEditorColorPicker
    val dirtyFileColor = SimpleStringProperty("#f7aeae")

    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Discord Server", desc = "Join our Discord server for support and community!")
    @ConfigEditorButton(text = "Discord")
    val discordLink: () -> Unit = { OpenLinks.discordLink() }

    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Github", desc = "Check out the source code and contribute on GitHub!")
    @ConfigEditorButton(text = "GitHub")
    val githubLink: () -> Unit = { OpenLinks.gitHubLink() }

    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Report Issues", desc = "Report bugs or issues you encounter!")
    @ConfigEditorButton(text = "Report Issue")
    val reportBugLink: () -> Unit = { OpenLinks.reportBugLink() }

    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Support Me", desc = "Support the development of this mod!")
    @ConfigEditorButton(text = "Buy Me A Coffee")
    val buyMeACoffeeLink: () -> Unit = { OpenLinks.buyMeACoffeeLink() }

    /* TODO: Add once modrinth page is up
    @Expose
    @ConfigCategory(name = "Links")
    @ConfigOption(name = "Modrinth", desc = "Check out the project on Modrinth!")
    @ConfigEditorButton(text = "Modrinth")
    val modrinthLink: () -> Unit = { OpenLinks.modrinthLink() }
    */
}
