package com.example.invenfinder.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.invenfinder.R
import com.example.invenfinder.components.TitleBar
import com.example.invenfinder.utils.AppColors
import com.example.invenfinder.utils.ItemManager
import com.example.invenfinder.utils.Preferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ConnectionActivity : ComponentActivity() {
	private enum class ConnectionState {
		SignedOut,
		SigningIn,
		SignedIn,
		SigningOut
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		var authenticated by mutableStateOf(ConnectionState.SignedOut)

		MainScope().launch {
			try {
				authenticated = if (ItemManager.testAuthAsync().await())
					ConnectionState.SignedIn
				else ConnectionState.SignedOut
			} catch (e: Exception) {
				Toast.makeText(this@ConnectionActivity, e.message, Toast.LENGTH_LONG).show()
			}
		}

		setContent {
			Column {
				TitleBar(stringResource(R.string.connection_settings))
				ConnectionForm(authenticated, onConnectionUpdate = { a -> authenticated = a })
			}
		}
	}

	@Composable
	private fun ConnectionForm(
		authenticated: ConnectionState,
		onConnectionUpdate: (ConnectionState) -> Unit
	) {
		val prefs = rememberSaveable { Preferences.getPreferences() }
		var url by rememberSaveable { mutableStateOf(prefs.getString("url", null) ?: "") }
		var username by rememberSaveable { mutableStateOf(prefs.getString("username", null) ?: "") }
		var password by rememberSaveable { mutableStateOf("") }
		val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
			backgroundColor = AppColors.auto.background,
			textColor = AppColors.auto.foreground,
			unfocusedBorderColor = AppColors.auto.light,
			focusedBorderColor = AppColors.auto.muted,
			placeholderColor = AppColors.auto.light
		)

		Column(
			modifier = Modifier
				.padding(16.dp, 0.dp, 16.dp, 0.dp)
				.fillMaxWidth()
		) {
			Text(stringResource(R.string.url), color = AppColors.auto.foreground)
			OutlinedTextField(
				url,
				placeholder = { Text(stringResource(R.string.url_hint)) },
				onValueChange = { u -> url = u },
				colors = textFieldColors,
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp, bottom = 16.dp)
			)

			Text(stringResource(R.string.username), color = AppColors.auto.foreground)
			OutlinedTextField(
				username,
				placeholder = { Text(stringResource(R.string.user_hint)) },
				onValueChange = { u -> username = u },
				colors = textFieldColors,
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp, bottom = 16.dp)
			)

			Text(stringResource(R.string.password), color = AppColors.auto.foreground)
			OutlinedTextField(
				password,
				placeholder = { Text(stringResource(R.string.password_hint)) },
				visualTransformation = PasswordVisualTransformation(),
				onValueChange = { p -> password = p },
				colors = textFieldColors,
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp, bottom = 16.dp)
			)

			Row(verticalAlignment = Alignment.CenterVertically) {
				Spacer(modifier = Modifier.weight(1f))
				Text(
					stringResource(getConnectionResource(authenticated)),
					color = AppColors.auto.muted,
					modifier = Modifier.padding(end = 16.dp)
				)
				if (authenticated == ConnectionState.SignedOut) {
					Button(
						onClick = {
							MainScope().launch {
								try {
									onConnectionUpdate(ConnectionState.SigningIn)
									val res = ItemManager.loginAsync(url, username, password).await()
									if (res) {
										onConnectionUpdate(ConnectionState.SignedIn)
									} else {
										Toast.makeText(
											this@ConnectionActivity,
											R.string.login_failed,
											Toast.LENGTH_LONG
										)
											.show()
										onConnectionUpdate(ConnectionState.SignedOut)
									}
								} catch (e: Exception) {
									Toast.makeText(this@ConnectionActivity, e.message, Toast.LENGTH_LONG).show()
								}
							}
						},
						colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.auto.accent)
					) {
						Text(stringResource(R.string.sign_in), color = AppColors.auto.onAccent)
					}
				} else if (authenticated == ConnectionState.SignedIn) {
					Button(
						onClick = {
							MainScope().launch {
								try {
									onConnectionUpdate(ConnectionState.SigningIn)
									val res = ItemManager.logoutAsync().await()
									if (res) {
										onConnectionUpdate(ConnectionState.SignedOut)
									} else {
										Toast.makeText(
											this@ConnectionActivity,
											R.string.logout_failed,
											Toast.LENGTH_LONG
										)
											.show()
										onConnectionUpdate(ConnectionState.SignedIn)
									}
								} catch (e: Exception) {
									Toast.makeText(this@ConnectionActivity, e.message, Toast.LENGTH_LONG).show()
								}
							}
						},
						colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.auto.accent)
					) {
						Text(stringResource(R.string.sign_out), color = AppColors.auto.onAccent)
					}
				}
			}
		}
	}

	private fun getConnectionResource(state: ConnectionState): Int {
		return when (state) {
			ConnectionState.SignedOut -> R.string.not_signed_in
			ConnectionState.SigningIn -> R.string.signing_in_e
			ConnectionState.SignedIn -> R.string.signed_in
			ConnectionState.SigningOut -> R.string.signing_out_e
		}
	}
}
