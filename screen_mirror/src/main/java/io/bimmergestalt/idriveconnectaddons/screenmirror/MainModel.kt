package io.bimmergestalt.idriveconnectaddons.screenmirror

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.bimmergestalt.idriveconnectaddons.lib.LiveDataHelpers.map
import io.bimmergestalt.idriveconnectkit.RHMIDimensions
import androidx.lifecycle.*

class MainModel(appContext: Context, val carCapabilities: LiveData<Map<String, String>>): ViewModel() {
    class Factory(val appContext: Context): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val carCapabilities = MutableLiveData<Map<String, String>>()
//            val carInfo = CarInformationObserver { capabilities ->
//                carCapabilities.postValue(capabilities)
//            }
//            if (carInfo.capabilities.isNotEmpty()) {
//                carCapabilities.value = carInfo.capabilities
//            }
            AppSettings.loadSettings(appContext)
            return MainModel(appContext, carCapabilities) as T
        }
    }
    private val origDimensions = carCapabilities.map { RHMIDimensions.create(it) }

    val settingsViewer = AppSettingsViewer()
    val origRhmiWidth = "980"
    val origRhmiHeight = "540"
    val origMarginLeft = "90"
    val origMarginRight = "5"
    val origPaddingLeft = "90"
    val origPaddingTop = "67"

    val rhmiWidth = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_RHMI_WIDTH)
    val rhmiHeight = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_RHMI_HEIGHT)
    val marginLeft = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_MARGIN_LEFT)
    val marginRight = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_MARGIN_RIGHT)
    val paddingLeft = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_PADDING_LEFT)
    val paddingTop = StringLiveSetting(appContext, AppSettings.KEYS.DIMENSIONS_PADDING_TOP)

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