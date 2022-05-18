package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.invenfinder.R
import com.example.invenfinder.adapters.ComponentAdapter
import com.example.invenfinder.data.Component
import java.sql.DriverManager


class MainActivity : Activity() {
	private val componentAdapter = ComponentAdapter(this)

	private lateinit var componentList: RecyclerView
	private lateinit var searchField: EditText
	private lateinit var refreshLayout: SwipeRefreshLayout


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main);

		componentList = findViewById(R.id.component_list)
		searchField = findViewById(R.id.search_field)
		refreshLayout = findViewById(R.id.refresh_layout)

		loadData()

		searchField.doOnTextChanged { text, _, _, _ -> componentAdapter.filter(text.toString()) }

		refreshLayout.setOnRefreshListener {
			loadData()
		}

		componentList.addItemDecoration(
			DividerItemDecoration(
				this,
				DividerItemDecoration.VERTICAL
			)
		)
		componentList.layoutManager = LinearLayoutManager(this)
		componentList.adapter = componentAdapter
	}


	private fun loadData() {
		Thread {
			refreshLayout.isRefreshing = true

			val conn =
				DriverManager.getConnection(
					"jdbc:mariadb://192.168.1.11:3306/invenfinder",
					"root",
					"test"
				)
			val st = conn.createStatement();
			val res = st.executeQuery("select * from components")

			val components = ArrayList<Component>()

			while (res.next()) {
				components.add(
					Component(
						res.getString("name"),
						res.getString("description"),
						res.getInt("drawer"),
						res.getInt("col"),
						res.getInt("row"),
						res.getInt("amount")
					)
				)
			}
			st.close()
			conn.close()

			runOnUiThread {
				refreshLayout.isRefreshing = false
				componentAdapter.setComponents(components)
			}
		}.start()
	}
}
