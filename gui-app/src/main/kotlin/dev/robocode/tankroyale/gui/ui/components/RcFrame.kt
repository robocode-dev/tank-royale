package dev.robocode.tankroyale.gui.ui.components

import dev.robocode.tankroyale.gui.ui.UiTitles
import javax.swing.JFrame

open class RcFrame(title: String, isTitlePropertyName: Boolean = true) :
    JFrame(if (isTitlePropertyName) UiTitles.get(title) else title) {

    init {
        iconImage = RcImages.tankImage
    }
}