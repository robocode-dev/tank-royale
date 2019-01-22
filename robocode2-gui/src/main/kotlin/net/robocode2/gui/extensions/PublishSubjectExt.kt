package net.robocode2.gui.extensions

import io.reactivex.subjects.PublishSubject
import java.awt.EventQueue

object PublishSubjectExt {

    fun PublishSubject<Unit>.invokeLater(runnable: ((Unit) -> Unit)) {
        subscribe { EventQueue.invokeLater { runnable.invoke(Unit) } }
    }
}