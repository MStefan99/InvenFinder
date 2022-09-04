package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.invenfinder.R
import com.example.invenfinder.utils.ItemManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ConnectionActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_connection)

		val vURL: EditText = findViewById(R.id.url_input)
		val vUsername: EditText = findViewById(R.id.username_input)
		val vPassword: EditText = findViewById(R.id.password_input)
		val vTestButton: Button = findViewById(R.id.test_button)
		val vConnectButton: Button = findViewById(R.id.save_button)
		val vTestLabel: TextView = findViewById(R.id.test_label)

		val prefs = getSharedPreferences("credentials", MODE_PRIVATE)
		vURL.setText(prefs.getString("url", null))
		vUsername.setText(prefs.getString("username", null))
		vPassword.setText(prefs.getString("password", null))

		vTestButton.setOnClickListener {
			vTestLabel.setTextColor(getColorFromAttr(R.attr.colorMuted))
			vTestLabel.setText(R.string.testing_e)

			MainScope().launch {
				val reachable = ItemManager.loginAsync(
					vURL.text.toString(),
					vUsername.text.toString(),
					vPassword.text.toString()
				)

				if (reachable.await()) {
					vTestLabel.setTextColor(getColorFromAttr(R.attr.colorSuccess))
					vTestLabel.setText(R.string.connection_successful)
				} else {
					vTestLabel.setTextColor(getColorFromAttr(R.attr.colorError))
					vTestLabel.setText(R.string.connection_failed)
				}
			}
		}


		vConnectButton.setOnClickListener {
			vTestLabel.setTextColor(getColorFromAttr(R.attr.colorMuted))
			vTestLabel.setText(R.string.testing_e)

			MainScope().launch {
				val reachable = ItemManager.loginAsync(
					vURL.text.toString(),
					vUsername.text.toString(),
					vPassword.text.toString()
				)

				if (reachable.await()) {
					vTestLabel.setTextColor(getColorFromAttr(R.attr.colorSuccess))
					vTestLabel.setText(R.string.saved)

					val editor = prefs.edit()
					editor.putString("url", vURL.text.toString())
					editor.putString("username", vUsername.text.toString())
					editor.putString("password", vPassword.text.toString())
					editor.apply()
				} else {
					vTestLabel.setTextColor(getColorFromAttr(R.attr.colorError))
					vTestLabel.setText(R.string.connection_failed)
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
