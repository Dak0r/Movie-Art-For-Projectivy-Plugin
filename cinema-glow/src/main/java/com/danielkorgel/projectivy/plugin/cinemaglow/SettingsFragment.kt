package com.danielkorgel.projectivy.plugin.cinemaglow


import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import java.io.File

class SettingsFragment: GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        return Guidance(
            getString(R.string.plugin_name),
            getString(R.string.plugin_description),
            getString(R.string.settings),
            AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_plugin)
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        val action = GuidedAction.Builder(context)
            .id(ACTION_ID_CLEAR_CACHE)
            .title(R.string.setting_clear_cache_title)
            .description(R.string.setting_clear_cache_description)
            .descriptionEditable(false)
            .build()
        actions.add(action)
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
        }
    }

    companion object {
        private const val ACTION_ID_CLEAR_CACHE = 1L
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
