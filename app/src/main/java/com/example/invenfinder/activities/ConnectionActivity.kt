package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.invenfinder.R
import com.example.invenfinder.utils.ComponentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class ConnectionActivity : Activity() {
	private lateinit var vURL: EditText
	private lateinit var vUsername: EditText
	private lateinit var vPassword: EditText
	private lateinit var vTestButton: Button
	private lateinit var vConnectButton: Button
	private lateinit var vTestResult: TextView


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_connection)

		vURL = findViewById(R.id.url_input)
		vUsername = findViewById(R.id.username_input)
		vPassword = findViewById(R.id.password_input)
		vTestButton = findViewById(R.id.test_button)
		vConnectButton = findViewById(R.id.save_button)
		vTestResult = findViewById(R.id.test_result)

		val prefs = getSharedPreferences("credentials", MODE_PRIVATE)
		vURL.setText(prefs.getString("url", null))
		vUsername.setText(prefs.getString("username", null))
		vPassword.setText(prefs.getString("password", null))

		vTestButton.setOnClickListener {
			vTestResult.setTextColor(getColorFromAttr(R.attr.colorMuted))
			vTestResult.setText(R.string.testing_e)

			MainScope().launch {
				val reachable = ComponentManager.testConnectionAsync(
					ComponentManager.ConnectionOptions(
						vURL.text.toString(),
						vUsername.text.toString(),
						vPassword.text.toString()
					)
				)

				if (reachable.await()) {
					vTestResult.setTextColor(getColorFromAttr(R.attr.colorSuccess))
					vTestResult.setText(R.string.connection_successful)
				} else {
					vTestResult.setTextColor(getColorFromAttr(R.attr.colorError))
					vTestResult.setText(R.string.connection_failed)
				}
			}
		}


		vConnectButton.setOnClickListener {
			vTestResult.setTextColor(getColorFromAttr(R.attr.colorMuted))
			vTestResult.setText(R.string.testing_e)

			MainScope().launch {
				val reachable = ComponentManager.testConnectionAsync(
					ComponentManager.ConnectionOptions(
						vURL.text.toString(),
						vUsername.text.toString(),
						vPassword.text.toString()
					)
				)

				if (reachable.await()) {
					vTestResult.setTextColor(getColorFromAttr(R.attr.colorSuccess))
					vTestResult.setText(R.string.saved)

					val editor = prefs.edit()
					editor.putString("url", vURL.text.toString())
					editor.putString("username", vUsername.text.toString())
					editor.putString("password", vPassword.text.toString())
					editor.apply()
				} else {
					vTestResult.setTextColor(getColorFromAttr(R.attr.colorError))
					vTestResult.setText(R.string.connection_failed)
				}
			}
		}
	}


	private fun getColorFromAttr(
		attrColor: Int,
		resolveRefs: Boolean = true,
		typedValue: TypedValue = TypedValue()
	): Int {
		theme.resolveAttribute(attrColor, typedValue, resolveRefs)
		return typedValue.data
	}
}
