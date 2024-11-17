package xchange.mya.su

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import xchange.mya.su.window.loginWindow
import xchange.mya.su.window.mainWindow
import xchange.mya.su.window.registerWindow
import java.security.Security


fun main() {
	Security.addProvider(BouncyCastleProvider())

	val terminal = DefaultTerminalFactory().createTerminal()
	val screen = TerminalScreen(terminal)
	screen.cursorPosition = null
	screen.startScreen()

	val api = Api()
	val gui = MultiWindowTextGUI(screen)

	loginWindow(
		gui,
		onRegister = {
			registerWindow(gui, api)
		},
		onLogin = { client ->
			mainWindow(gui, api, client)
		}
	)
	screen.stopScreen()
}