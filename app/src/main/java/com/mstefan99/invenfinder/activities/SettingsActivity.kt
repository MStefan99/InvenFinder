package com.mstefan99.invenfinder.activities

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
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mstefan99.invenfinder.components.TitleBar
import com.mstefan99.invenfinder.utils.AppColors

private enum class SettingsOptionType {
	Button
}

private class SettingsOption(
	val type: SettingsOptionType = SettingsOptionType.Button,
	val text: String = "",
	val onClick: () -> Unit = {}
)

private class SettingsSection(
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
			Column {
				TitleBar("Settings")
				SettingsContent(defaultSettings)
			}
		}
	}
}

@Composable
private fun SettingsContent(sections: List<SettingsSection>) {
	LazyColumn(
		modifier = Modifier.padding(all = 16.dp)
	) {
		items(sections) { section ->
			Text(
				section.title, fontSize = 24.sp,
				color = AppColors.auto.foreground,
				modifier = Modifier.padding(bottom = 8.dp)
			)
			Column {
				for (option in section.options) {
					TextButton(
						content = {
							Text(option.text, color = AppColors.auto.foreground)
							Spacer(modifier = Modifier.weight(1f))
						}, onClick = option.onClick,
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
		}
	}
}
