package xchange.mya.su.window

import com.googlecode.lanterna.gui2.ActionListBox
import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Window
import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import kotlinx.coroutines.runBlocking
import xchange.mya.su.Keyguard
import xchange.mya.su.entity.Client

fun loginWindow(
	gui: WindowBasedTextGUI,
	onRegister: () -> Client?,
): Client? {
	val clients = runBlocking { Keyguard.list() }
	val listBox = ActionListBox()
	val window = BasicWindow()

	var selected: Client? = null
	for (client in clients) {
		listBox.addItem("${client.id}") {
			window.close()
			selected = client
		}
	}

	listBox.addItem("New...") {
		window.close()
		selected = onRegister()
	}

	window.setHints(arrayListOf(Window.Hint.CENTERED))
	window.component = listBox
	window.title = "Account"

	gui.addWindowAndWait(window)
	return selected
}