package xchange.mya.su

import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import kotlinx.coroutines.runBlocking
import org.bouncycastle.jce.provider.BouncyCastleProvider
import xchange.mya.su.entity.Transaction
import java.security.Security


fun main() {
	Security.addProvider(BouncyCastleProvider())
	val api = Api()

	val terminal = DefaultTerminalFactory().createTerminal()
	val screen = TerminalScreen(terminal)
	screen.cursorPosition = null
	screen.startScreen()

	val gui = MultiWindowTextGUI(screen)
	val pair = Keyguard.createPair()

//	val encoded = ProtoBuf.encodeToByteArray(Ed25519PublicKeySerializer, pair.public)
//	val decoded = ProtoBuf.decodeFromByteArray(Ed25519PublicKeySerializer, encoded)

	runBlocking {
		val registration = api.register(pair.public)

		val synAck = api.transactionSyn()
		val transaction = Transaction(
			id = synAck.id,
			sender = registration.id,
			recipient = 2L,
			currency = 1L,
			amount = 1L,
			timestamp = synAck.timestamp
		)
		transaction.sign(pair.private)
		api.transactionAck(transaction)
	}

	screen.stopScreen()
}