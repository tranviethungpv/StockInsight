package com.example.stockinsight.data.socket

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor() {
    private val sockets: MutableMap<String, Socket> = mutableMapOf()

    fun addSocket(name: String, url: String = "http://128.199.186.7") {
        if (sockets.containsKey(name)) {
            closeSocket(name)
        }
        try {
            val socket = IO.socket(url)
            sockets[name] = socket
            socket.connect()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    fun emit(name: String, event: String, data: JSONObject) {
        sockets[name]?.emit(event, data)
    }

    fun on(name: String, event: String, listener: Emitter.Listener) {
        sockets[name]?.on(event, listener)
    }

    fun off(name: String, event: String, listener: Emitter.Listener) {
        sockets[name]?.off(event, listener)
    }

    fun closeSocket(name: String) {
        sockets[name]?.let {
            it.disconnect()
            it.close()
            sockets.remove(name)
        }
    }

    fun reopenSocket(name: String, url: String = "http://128.199.186.7") {
        closeSocket(name)
        addSocket(name, url)
    }

    fun closeAllSockets() {
        sockets.values.forEach {
            it.disconnect()
            it.close()
        }
        sockets.clear()
    }
}
