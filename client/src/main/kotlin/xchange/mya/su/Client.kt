package xchange.mya.su

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import xchange.mya.su.auth.authWindow
import java.security.Security

data class Client(
	val id: Long,
	val privateKey: Ed25519PrivateKeyParameters,
	val publicKey: Ed25519PublicKeyParameters,
)

fun main() {
	Security.addProvider(BouncyCastleProvider())
	val api = Api()

	val terminal = DefaultTerminalFactory().createTerminal()
	val screen = TerminalScreen(terminal)
	screen.cursorPosition = null
	screen.startScreen()

	val gui = MultiWindowTextGUI(screen)
	val client = authWindow(gui, api)

	if (client != null) {

	}

	screen.stopScreen()
}