package com.example.invenfinder.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invenfinder.utils.AppColors

@Preview(widthDp = 320)
@Composable
fun TitleBar(name: String = "Title", content: @Composable () -> Unit = {}) {
	TopAppBar(backgroundColor = AppColors.auto.accent) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp)
		) {
			Text(
				name,
				fontSize = 24.sp,
				color = AppColors.auto.onAccent
			)
			Spacer(modifier = Modifier.weight(1f))
			Row {
				content()
			}
		}
	}
}
