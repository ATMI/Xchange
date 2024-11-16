package xchange.mya.su.window

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xchange.mya.su.Api
import xchange.mya.su.entity.Client
import xchange.mya.su.entity.Transaction
import xchange.mya.su.response.CurrencySymbol
import java.util.regex.Pattern

class TransactionModel(
	private val ui: TextGUIThread,
	private val api: Api,
	private val client: Client,
) {
	private val scope = CoroutineScope(Dispatchers.IO)

	fun loadCurrencyList(comboBox: ComboBox<CurrencySymbol>) = scope.launch(Dispatchers.IO) {
		val currencyList = api.currencyList()
		ui.invokeAndWait {
			comboBox.clearItems()
			for (i in currencyList) {
				comboBox.addItem(i)
			}
		}
	}

	fun transact(
		progressBar: ProgressBar,
		currency: CurrencySymbol?,
		recipientValue: String,
		amountValue: String,
		onDone: () -> Unit,
	) = scope.launch(Dispatchers.IO) {
		val recipient = recipientValue.toLong()

		val pattern = Pattern.compile("^(\\d*)\\.?(\\d{0,2})\$")
		val match = pattern.matcher(amountValue)
		if (!match.find()) {
			return@launch
		}
		progressBar.value++

		val wholeGroup = match.group(1)
		val centsGroup = match.group(2)

		val whole = wholeGroup.toLongOrNull() ?: 0
		var cents = centsGroup.toLongOrNull() ?: 0

		if (cents > 0 && centsGroup.length == 1) {
			cents *= 10
		}
		val amount = 100 * whole + cents

		val synAck = api.transactionSyn()
		progressBar.value++

		val transaction = Transaction(
			synAck.id,
			client.id,
			recipient,
			currency!!.id,
			amount,
			synAck.timestamp,
		)

		transaction.sign(client.privateKey)
		progressBar.value++

		api.transactionAck(transaction)
		progressBar.value++

		ui.invokeAndWait(onDone)
	}
}

private fun progressWindow() {

}

fun transactionWindow(
	gui: MultiWindowTextGUI,
	api: Api,
	client: Client,
) {
	val model = TransactionModel(gui.guiThread, api, client)
	val window = BasicWindow("Transaction")
	val panel = Panel(GridLayout(2))

	fun separator(): Separator {
		return Separator(Direction.HORIZONTAL)
			.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2))
	}

	fun addSeparator() {
		separator().addTo(panel)
	}

	addSeparator()

	Label("Sender").addTo(panel)
	Label(client.id.toString())
		.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(1))
		.addTo(panel)

	addSeparator()

	Label("Recipient").addTo(panel)
	val recipientValue = TextBox()
		.setValidationPattern(Pattern.compile("\\d+"))
		.addTo(panel)

	addSeparator()

	Label("Currency").addTo(panel)
	val currencyValue = ComboBox<CurrencySymbol>()
		.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(1))
		.addTo(panel)
	model.loadCurrencyList(currencyValue)

	addSeparator()

	Label("Amount").addTo(panel)
	val amountValue = TextBox()
		.setValidationPattern(Pattern.compile("^\\d*\\.?\\d{0,2}$"))
		.addTo(panel)

	addSeparator()

	val buttons = Panel(LinearLayout(Direction.HORIZONTAL))
	Button(MessageDialogButton.Cancel.toString()) {
		window.close()
	}.addTo(buttons)

	Button(MessageDialogButton.OK.toString()) {
		val currency = currencyValue.selectedItem
		val recipient = recipientValue.text
		val amount = amountValue.text

		val progressSeparator = separator()
		val progressBar = ProgressBar(0, 4)
			.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2))

		panel.addComponent(13, progressBar)
		panel.addComponent(14, progressSeparator)

		model.transact(progressBar, currency, recipient, amount) {
			recipientValue.isEnabled = false
			currencyValue.isEnabled = false
			amountValue.isEnabled = false

			buttons.removeAllComponents()
			Button(MessageDialogButton.Close.toString()) {
				window.close()
			}
				.addTo(buttons)
				.takeFocus()
//
//			window.close()
		}
	}.addTo(buttons)

	buttons
		.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(2))
		.addTo(panel)

	window.component = panel
	window.setCloseWindowWithEscape(true)
	window.setHints(arrayListOf(Window.Hint.CENTERED))
	gui.addWindowAndWait(window)
}