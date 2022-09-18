package com.example.invenfinder.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(widthDp = 320)
@Composable
fun TitleBar(name: String = "Title", content: @Composable () -> Unit = {}) {
	TopAppBar(
		modifier = Modifier.padding(bottom = 16.dp),
		backgroundColor = MaterialTheme.colors.primary
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp)
		) {
			Text(
				name,
				fontSize = 24.sp,
				color = MaterialTheme.colors.onPrimary
			)
			Spacer(modifier = Modifier.weight(1f))
			Row {
				content()
			}
		}
	}
}
