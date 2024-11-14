package xchange.mya.su.auth

import com.googlecode.lanterna.gui2.WindowBasedTextGUI
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogResultValidator

private fun idDialog(): TextInputDialog {
	val builder = TextInputDialogBuilder().apply {
		title = "ID"
		description = "ID of the user to login"
		validator = TextInputDialogResultValidator { input ->
			val id = input.toLongOrNull()
			if (id == null) {
				"ID should consist of digits only"
			} else {
				null
			}
		}
	}
	return builder.build()
}

fun loginWindow(
	gui: WindowBasedTextGUI,
): Long? {
	val idDialog = idDialog()
	val id = idDialog.showDialog(gui)?.toLongOrNull()
	return id
}