package com.danielkorgel.projectivy.plugin.cinemaglow


import android.app.Activity
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
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.BackgroundPickerHelper

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
        PreferencesManager.lastWallpaper = ""
        // Custom App Background Toggle
        val fallbackBackground = GuidedAction.Builder(context)
            .id(ACTION_ID_FALLBACK_BG_TOGGLE)
            .title(R.string.setting_custom_bg_title)
            .description(PreferencesManager.fallbackBackground.text)
            .descriptionEditable(false)
            .build()
        actions.add(fallbackBackground)

        // Pick from Gallery
        val actionPickGallery = GuidedAction.Builder(context)
            .id(ACTION_ID_PICK_GALLERY)
            .title(R.string.setting_custom_bg_pick_gallery_title)
            .description(R.string.setting_custom_bg_pick_gallery_description)
            .descriptionEditable(false)
            .build()
        actions.add(actionPickGallery)

        // Get Projectivy
        val actionGetProjectIvy = GuidedAction.Builder(context)
            .id(ACTION_ID_GET_PROJECTIVY)
            .title(R.string.setting_projectivy_title)
            .description(R.string.setting_projectivy_description)
            .descriptionEditable(false)
            .build()
        actions.add(actionGetProjectIvy)

        // About
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
            ACTION_ID_FALLBACK_BG_TOGGLE -> {
                val newState: PreferencesManager.FallbackBackground = when (PreferencesManager.fallbackBackground) {
                    PreferencesManager.FallbackBackground.PopularMoviesAndShows -> {
                        PreferencesManager.FallbackBackground.DynamicColors
                    }

                    PreferencesManager.FallbackBackground.DynamicColors -> {
                        PreferencesManager.FallbackBackground.CustomBackground
                    }

                    else -> {
                        PreferencesManager.FallbackBackground.PopularMoviesAndShows
                    }
                }
                PreferencesManager.fallbackBackground = newState
                // Update the action description
                action.description = newState.text
                notifyActionChanged(findActionPositionById(ACTION_ID_FALLBACK_BG_TOGGLE))
                println("Fallback background changed: $newState")
            }

            ACTION_ID_PICK_GALLERY -> {
                openGalleryPicker()
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

    private fun openGalleryPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No gallery app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToggleAction() {
        val position = findActionPositionById(ACTION_ID_FALLBACK_BG_TOGGLE)
        if (position >= 0) {
            val action = actions[position]
            action.description = PreferencesManager.fallbackBackground.text
            notifyActionChanged(position)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val context = requireContext()
                val file = BackgroundPickerHelper.copyBackgroundFromUri(context, uri)
                if (file != null && file.exists()) {
                    // Delete old file if it exists to avoid cluttering cache
                    PreferencesManager.customAppBackgroundName?.let { oldName ->
                        val oldFile = BackgroundPickerHelper.getCustomBackgroundFile(context, oldName)
                        if (oldFile.exists() && oldFile.name != file.name) {
                            oldFile.delete()
                        }
                    }
                    
                    PreferencesManager.customAppBackgroundName = file.name
                    PreferencesManager.fallbackBackground = PreferencesManager.FallbackBackground.CustomBackground
                    updateToggleAction()
                    Toast.makeText(context, R.string.custom_bg_set_success, Toast.LENGTH_LONG).show()
                    println("Custom background set from gallery: ${file.absolutePath}")
                } else {
                    Toast.makeText(context, "Failed to copy file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val ACTION_ID_GET_PROJECTIVY = 1L
        private const val ACTION_ID_ABOUT = 2L
        private const val ACTION_ID_FALLBACK_BG_TOGGLE = 3L
        private const val ACTION_ID_PICK_GALLERY = 4L

        private const val REQUEST_CODE_PICK_IMAGE = 100
    }
}
