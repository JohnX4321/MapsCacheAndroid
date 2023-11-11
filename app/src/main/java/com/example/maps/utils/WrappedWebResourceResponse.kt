package com.example.maps.utils

import android.os.Handler
import android.os.HandlerThread
import android.webkit.WebResourceResponse
import java.io.InputStream
import java.util.concurrent.locks.ReentrantLock


class WrappedWebResourceResponse(mime: String, encoding: String, inp: InputStream?): WebResourceResponse(mime,encoding,inp) {

    companion object {
        val handlerThread = HandlerThread("LockOwner")
        val lockHandler : Handler
        val lock = ReentrantLock()
        init {
            handlerThread.start()
            lockHandler = Handler(handlerThread.looper)
        }
    }

    private var default = true

    fun isDefault() = default

    fun modifyResource(mime: String, encoding: String, data: InputStream?) {
        this.mimeType = mime
        this.encoding = encoding
        this.data = data
        this.default = false
    }

    override fun getData(): InputStream? {
        try {
            while (lock.isLocked) {
                continue
            }
            lockHandler.post { lock.lock() }
            if (isDefault()) {
                while (isDefault()) {
                    continue
                }
                return super.getData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            lockHandler.postDelayed({
                if (lock.isLocked)
                    lock.unlock()
            },1000)
        }
        return super.getData()
    }

}