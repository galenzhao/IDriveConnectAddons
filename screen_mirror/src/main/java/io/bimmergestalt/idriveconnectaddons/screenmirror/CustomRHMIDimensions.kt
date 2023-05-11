package io.bimmergestalt.idriveconnectaddons.screenmirror

import io.bimmergestalt.idriveconnectkit.RHMIDimensions

class CustomRHMIDimensions(val original: RHMIDimensions, val settingsViewer: AppSettingsViewer): RHMIDimensions {
	override val rhmiWidth: Int
		get() = settingsViewer[AppSettings.KEYS.DIMENSIONS_RHMI_WIDTH].toIntOrNull() ?: 980
	override val rhmiHeight: Int
		get() = settingsViewer[AppSettings.KEYS.DIMENSIONS_RHMI_HEIGHT].toIntOrNull() ?: 540
	override val marginLeft: Int
		get() = settingsViewer[AppSettings.KEYS.DIMENSIONS_MARGIN_LEFT].toIntOrNull() ?: 90
	override val paddingLeft: Int
		get() = settingsViewer[AppSettings.KEYS.DIMENSIONS_PADDING_LEFT].toIntOrNull() ?: 90
	override val paddingTop: Int
		get() = settingsViewer[AppSettings.KEYS.DIMENSIONS_PADDING_TOP].toIntOrNull() ?: 67
	override val marginRight: Int
		get() = settingsViewer[AppSettings.KEYS.DIMENSIONS_MARGIN_RIGHT].toIntOrNull() ?: 5
}
class UpdatingSidebarRHMIDimensions(val fullscreen: RHMIDimensions, val isWidescreen: () -> Boolean):
		RHMIDimensions {
	override val rhmiWidth: Int
		get() = fullscreen.rhmiWidth
	override val rhmiHeight: Int
		get() = fullscreen.rhmiHeight
	override val marginLeft: Int
		get() = fullscreen.marginLeft
	override val paddingLeft: Int
		get() = fullscreen.paddingLeft
	override val paddingTop: Int
		get() = fullscreen.paddingTop
	override val marginRight: Int
		get() = if (isWidescreen() || fullscreen.rhmiWidth < 900) { fullscreen.marginRight } else {
			(fullscreen.rhmiWidth * 0.37).toInt()
		}
}