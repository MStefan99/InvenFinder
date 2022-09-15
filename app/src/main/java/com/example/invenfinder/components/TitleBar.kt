package com.example.invenfinder.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(widthDp = 320)
@Composable
fun TitleBar(name: String = "Title") {
	Box(modifier = Modifier.padding(bottom = 16.dp)) {
		Surface(
			color = MaterialTheme.colors.primary,
			modifier = Modifier
				.fillMaxWidth()
		) {
			Text(
				name,
				fontSize = 24.sp,
				modifier = Modifier
					.padding(vertical = 8.dp, horizontal = 16.dp)
			)
		}
	}
}
