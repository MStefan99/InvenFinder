package com.example.invenfinder.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class SettingsOptionType {
	Button
}

class SettingsOption(
	val type: SettingsOptionType = SettingsOptionType.Button,
	val text: String = "",
	val onClick: () -> Unit = {}
)

class SettingsSection(
	val title: String = "",
	val options: Array<SettingsOption> = arrayOf()
)

class SettingsActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val defaultSettings = listOf(
			SettingsSection(
				"Database",
				arrayOf(SettingsOption(SettingsOptionType.Button, "Credentials") {
					startActivity(Intent(this@SettingsActivity, ConnectionActivity::class.java))
				})
			)
		)

		setContent {
			SettingsContent(defaultSettings)
		}
	}
}

@Composable
fun SettingsContent(sections: List<SettingsSection>) {
	LazyColumn(
		modifier = Modifier.padding(16.dp)
	) {
		items(sections) { section ->
			Text(section.title, style = MaterialTheme.typography.h4)
			Column {
				for (option in section.options) {
					TextButton(
						content = {
							Text(option.text)
							Spacer(modifier = Modifier.weight(1f))
						}, onClick = option.onClick,
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
		}
	}
}
