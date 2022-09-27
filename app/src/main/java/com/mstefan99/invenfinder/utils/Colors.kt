package com.mstefan99.invenfinder.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val black = Color(0xff000000)
private val white = Color(0xffffffff)
private val darkGray = Color(0xff444444)
private val gray = Color(0xff888888)
private val lightGray = Color(0xffcccccc)
private val purpleLight = Color(0xff3b00a2)
private val purpleDark = Color(0xff703cb2)

data class ColorScheme(
	val background: Color,
	val foreground: Color,
	val muted: Color,
	val light: Color,
	val accent: Color,
	val onAccent: Color
)

object AppColors {
	val light = ColorScheme(
		background = white,
		foreground = black,
		muted = darkGray,
		light = lightGray,
		accent = purpleLight,
		onAccent = white
	)

	val dark = ColorScheme(
		background = black,
		foreground = white,
		muted = gray,
		light = darkGray,
		accent = purpleDark,
		onAccent = white
	)

	val auto: ColorScheme
		@Composable
		get() = if (isSystemInDarkTheme()) dark else light
}
