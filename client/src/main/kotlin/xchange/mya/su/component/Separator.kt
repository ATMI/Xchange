package xchange.mya.su.component

import com.googlecode.lanterna.gui2.Direction
import com.googlecode.lanterna.gui2.GridLayout
import com.googlecode.lanterna.gui2.Panel
import com.googlecode.lanterna.gui2.Separator

fun separator(span: Int): Separator {
	return Separator(Direction.HORIZONTAL)
		.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(span))
}

fun Panel.addSeparator(span: Int) {
	separator(span).addTo(this)
}