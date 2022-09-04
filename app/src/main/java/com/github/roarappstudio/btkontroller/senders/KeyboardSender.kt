package com.github.roarappstudio.btkontroller.senders

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.util.Log
import android.view.KeyEvent
import com.github.roarappstudio.btkontroller.reports.KeyboardReport

@Suppress("MemberVisibilityCanBePrivate")
open class KeyboardSender(
    val hidDevice: BluetoothHidDevice,
    val host: BluetoothDevice
) {
    val keyboardReport = KeyboardReport()
    protected open fun sendKeys() {
        Log.d(TAG, "keyboardReport.bytes ${keyboardReport.bytes[0]} ${keyboardReport.bytes[1]} ${keyboardReport.bytes[2]}")

        if (!hidDevice.sendReport(host, KeyboardReport.ID, keyboardReport.bytes)) {
            Log.e(TAG, "Report wasn't sent")
        }
    }
    protected open fun customSender(modifier_checked_state: Int) {
        sendKeys()
        if (modifier_checked_state == 0) sendNullKeys()
        else {
            keyboardReport.key1 = 0.toByte()
            sendKeys()
        }
    }
    protected open fun setModifiers(event: KeyEvent) {
        if (event.isShiftPressed) keyboardReport.leftShift = true
        if (event.isAltPressed) keyboardReport.leftAlt = true
        if (event.isCtrlPressed) keyboardReport.leftControl = true
        if (event.isMetaPressed) keyboardReport.leftGui = true
    }
    fun sendNullKeys() {
        keyboardReport.bytes.fill(0)
        if (!hidDevice.sendReport(host, KeyboardReport.ID, keyboardReport.bytes)) {
            Log.e(TAG, "Report wasn't sent")
        }
    }
    fun keyEventHandler(
        keyEventCode: Int,
        event: KeyEvent,
        modifier_checked_state: Int,
        keyCode: Int
    ): Boolean {
        val byteKey = KeyboardReport.KeyEventMap[keyEventCode]
        if (byteKey != null) {
            setModifiers(event)
            if (event.keyCode == KeyEvent.KEYCODE_AT || event.keyCode == KeyEvent.KEYCODE_POUND || event.keyCode == KeyEvent.KEYCODE_STAR) {
                keyboardReport.leftShift = true
            }
            keyboardReport.key1 = byteKey.toByte()
            customSender(modifier_checked_state)
            return true
        } else {
            return false
        }
    }
    fun sendKeyboard(keyCode: Int, event: KeyEvent, modifier_checked_state: Int): Boolean {
        return keyEventHandler(event.keyCode, event, modifier_checked_state, keyCode)
    }
    companion object {
        const val TAG = "KeyboardSender"
    }
}