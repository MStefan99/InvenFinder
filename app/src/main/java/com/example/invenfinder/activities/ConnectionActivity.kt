package com.example.invenfinder.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.invenfinder.R
import com.example.invenfinder.utils.ConnectionManager


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
		vConnectButton = findViewById(R.id.connect_button)
		vTestResult = findViewById(R.id.test_result)

		vTestButton.setOnClickListener {
			vTestResult.setTextColor(Color.parseColor("#666666"))
			vTestResult.text = "Testing..."

			ConnectionManager.testConnection(
				ConnectionManager.Options(
					vURL.text.toString(),
					vUsername.text.toString(),
					vPassword.text.toString()
				), this
			) {
				if (it) {
					vTestResult.setTextColor(Color.parseColor("#0AA300"))
					vTestResult.text = "Connection succeeded"
				} else {
					vTestResult.setTextColor(Color.parseColor("#FF4866"))
					vTestResult.text = "Connection failed"
				}
			}
		}


		vConnectButton.setOnClickListener {
			vTestResult.setTextColor(Color.parseColor("#666666"))
			vTestResult.text = "Testing..."

			ConnectionManager.testConnection(
				ConnectionManager.Options(
					vURL.text.toString(),
					vUsername.text.toString(),
					vPassword.text.toString()
				), this
			) {
				if (it) {
					val prefs = getSharedPreferences("credentials", MODE_PRIVATE)
					val editor = prefs.edit()

					editor.putString("url", vURL.text.toString())
					editor.putString("username", vUsername.text.toString())
					editor.putString("password", vPassword.text.toString())
					editor.apply()
					finish()

				} else {
					vTestResult.setTextColor(Color.parseColor("#FF4866"))
					vTestResult.text = "Connection failed"
				}
			}
		}
	}


}

