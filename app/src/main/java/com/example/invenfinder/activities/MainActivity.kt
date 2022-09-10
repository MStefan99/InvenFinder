package com.example.invenfinder.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.invenfinder.R
import com.example.invenfinder.adapters.ItemAdapter
import com.example.invenfinder.utils.ItemManager
import com.example.invenfinder.utils.Preferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MainActivity : Activity() {
	private val itemAdapter = ItemAdapter(this)

	private lateinit var vList: RecyclerView
	private lateinit var vSearch: EditText
	private lateinit var vRefresh: SwipeRefreshLayout
	private lateinit var vSettings: ImageView
	private lateinit var vAdd: ImageView


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		vList = findViewById(R.id.component_list)
		vSearch = findViewById(R.id.search_field)
		vRefresh = findViewById(R.id.refresh_layout)
		vSettings = findViewById(R.id.settings_button)
		vAdd = findViewById(R.id.submit_button)

		val prefs = getSharedPreferences("credentials", MODE_PRIVATE)
		Preferences.setPreferences(prefs)

		vAdd.setOnClickListener {
			startActivity(Intent(this, ItemEditActivity::class.java).apply {
				putExtra("action", ItemEditActivity.Action.ADD)
			})
		}

		vSettings.setOnClickListener {
			startActivity(Intent(this, SettingsActivity::class.java))
		}

		vSearch.doOnTextChanged { text, _, _, _ -> itemAdapter.filter(text.toString()) }

		vRefresh.setOnRefreshListener {
			loadData()
		}

		vList.addItemDecoration(
			DividerItemDecoration(
				this,
				DividerItemDecoration.VERTICAL
			)
		)
		vList.layoutManager = LinearLayoutManager(this)
		vList.adapter = itemAdapter
	}


	override fun onResume() {
		super.onResume()
		loadData()
	}


	private fun loadData() {
		vRefresh.isRefreshing = true

		MainScope().launch {
			try {
				val components = ItemManager.getAllAsync().await()
				itemAdapter.setComponents(components)
				itemAdapter.filter(vSearch.text.toString())
			} catch (e: Error) {
				Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
			}

			vRefresh.isRefreshing = false
		}
	}
}
