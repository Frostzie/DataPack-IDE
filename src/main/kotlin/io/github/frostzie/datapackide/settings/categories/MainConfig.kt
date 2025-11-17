package io.github.frostzie.datapackide.settings.categories

import io.github.frostzie.datapackide.settings.annotations.ConfigCategory
import io.github.frostzie.datapackide.settings.annotations.ConfigEditorButton
import io.github.frostzie.datapackide.settings.annotations.ConfigOption
import io.github.frostzie.datapackide.settings.annotations.Expose
import io.github.frostzie.datapackide.utils.OpenLinks

object MainConfig {
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
