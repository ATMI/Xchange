package xchange.mya.su.auth

import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters


fun createKeyWindow(
	gui: WindowBasedTextGUI,
): Pair<Ed25519PrivateKeyParameters, Ed25519PublicKeyParameters>? {
	val registerMessage = MessageDialogBuilder().apply {
		setTitle("New Wallet")
		setText("Create a new wallet in one click?")
		addButton(MessageDialogButton.No)
		addButton(MessageDialogButton.Yes)
	}.build()

	val button = registerMessage.showDialog(gui)
	if (button != MessageDialogButton.Yes) {
		return null
	}

	val pair = Keyguard.createPair()
	Keyguard.save(pair.first)

	return pair
}

fun rememberIdWindow(
	gui: WindowBasedTextGUI,
	id: Long,
) {
	val rememberMessage = MessageDialogBuilder().apply {
		setTitle("ID")
		setText("Remember your ID: $id")
		addButton(MessageDialogButton.OK)
	}.build()

	rememberMessage.showDialog(gui)
}