package xchange.mya.su.auth

import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import kotlinx.coroutines.runBlocking
import xchange.mya.su.Api
import xchange.mya.su.Client

fun authWindow(
	gui: WindowBasedTextGUI,
	api: Api,
): Client? {
	var keyPair = Keyguard.load()
	return if (keyPair == null) {
		keyPair = createKeyWindow(gui) ?: return null

		val id = runBlocking { api.register(keyPair.second) } ?: return null
		rememberIdWindow(gui, id)

		Client(id, keyPair.first, keyPair.second)
	} else {
		val id = loginWindow(gui) ?: return null
		Client(id, keyPair.first, keyPair.second)
	}
}