package com.example.gradle_demo

import org.junit.Test

import org.junit.Assert.*
import javax.net.ssl.SSLSocket

import javax.net.ssl.SSLSocketFactory




/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @kotlin.Throws(java.lang.Exception::class)
    @kotlin.jvm.JvmStatic
    @Test
    fun ddd(args: Array<String>) {
        val context: SSLContext = SSLContext.getInstance("TLS")
        context.init(null, null, null)
        val factory: javax.net.ssl.SSLSocketFactory =
            context.getSocketFactory() as javax.net.ssl.SSLSocketFactory
        val socket: javax.net.ssl.SSLSocket = factory.createSocket() as javax.net.ssl.SSLSocket
        var protocols: Array<String> = socket.getSupportedProtocols()
        println("Supported Protocols: " + protocols.size)
        for (i in protocols.indices) {
            println(" " + protocols[i])
        }
        protocols = socket.getEnabledProtocols()
        println("Enabled Protocols: " + protocols.size)
        for (i in protocols.indices) {
            println(" " + protocols[i])
        }
    }

}