package xchange.mya.su.window

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xchange.mya.su.Api
import xchange.mya.su.Money
import xchange.mya.su.Money.toMoney
import xchange.mya.su.component.addSeparator
import xchange.mya.su.component.separator
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
		val amount = amountValue.toMoney()
		progressBar.value++

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

fun transactionWindow(
	gui: MultiWindowTextGUI,
	api: Api,
	client: Client,
) {
	val model = TransactionModel(gui.guiThread, api, client)
	val window = BasicWindow("Transaction")
	val panel = Panel(GridLayout(3))

	panel.addSeparator(3)

	Label("Sender").addTo(panel)
	Label(client.id.toString())
		.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(2))
		.addTo(panel)

	panel.addSeparator(3)

	Label("Recipient").addTo(panel)
	val recipientValue = TextBox()
		.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(2))
		.setValidationPattern(Pattern.compile("\\d+"))
		.addTo(panel)

	panel.addSeparator(3)

	Label("Amount").addTo(panel)
	val amountValue = TextBox()
		.setValidationPattern(Pattern.compile(Money.PATTERN))
		.addTo(panel)
	val currencyValue = ComboBox<CurrencySymbol>()
		.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(1))
		.addTo(panel)
	model.loadCurrencyList(currencyValue)

	panel.addSeparator(3)

	val buttons = Panel(LinearLayout(Direction.HORIZONTAL))
	Button(MessageDialogButton.Cancel.toString()) {
		window.close()
	}.addTo(buttons)

	Button(MessageDialogButton.OK.toString()) {
		val currency = currencyValue.selectedItem
		val recipient = recipientValue.text
		val amount = amountValue.text

		val progressSeparator = separator(3)
		val progressBar = ProgressBar(0, 4)
			.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(3))

		panel.addComponent(11, progressBar)
		panel.addComponent(12, progressSeparator)

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
		.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(3))
		.addTo(panel)

	window.component = panel
	window.setCloseWindowWithEscape(true)
	window.setHints(arrayListOf(Window.Hint.CENTERED))
	gui.addWindowAndWait(window)
}