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
import com.danielkorgel.projectivy.plugin.cinemaglow.helpers.ImagePickerHelper

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
        // Custom App Background Toggle
        val isCustomBgEnabled = PreferencesManager.useCustomAppBackground
        val actionCustomBgToggle = GuidedAction.Builder(context)
            .id(ACTION_ID_CUSTOM_BG_TOGGLE)
            .title(R.string.setting_custom_bg_title)
            .description(if (isCustomBgEnabled) R.string.setting_custom_bg_enabled else R.string.setting_custom_bg_disabled)
            .descriptionEditable(false)
            .build()
        actions.add(actionCustomBgToggle)

        // Pick from Gallery
        val actionPickGallery = GuidedAction.Builder(context)
            .id(ACTION_ID_PICK_GALLERY)
            .title(R.string.setting_custom_bg_pick_gallery_title)
            .description(R.string.setting_custom_bg_pick_gallery_description)
            .descriptionEditable(false)
            .build()
        actions.add(actionPickGallery)

        // Clear Cache
        val actionClearCache = GuidedAction.Builder(context)
            .id(ACTION_ID_CLEAR_CACHE)
            .title(R.string.setting_clear_cache_title)
            .description(R.string.setting_clear_cache_description)
            .descriptionEditable(false)
            .build()
        actions.add(actionClearCache)

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
            ACTION_ID_CUSTOM_BG_TOGGLE -> {
                val newState = !PreferencesManager.useCustomAppBackground
                PreferencesManager.useCustomAppBackground = newState
                // Update the action description
                action.description = getString(
                    if (newState) R.string.setting_custom_bg_enabled 
                    else R.string.setting_custom_bg_disabled
                )
                notifyActionChanged(findActionPositionById(ACTION_ID_CUSTOM_BG_TOGGLE))
                println("Custom background toggled: $newState")
            }

            ACTION_ID_PICK_GALLERY -> {
                openGalleryPicker()
            }

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

    private fun openGalleryPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No gallery app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToggleAction() {
        val position = findActionPositionById(ACTION_ID_CUSTOM_BG_TOGGLE)
        if (position >= 0) {
            val action = actions[position]
            action.description = getString(
                if (PreferencesManager.useCustomAppBackground) R.string.setting_custom_bg_enabled
                else R.string.setting_custom_bg_disabled
            )
            notifyActionChanged(position)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val context = requireContext()
                val file = ImagePickerHelper.copyImageFromUri(context, uri)
                if (file != null && file.exists()) {
                    // Delete old file if it exists
                    if(PreferencesManager.customAppBackgroundName != null) {
                        val oldFile = ImagePickerHelper.getCustomBackgroundFile(
                            context,
                            PreferencesManager.customAppBackgroundName!!
                        )
                        if (oldFile.exists()) {
                            oldFile.delete()
                        }
                    }
                    PreferencesManager.customAppBackgroundName = file.name
                    PreferencesManager.useCustomAppBackground = true
                    updateToggleAction()
                    Toast.makeText(context, R.string.custom_bg_set_success, Toast.LENGTH_SHORT).show()
                    println("Custom background set from gallery: ${file.absolutePath}")
                } else {
                    Toast.makeText(context, "Failed to copy image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val ACTION_ID_CLEAR_CACHE = 1L
        private const val ACTION_ID_GET_PROJECTIVY = 2L
        private const val ACTION_ID_ABOUT = 3L
        private const val ACTION_ID_CUSTOM_BG_TOGGLE = 4L
        private const val ACTION_ID_PICK_GALLERY = 5L

        private const val REQUEST_CODE_PICK_IMAGE = 100
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
