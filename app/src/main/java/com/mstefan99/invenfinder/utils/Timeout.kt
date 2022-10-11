package com.mstefan99.invenfinder.utils

import android.os.Handler
import android.os.Looper


object Timeout {
	class TimeoutEvent(delay: Long, cb: () -> Unit) {
		@Volatile
		private var runnable: (() -> Unit)?

		init {
			runnable = cb
			handler.postDelayed(Runnable {
				(runnable ?: return@Runnable)()
			}, delay)
		}

		fun cancelTimeout() {
			runnable = null
		}

		companion object {
			private val handler = Handler(Looper.getMainLooper())
		}
	}

	fun setTimeout(delay: Long, cb: () -> Unit): TimeoutEvent {
		return TimeoutEvent(delay, cb)
	}

	fun clearTimeout(timeoutEvent: TimeoutEvent?) {
		timeoutEvent?.cancelTimeout()
	}
}