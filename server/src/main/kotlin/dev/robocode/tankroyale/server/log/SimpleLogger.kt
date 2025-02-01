package dev.robocode.tankroyale.server.log

import org.fusesource.jansi.Ansi.ansi
import org.slf4j.event.Level.ERROR
import org.slf4j.event.Level.WARN
import org.slf4j.event.Level.INFO
import org.slf4j.event.Level.DEBUG
import org.slf4j.event.Level.TRACE
import org.slf4j.Logger
import org.slf4j.ILoggerFactory
import org.slf4j.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

class SimpleLogger(private val loggerName: String) : Logger {

    // Flags defining which level of messages is enabled/disabled
    var traceEnabled = false
    var debugEnabled = false
    var infoEnabled = true
    var warnEnabled = true
    var errorEnabled = true

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    }

    // The method that performs logging of messages
    private fun log(level: String, message: String, throwable: Throwable? = null) {

        if (level == ERROR.name) {
            logError(message, throwable)
            return
        }

        val ansiColor = when (level) {
            TRACE.name -> ansi().fgCyan()
            DEBUG.name -> ansi().fgBlue()
            INFO.name -> ansi().fgGreen()
            WARN.name -> ansi().fgYellow()
            else -> ansi().fgDefault()
        }

        val levelInfo = " ${ansiColor}[$level]${ansi().fgDefault()} "

        buildString {
            append(dateFormat.format(Date()))
            append(levelInfo)
            append(message)
        }.also {
            println(it)
            throwable?.printStackTrace(System.out)
        }
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        val levelInfo = " [${ERROR.name}] "
        buildString {
            append(ansi().fgBrightRed())
            append(dateFormat.format(Date()))
            append(levelInfo)
            append(message)
        }.also {
            System.err.println(it)
            throwable?.printStackTrace(System.err)
            System.err.println(ansi().fgDefault().toString())
        }
    }

    override fun getName(): String = loggerName

    // Level checks
    override fun isTraceEnabled(): Boolean = traceEnabled
    override fun isTraceEnabled(marker: Marker?): Boolean = traceEnabled
    override fun isDebugEnabled(): Boolean = debugEnabled
    override fun isDebugEnabled(marker: Marker?): Boolean = debugEnabled
    override fun isInfoEnabled(): Boolean = infoEnabled
    override fun isInfoEnabled(marker: Marker?): Boolean = infoEnabled
    override fun isWarnEnabled(): Boolean = warnEnabled
    override fun isWarnEnabled(marker: Marker?): Boolean = warnEnabled
    override fun isErrorEnabled(): Boolean = errorEnabled
    override fun isErrorEnabled(marker: Marker?): Boolean = errorEnabled

    // Basic logging methods
    override fun trace(msg: String) { if (traceEnabled) log(TRACE.name, msg) }
    override fun debug(msg: String) { if (debugEnabled) log(DEBUG.name, msg) }
    override fun info(msg: String) { if (infoEnabled) log(INFO.name, msg) }
    override fun warn(msg: String) { if (warnEnabled) log(WARN.name, msg) }
    override fun error(msg: String) { if (errorEnabled) log(ERROR.name, msg) }

    // Logging with throwables
    override fun trace(msg: String, t: Throwable) { if (traceEnabled) log(TRACE.name, msg, t) }
    override fun debug(msg: String, t: Throwable) { if (debugEnabled) log(DEBUG.name, msg, t) }
    override fun info(msg: String, t: Throwable) { if (infoEnabled) log(INFO.name, msg, t) }
    override fun warn(msg: String, t: Throwable) { if (warnEnabled) log(WARN.name, msg, t) }
    override fun error(msg: String, t: Throwable) { if (errorEnabled) log(ERROR.name, msg, t) }

    // Marker-based logging (ignored in this simple implementation)
    override fun trace(marker: Marker?, msg: String) = trace(msg)
    override fun debug(marker: Marker?, msg: String) = debug(msg)
    override fun info(marker: Marker?, msg: String) = info(msg)
    override fun warn(marker: Marker?, msg: String) = warn(msg)
    override fun error(marker: Marker?, msg: String) = error(msg)

    // Marker-based logging with throwables
    override fun trace(marker: Marker?, msg: String, t: Throwable) = trace(msg, t)
    override fun debug(marker: Marker?, msg: String, t: Throwable) = debug(msg, t)
    override fun info(marker: Marker?, msg: String, t: Throwable) = info(msg, t)
    override fun warn(marker: Marker?, msg: String, t: Throwable) = warn(msg, t)
    override fun error(marker: Marker?, msg: String, t: Throwable) = error(msg, t)

    // Format-based logging
    override fun trace(format: String, arg: Any?) = trace(format(format, arg))
    override fun debug(format: String, arg: Any?) = debug(format(format, arg))
    override fun info(format: String, arg: Any?) = info(format(format, arg))
    override fun warn(format: String, arg: Any?) = warn(format(format, arg))
    override fun error(format: String, arg: Any?) = error(format(format, arg))

    override fun trace(format: String, arg1: Any?, arg2: Any?) = trace(format(format, arg1, arg2))
    override fun debug(format: String, arg1: Any?, arg2: Any?) = debug(format(format, arg1, arg2))
    override fun info(format: String, arg1: Any?, arg2: Any?) = info(format(format, arg1, arg2))
    override fun warn(format: String, arg1: Any?, arg2: Any?) = warn(format(format, arg1, arg2))
    override fun error(format: String, arg1: Any?, arg2: Any?) = error(format(format, arg1, arg2))

    override fun trace(format: String, vararg arguments: Any?) = trace(format(format, *arguments))
    override fun debug(format: String, vararg arguments: Any?) = debug(format(format, *arguments))
    override fun info(format: String, vararg arguments: Any?) = info(format(format, *arguments))
    override fun warn(format: String, vararg arguments: Any?) = warn(format(format, *arguments))
    override fun error(format: String, vararg arguments: Any?) = error(format(format, *arguments))

    // Marker-based format logging
    override fun trace(marker: Marker?, format: String, arg: Any?) = trace(format, arg)
    override fun debug(marker: Marker?, format: String, arg: Any?) = debug(format, arg)
    override fun info(marker: Marker?, format: String, arg: Any?) = info(format, arg)
    override fun warn(marker: Marker?, format: String, arg: Any?) = warn(format, arg)
    override fun error(marker: Marker?, format: String, arg: Any?) = error(format, arg)

    override fun trace(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = trace(format, arg1, arg2)
    override fun debug(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = debug(format, arg1, arg2)
    override fun info(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = info(format, arg1, arg2)
    override fun warn(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = warn(format, arg1, arg2)
    override fun error(marker: Marker?, format: String, arg1: Any?, arg2: Any?) = error(format, arg1, arg2)

    override fun trace(marker: Marker?, format: String, vararg arguments: Any?) = trace(format, *arguments)
    override fun debug(marker: Marker?, format: String, vararg arguments: Any?) = debug(format, *arguments)
    override fun info(marker: Marker?, format: String, vararg arguments: Any?) = info(format, *arguments)
    override fun warn(marker: Marker?, format: String, vararg arguments: Any?) = warn(format, *arguments)
    override fun error(marker: Marker?, format: String, vararg arguments: Any?) = error(format, *arguments)

    private fun format(format: String, vararg arguments: Any?): String {
        val regex = Regex("\\{}") // Matches "{}"
        var argumentIndex = 0

        // Replace each "{}" with the next argument
        return regex.replace(format) {
            if (argumentIndex < arguments.size) {
                arguments[argumentIndex++].toString()
            } else {
                "{}" // If no more arguments are left, keep "{}" as is
            }
        }
    }
}

class SimpleLoggerFactory : ILoggerFactory {
    private val loggerMap = ConcurrentHashMap<String, Logger>()

    override fun getLogger(name: String): Logger {
        val logger = SimpleLogger(name)
        return loggerMap.getOrPut(name) { logger }
    }
}
