package xchange.mya.su.window

import com.googlecode.lanterna.gui2.*
import com.googlecode.lanterna.gui2.table.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import xchange.mya.su.Api
import xchange.mya.su.entity.Client

//fun mainMenuBar(): MenuBar {
//	val menuBar = MenuBar()
//
//	val transactionMenu = Menu("Transaction")
//	val transactionNewItem = MenuItem("New")
//	transactionMenu.add(transactionNewItem)
//	menuBar.add(transactionMenu)
//
//	return menuBar
//}

private fun transactionTable(
	gui: MultiWindowTextGUI,
	api: Api,
	scope: CoroutineScope,
): Table<String> {
	val table = Table<String>("ID", "Sender", "Recipient", "Currency", "Amount")

	scope.launch(Dispatchers.IO) {
		val history = api.transactionHistory()
		gui.guiThread.invokeAndWait {
			val model = table.tableModel
			for (i in history.transactions) {
				model.addRow(
					i.id.toString(),
					i.sender.toString(),
					i.recipient.toString(),
					i.currency,
					i.amount.toString(),
				)
			}
		}
	}

	return table
}

private fun balanceTable(
	gui: MultiWindowTextGUI,
	api: Api,
	client: Client,
	scope: CoroutineScope,
): Table<String> {
	val table = Table<String>("Currency", "Amount")

	scope.launch(Dispatchers.IO) {
		val balance = api.clientBalance(client.id)
		gui.guiThread.invokeAndWait {
			val model = table.tableModel
			for (i in balance.currencies) {
				model.addRow(
					i.currency,
					i.amount.toString(),
				)
			}
		}
	}

	return table
}

fun mainWindow(
	gui: MultiWindowTextGUI,
	api: Api,
	client: Client,
) {
	val scope = CoroutineScope(Dispatchers.IO)
	val window = BasicWindow()
	val panel = Panel(LinearLayout(Direction.HORIZONTAL))

	val leftPanel = Panel()
	val rightPanel = Panel()

	// left panel
	val transactionTable = transactionTable(gui, api, scope)
	leftPanel.addComponent(transactionTable)
	leftPanel.setLayoutData(
		LinearLayout.createLayoutData(
			LinearLayout.Alignment.Fill,
			LinearLayout.GrowPolicy.CanGrow,
		)
	)

	// right panel
	val balanceTable = balanceTable(gui, api, client, scope)
	rightPanel.addComponent(balanceTable)
	rightPanel.setLayoutData(
		LinearLayout.createLayoutData(
			LinearLayout.Alignment.Fill,
			LinearLayout.GrowPolicy.CanGrow,
		)
	)

	val leftBorder = leftPanel.withBorder(Borders.singleLine("Transactions"))
	val rightBorder = rightPanel.withBorder(Borders.singleLine("Balance"))

	panel.addComponent(leftBorder)
	panel.addComponent(rightBorder)

	window.setHints(arrayListOf(Window.Hint.EXPANDED))
	window.component = panel

	gui.addWindowAndWait(window)
	scope.cancel()
}