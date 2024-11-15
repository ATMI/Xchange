package xchange.mya.su.window

import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.ProgressBar
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import xchange.mya.su.Api
import xchange.mya.su.Keyguard
import xchange.mya.su.entity.Client

fun registerWindow(
	gui: WindowBasedTextGUI,
	api: Api,
): Client? {
	var client: Client? = null
	val window = BasicWindow()
	val progressBar = ProgressBar(0, 3)

	window.setHints(arrayListOf(Window.Hint.CENTERED))
	window.component = progressBar
	window.title = "Registration"

	val scope = CoroutineScope(Dispatchers.IO)
	scope.launch {
		val keys = Keyguard.createPair()
		progressBar.value = 1

		val registration = api.clientRegister(keys.public)
		progressBar.value = 2

		Keyguard.save(registration.id, keys.private)
		progressBar.value = 3

		val result = Client(
			id = registration.id,
			privateKey = keys.private,
			publicKey = keys.public,
		)

		client = result
		gui.guiThread.invokeAndWait {
			window.close()
		}
	}

	gui.addWindowAndWait(window)
	scope.cancel()
	return client
}