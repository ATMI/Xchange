package xchange.mya.su.window

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.gui2.table.TableModel
import kotlinx.coroutines.*
import xchange.mya.su.Api
import xchange.mya.su.entity.Client
import kotlin.math.absoluteValue

class MainModel(
	private val ui: TextGUIThread,
	private val api: Api,
	private val client: Client,
) {
	private val scope = CoroutineScope(Dispatchers.IO)

	private fun amountString(amount: Long) : String {
		return "%8d.%02d".format(amount / 100, amount.absoluteValue % 100)
	}

	fun loadTransactions(table: TableModel<String>) = scope.launch(Dispatchers.IO) {
		val history = api.transactionHistory()
		ui.invokeAndWait {
			table.clear()
			for (i in history) {
				table.addRow(
					i.id.toString(),
					i.sender.toString(),
					i.recipient.toString(),
					i.currency,
					amountString(i.amount),
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
					amountString(i.amount),
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
	val panel = Panel(LinearLayout(Direction.VERTICAL))
	val content = Panel(GridLayout(2))

	val transactionTable = transactionTable(model)
	val balanceTable = balanceTable(model)
	val menu = mainMenu(
		onTransaction = {
			transactionWindow(gui, api, client)
			model.loadTransactions(transactionTable.tableModel)
		},
		onExchange = {

		},
	)

	transactionTable.setLayoutData(
		GridLayout.createLayoutData(
			GridLayout.Alignment.FILL,
			GridLayout.Alignment.FILL,
			true,
			true
		)
	)
	balanceTable.setLayoutData(
		GridLayout.createLayoutData(
			GridLayout.Alignment.FILL,
			GridLayout.Alignment.BEGINNING,
			false,
			false,
		)
	)
	content.addComponent(transactionTable.withBorder(Borders.singleLine("Transactions")))
	content.addComponent(balanceTable.withBorder(Borders.singleLine("Balance")))

	content.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow))
	menu.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.End, LinearLayout.GrowPolicy.None))
	panel.addComponent(content)
	panel.addComponent(menu)

	window.setHints(arrayListOf(Window.Hint.EXPANDED))
	window.setCloseWindowWithEscape(true)
	window.component = panel

	gui.addWindowAndWait(window)
	model.destroy()
}