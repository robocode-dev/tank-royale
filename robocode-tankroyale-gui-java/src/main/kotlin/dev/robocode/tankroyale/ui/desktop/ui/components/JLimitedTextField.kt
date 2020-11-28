package dev.robocode.tankroyale.ui.desktop.ui.components

import javax.swing.JTextField

class JLimitedTextField(columns: Int, text: String? = null) : JTextField(JTextFieldLimit(columns), text, columns)