package com.danielkorgel.projectivy.plugin.cinemaglow


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import java.io.File
import androidx.core.net.toUri

class SettingsFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        return Guidance(
            getString(R.string.plugin_name),
            getString(R.string.plugin_description),
            getString(R.string.settings),
            AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_banner_drawable)
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        val actionClearCache = GuidedAction.Builder(context)
            .id(ACTION_ID_CLEAR_CACHE)
            .title(R.string.setting_clear_cache_title)
            .description(R.string.setting_clear_cache_description)
            .descriptionEditable(false)
            .build()
        actions.add(actionClearCache)

        val actionGetProjectIvy = GuidedAction.Builder(context)
            .id(ACTION_ID_GET_PROJECTIVY)
            .title(R.string.setting_projectivy_title)
            .description(R.string.setting_projectivy_description)
            .descriptionEditable(false)
            .build()
        actions.add(actionGetProjectIvy)

        val actionAuthor = GuidedAction.Builder(context)
            .id(ACTION_ID_ABOUT)
            .title(R.string.setting_about_title)
            .description(R.string.setting_about_description)
            .descriptionEditable(false)
            .build()
        actions.add(actionAuthor)
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_ID_CLEAR_CACHE -> {
                clearExternalCache(requireContext())
                Toast.makeText(
                    context,
                    "Cache cleared!",
                    Toast.LENGTH_LONG
                ).show()
                println("Cache cleared!")
            }

            ACTION_ID_GET_PROJECTIVY -> {
                val packageName = "com.spocky.projengmenu"
                try {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        "market://details?id=$packageName".toUri()))
                } catch (_: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        "https://play.google.com/store/apps/details?id=$packageName".toUri()))
                }
                println("Projectivy PlayStore page opened!")
            }

            ACTION_ID_ABOUT -> {
                startActivity(Intent(Intent.ACTION_VIEW, "https://github.com/Dak0r/Movie-Art-For-Projectivy-Plugin/".toUri()))
                println("Website Opened!")
            }
        }
    }

    companion object {
        private const val ACTION_ID_CLEAR_CACHE = 1L
        private const val ACTION_ID_GET_PROJECTIVY = 2L
        private const val ACTION_ID_ABOUT = 3L
    }

    fun clearExternalCache(context: Context) {
        val externalCacheDir: File? = context.externalCacheDir
        if (externalCacheDir != null && externalCacheDir.isDirectory) {
            deleteDirectoryContents(externalCacheDir)
        }
    }

    private fun deleteDirectoryContents(dir: File): Boolean {
        val files = dir.listFiles() ?: return false

        for (file in files) {
            if (file.isDirectory) {
                deleteDirectoryContents(file)
            }
            file.delete()
        }
        return dir.delete()
    }
}
