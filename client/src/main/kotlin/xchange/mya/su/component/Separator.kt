package xchange.mya.su.component

import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Separator

fun separator(): Separator {
	return Separator(Direction.HORIZONTAL)
		.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2))
}

fun Panel.addSeparator() {
	separator().addTo(this)
}