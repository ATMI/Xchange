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
import xchange.mya.su.request.OrderRequest
import xchange.mya.su.response.CurrencySymbol
import java.util.regex.Pattern

/*enum class OrderType {
	Buy,
	Sell;

	override fun toString(): String {
		return when (this) {
			Buy -> "Buy"
			Sell -> "Sell"
		}
	}
}*/

class OrderModel(
	private val ui: TextGUIThread,
	private val api: Api,
	private val client: Client,
) {
	private val scope = CoroutineScope(Dispatchers.IO)

	fun loadCurrencyList(vararg comboBox: ComboBox<CurrencySymbol>) = scope.launch(Dispatchers.IO) {
		val currencyList = api.currencyList()
		ui.invokeAndWait {
			comboBox.forEach {
				it.clearItems()
			}

			for (i in currencyList) {
				comboBox.forEach {
					it.addItem(i)
				}
			}
		}
	}

	fun order(
		progressBar: ProgressBar,
		base: CurrencySymbol?,
		quote: CurrencySymbol?,
		amountValue: String,
		rateValue: String,
		onDone: () -> Unit,
	) = scope.launch(Dispatchers.IO) {
		val amount = amountValue.toMoney()
		val rate = rateValue.toMoney()
		progressBar.value++

		val synAck = api.transactionSyn()
		progressBar.value++

		val transaction = Transaction(
			synAck.id,
			client.id,
			0,
			base!!.id,
			amount * rate / 100,
			synAck.timestamp
		)
		transaction.sign(client.privateKey)
		progressBar.value++

		val order = OrderRequest(
			synAck.id,
			client.id,
			base.id,
			quote!!.id,
			amount,
			rate,
			synAck.timestamp,
			transaction.signature!!,
		)
		api.orderCreate(order)
		progressBar.value++

		ui.invokeAndWait(onDone)
	}
}

fun orderWindow(
	gui: MultiWindowTextGUI,
	api: Api,
	client: Client,
) {
	val model = OrderModel(gui.guiThread, api, client)
	val window = BasicWindow("Order")
	val panel = Panel(GridLayout(3))

	panel.addSeparator(3)

	Label("Amount").addTo(panel)
	val amountValue = TextBox()
		.setValidationPattern(Pattern.compile(Money.PATTERN))
		.addTo(panel)
	val quoteValue = ComboBox<CurrencySymbol>()
		.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(2))
		.addTo(panel)

	panel.addSeparator(3)

	val quoteLabel = Label("").addTo(panel)
	val rateValue = TextBox()
		.setValidationPattern(Pattern.compile(Money.PATTERN))
		.addTo(panel)
	val baseValue = ComboBox<CurrencySymbol>()
		.setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(1))
		.addTo(panel)

	model.loadCurrencyList(baseValue, quoteValue)
	panel.addSeparator(3)

	val rateListener = ComboBox.Listener { _, _, _ ->
		quoteLabel.text = "1 ${quoteValue.selectedItem} = "
	}
	quoteValue.addListener(rateListener)

	val buttons = Panel(LinearLayout(Direction.HORIZONTAL))
	Button(MessageDialogButton.Cancel.toString()) {
		window.close()
	}.addTo(buttons)

	Button(MessageDialogButton.OK.toString()) {
		val base = baseValue.selectedItem
		val quote = quoteValue.selectedItem
		val amount = amountValue.text
		val rate = rateValue.text

		val progressSeparator = separator(3)
		val progressBar = ProgressBar(0, 4)
			.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(3))

		panel.addComponent(9, progressBar)
		panel.addComponent(10, progressSeparator)

		model.order(progressBar, base, quote, amount, rate) {
			baseValue.isEnabled = false
			quoteValue.isEnabled = false
			amountValue.isEnabled = false
			rateValue.isEnabled = false

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