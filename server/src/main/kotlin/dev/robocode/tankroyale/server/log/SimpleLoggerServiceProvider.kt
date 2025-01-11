package dev.robocode.tankroyale.server.log

import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

class SimpleLoggerServiceProvider : SLF4JServiceProvider {
    private val loggerFactory: ILoggerFactory = SimpleLoggerFactory()

    override fun getLoggerFactory(): ILoggerFactory = loggerFactory

    override fun getMarkerFactory(): IMarkerFactory {
        TODO("Not yet implemented")
    }

    override fun getMDCAdapter(): MDCAdapter {
        TODO("Not yet implemented")
    }

    override fun getRequestedApiVersion(): String = "2.0.99" // Adjust as needed

    override fun initialize() {
        // Initialization code if needed
    }
}
