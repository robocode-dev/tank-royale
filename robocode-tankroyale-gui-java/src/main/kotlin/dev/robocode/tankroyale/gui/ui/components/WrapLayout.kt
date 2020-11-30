package dev.robocode.tankroyale.gui.ui.components

import java.awt.Container
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

// From: https://tips4java.wordpress.com/2008/11/06/wrap-layout/

/**
 * FlowLayout subclass that fully supports wrapping of components.
 * <p>
 * Constructs a new `WrapLayout` with a left alignment and a default
 * 5-unit horizontal and vertical gap.
 */
class WrapLayout : FlowLayout() {
    /**
     * Returns the preferred dimensions for this layout given the
     * *visible* components in the specified target container.
     * @param target the component which needs to be laid out
     * @return the preferred dimensions to lay out the
     * subcomponents of the specified container
     */
    override fun preferredLayoutSize(target: Container): Dimension {
        return layoutSize(target, true)
    }

    /**
     * Returns the minimum dimensions needed to layout the *visible*
     * components contained in the specified target container.
     * @param target the component which needs to be laid out
     * @return the minimum dimensions to lay out the
     * subcomponents of the specified container
     */
    override fun minimumLayoutSize(target: Container): Dimension {
        val minimum = layoutSize(target, false)
        minimum.width -= hgap + 1
        return minimum
    }

    /**
     * Returns the minimum or preferred dimension needed to layout the target
     * container.
     *
     * @param target target to get layout size for
     * @param preferred should preferred size be calculated
     * @return the dimension to layout the target container
     */
    private fun layoutSize(target: Container, preferred: Boolean): Dimension {
        synchronized(target.treeLock) {

            //  Each row must fit with the width allocated to the container.
            //  When the container width = 0, the preferred width of the container
            //  has not yet been calculated so lets ask for the maximum.
            var container = target
            while (container.size.width == 0 && container.parent != null) {
                container = container.parent
            }
            var targetWidth = container.size.width
            if (targetWidth == 0) targetWidth = Int.MAX_VALUE
            val insets = target.insets
            val horizontalInsetsAndGap = insets.left + insets.right + hgap * 2
            val maxWidth = targetWidth - horizontalInsetsAndGap

            //  Fit components into the allowed width
            val dim = Dimension(0, 0)
            var rowWidth = 0
            var rowHeight = 0
            val nMembers = target.componentCount
            for (i in 0 until nMembers) {
                val m = target.getComponent(i)
                if (m.isVisible) {
                    val d = if (preferred) m.preferredSize else m.minimumSize

                    //  Can't add the component to current row. Start a new row.
                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight)
                        rowWidth = 0
                        rowHeight = 0
                    }

                    //  Add a horizontal gap for all components after the first
                    if (rowWidth != 0) {
                        rowWidth += hgap
                    }
                    rowWidth += d.width
                    rowHeight = rowHeight.coerceAtLeast(d.height)
                }
            }
            addRow(dim, rowWidth, rowHeight)
            dim.width += horizontalInsetsAndGap
            dim.height += insets.top + insets.bottom + vgap * 2

            //	When using a scroll pane or the DecoratedLookAndFeel we need to
            //  make sure the preferred size is less than the size of the
            //  target containter so shrinking the container size works
            //  correctly. Removing the horizontal gap is an easy way to do this.
            val scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane::class.java, target)
            if (scrollPane != null && target.isValid) {
                dim.width -= hgap + 1
            }
            return dim
        }
    }

    /**
     * A new row has been completed. Use the dimensions of this row
     * to update the preferred size for the container.
     *
     * @param dim update the width and height when appropriate
     * @param rowWidth the width of the row to add
     * @param rowHeight the height of the row to add
     */
    private fun addRow(dim: Dimension, rowWidth: Int, rowHeight: Int) {
        dim.width = dim.width.coerceAtLeast(rowWidth)
        if (dim.height > 0) {
            dim.height += vgap
        }
        dim.height += rowHeight
    }
}