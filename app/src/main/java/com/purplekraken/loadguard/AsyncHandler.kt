package com.purplekraken.loadguard

import android.os.Handler
import android.os.HandlerThread

typealias AsyncRunnable = () -> Unit

object AsyncHandler {
    private val handlerThread = HandlerThread("AsyncHandler")
    private val handler: Handler

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    fun post(r: AsyncRunnable) {
        handler.post(r)
    }
}
