package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.invenfinder.R
import com.example.invenfinder.utils.ItemManager
import com.example.invenfinder.utils.Preferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ConnectionActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_connection)

		val vURL: EditText = findViewById(R.id.url_input)
		val vUsername: EditText = findViewById(R.id.username_input)
		val vPassword: EditText = findViewById(R.id.password_input)
		val vLoginButton: Button = findViewById(R.id.login_button)
		val vLogoutButton: Button = findViewById(R.id.logout_button)
		val vTestLabel: TextView = findViewById(R.id.status_label)

		fun resetState() {
			vLoginButton.isEnabled = false
			vLogoutButton.isEnabled = false

			vLoginButton.setTextColor(getColorFromAttr(R.attr.colorMuted))
			vLogoutButton.setTextColor(getColorFromAttr(R.attr.colorMuted))

			vTestLabel.setTextColor(getColorFromAttr(R.attr.colorMuted))
			vTestLabel.setText(R.string.checking_e)
		}

		fun setState(connected: Boolean) {
			vLoginButton.isEnabled = !connected
			vLogoutButton.isEnabled = connected

			if (connected) {
				vLogoutButton.setTextColor(getColorFromAttr(R.attr.colorOnAccent))
				vTestLabel.setTextColor(getColorFromAttr(R.attr.colorSuccess))
				vTestLabel.setText(R.string.signed_in)
			} else {
				vLoginButton.setTextColor(getColorFromAttr(R.attr.colorOnAccent))
				vTestLabel.setTextColor(getColorFromAttr(R.attr.colorError))
				vTestLabel.setText(R.string.not_signed_in)
			}
		}

		val prefs = Preferences.getPreferences()
		vURL.setText(prefs.getString("url", null))
		vUsername.setText(prefs.getString("username", null))

		MainScope().launch {
			try {
				resetState()
				setState(ItemManager.testAuthAsync().await())
			} catch (e: Exception) {
				setState(false)
				Toast.makeText(this@ConnectionActivity, e.message, Toast.LENGTH_LONG).show()
			}
		}

		vLoginButton.setOnClickListener {
			vTestLabel.setTextColor(getColorFromAttr(R.attr.colorMuted))
			vTestLabel.setText(R.string.signing_in_e)

			MainScope().launch {
				try {
					resetState()
					setState(
						ItemManager.loginAsync(
							vURL.text.toString(),
							vUsername.text.toString(),
							vPassword.text.toString()
						).await()
					)
				} catch (e: Exception) {
					setState(false)
					Toast.makeText(this@ConnectionActivity, e.message, Toast.LENGTH_LONG).show()
				}
			}
		}


		vLogoutButton.setOnClickListener {
			vTestLabel.setTextColor(getColorFromAttr(R.attr.colorMuted))
			vTestLabel.setText(R.string.signing_out_e)

			MainScope().launch {
				try {
					resetState()
					setState(!ItemManager.logoutAsync().await())
				} catch (e: Exception) {
					setState(true)
					Toast.makeText(this@ConnectionActivity, e.message, Toast.LENGTH_LONG).show()
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
