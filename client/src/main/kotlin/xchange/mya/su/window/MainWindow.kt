package xchange.mya.su.window

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.gui2.table.TableModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import xchange.mya.su.Api
import xchange.mya.su.entity.Client

class MainModel(
	private val ui: TextGUIThread,
	private val api: Api,
	private val client: Client,
) {
	private val scope = CoroutineScope(Dispatchers.IO)

	fun loadTransactions(table: TableModel<String>) = scope.launch(Dispatchers.IO) {
		val history = api.transactionHistory()
		ui.invokeAndWait {
			for (i in history) {
				table.addRow(
					i.id.toString(),
					i.sender.toString(),
					i.recipient.toString(),
					i.currency,
					i.amount.toString(),
				)
			}
		}
	}

	fun loadBalance(table: TableModel<String>) = scope.launch(Dispatchers.IO) {
		val balance = api.clientBalance(client.id)
		ui.invokeAndWait {
			for (i in balance) {
				table.addRow(
					i.currency,
					i.amount.toString(),
				)
			}
		}
	}

	fun destroy() {
		scope.cancel()
	}
}

fun mainMenu(
	onTransaction: () -> Unit,
	onExchange: () -> Unit,
): Panel {
	val panel = Panel(LinearLayout(Direction.HORIZONTAL))

	val transactionButton = Button("Transaction", onTransaction)
	val exchangeButton = Button("Exchange", onExchange)

	panel.addComponent(transactionButton)
	panel.addComponent(exchangeButton)

	return panel
}

private fun transactionTable(
	model: MainModel,
): Table<String> {
	val table = Table<String>("ID", "Sender", "Recipient", "Currency", "Amount")
	model.loadTransactions(table.tableModel)
	return table
}

private fun balanceTable(
	model: MainModel,
): Table<String> {
	val table = Table<String>("Currency", "Amount")
	model.loadBalance(table.tableModel)
	return table
}

fun mainWindow(
	gui: MultiWindowTextGUI,
	api: Api,
	client: Client,
) {
	val model = MainModel(gui.guiThread, api, client)

	val window = BasicWindow()
	val panel = Panel(GridLayout(2))

	val leftPanel = Panel()
	val rightPanel = Panel()

	// left panel
	transactionTable(model).addTo(leftPanel)
	leftPanel.setLayoutData(
		GridLayout.createLayoutData(
			GridLayout.Alignment.FILL,
			GridLayout.Alignment.FILL,
			true,
			false,
		)
	)

	// right panel
	balanceTable(model).addTo(rightPanel)
	rightPanel.setLayoutData(
		GridLayout.createLayoutData(
			GridLayout.Alignment.FILL,
			GridLayout.Alignment.FILL,
			true,
			true,
		)
	)

	// menu panel
	val menu = mainMenu(
		onTransaction = {
			transactionWindow(gui, api, client)
		},
		onExchange = {

		},
	)
	menu.setLayoutData(
		GridLayout.createLayoutData(
			GridLayout.Alignment.END,
			GridLayout.Alignment.END,
			true,
			false,
			2,
			1,
		)
	)

	leftPanel
		.withBorder(Borders.singleLine("Transactions"))
		.addTo(panel)

	rightPanel
		.withBorder(Borders.singleLine("Balance"))
		.addTo(panel)

	menu.addTo(panel)

	window.setHints(arrayListOf(Window.Hint.EXPANDED))
	window.component = panel

	gui.addWindowAndWait(window)
	model.destroy()
}