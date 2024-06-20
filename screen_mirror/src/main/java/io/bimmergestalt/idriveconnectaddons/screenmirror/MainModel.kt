package io.bimmergestalt.idriveconnectaddons.screenmirror

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.bimmergestalt.idriveconnectaddons.lib.LiveDataHelpers.map
import io.bimmergestalt.idriveconnectkit.RHMIDimensions
import androidx.lifecycle.*

import io.bimmergestalt.idriveconnectaddons.lib.CarCapabilities

class MainModel(appContext: Context, val carCapabilities: Map<String, String?>): ViewModel() {
    class Factory(val appContext: Context): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val carCapabilities = HashMap<String, String?>()
            val preloadedCapabilities = CarCapabilities(appContext).getCapabilities()
            val capabilities = if (preloadedCapabilities.containsKey("hmi.display-width")) {
                preloadedCapabilities
            } else {
//                carConnection.rhmi_getCapabilities("", 255).map { it.key as String to it.value as String }.toMap()
                carCapabilities
            }
            AppSettings.loadSettings(appContext)
            return MainModel(appContext, capabilities) as T
        }
    }
    private val origDimensions = RHMIDimensions.create(carCapabilities)

    val settingsViewer = AppSettingsViewer()
    val origRhmiWidth = ""+origDimensions.rhmiWidth
    val origRhmiHeight = ""+origDimensions.rhmiHeight
    val origMarginLeft = ""+origDimensions.marginLeft
    val origMarginRight = ""+origDimensions.marginRight
    val origPaddingLeft = ""+origDimensions.paddingLeft
    val origPaddingTop = ""+origDimensions.paddingTop

    val rhmiWidth = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_RHMI_WIDTH)
    val rhmiHeight = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_RHMI_HEIGHT)
    val marginLeft = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_MARGIN_LEFT)
    val marginRight = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_MARGIN_RIGHT)
    val paddingLeft = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_PADDING_LEFT)
    val paddingTop = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_PADDING_TOP)
    val autopermission = StringLiveSetting(appContext, AppSettings.KEYS.AUTO_PERMISSION)
    val minFrameTime = StringLiveSetting(appContext, AppSettings.KEYS.MINFRAMETIME)
    val jpgQuality = StringLiveSetting(appContext, AppSettings.KEYS.jpgQuality)

    val mirroringState = ScreenMirrorProvider.state
    val mirroringStateText: LiveData<Context.() -> String> = ScreenMirrorProvider.state.map({getString(R.string.lbl_status_not_ready)}) {
        when (it) {
            MirroringState.NOT_ALLOWED -> {
                { getString(R.string.lbl_status_not_allowed) }
            }
            MirroringState.WAITING -> {
                { getString(R.string.lbl_status_waiting) }
            }
            MirroringState.ACTIVE -> {
                { getString(R.string.lbl_status_active) }
            }
            else -> {
                { getString(R.string.lbl_status_not_ready) }
            }
        }
    }
}