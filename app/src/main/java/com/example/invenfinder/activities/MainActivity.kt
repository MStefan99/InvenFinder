package com.example.invenfinder.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.invenfinder.R
import com.example.invenfinder.adapters.ComponentAdapter
import com.example.invenfinder.data.Component
import com.example.invenfinder.data.Location
import java.sql.DriverManager
import java.sql.SQLException


class MainActivity : Activity() {
	private val componentAdapter = ComponentAdapter(this)

	private lateinit var vComponentList: RecyclerView
	private lateinit var vSearchField: EditText
	private lateinit var vRefreshLayout: SwipeRefreshLayout


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		vComponentList = findViewById(R.id.component_list)
		vSearchField = findViewById(R.id.search_field)
		vRefreshLayout = findViewById(R.id.refresh_layout)

		vSearchField.doOnTextChanged { text, _, _, _ -> componentAdapter.filter(text.toString()) }

		vRefreshLayout.setOnRefreshListener {
			loadData()
		}

		vComponentList.addItemDecoration(
			DividerItemDecoration(
				this,
				DividerItemDecoration.VERTICAL
			)
		)
		vComponentList.layoutManager = LinearLayoutManager(this)
		vComponentList.adapter = componentAdapter
	}


	override fun onResume() {
		super.onResume()

		loadData()
	}


	private fun loadData() {
		val prefs = getSharedPreferences("credentials", MODE_PRIVATE)
		val url = prefs.getString("url", null)
		val username = prefs.getString("username", null)
		val password = prefs.getString("password", null)

		if (url == null || username == null || password == null) {
			startActivity(Intent(this, ConnectionActivity::class.java))
			return
		}

		vRefreshLayout.isRefreshing = true

		Thread {
			try {
				val conn =
					DriverManager.getConnection(
						"jdbc:mariadb://${url}:3306/invenfinder",
						username,
						password
					)
				val st = conn.createStatement();
				val res = st.executeQuery("select * from components")

				val components = ArrayList<Component>()

				while (res.next()) {
					components.add(
						Component(
							res.getInt("id"),
							res.getString("name"),
							res.getString("description"),
							Location(
								res.getInt("drawer"),
								res.getInt("col"),
								res.getInt("row")
							),
							res.getInt("amount")
						)
					)
				}
				st.close()
				conn.close()

				runOnUiThread {
					vRefreshLayout.isRefreshing = false
					componentAdapter.setComponents(components)
					componentAdapter.filter(vSearchField.text.toString())
				}
			} catch (e: SQLException) {
				runOnUiThread {
					Toast.makeText(
						this, "Unable to connect, " +
								"please check your connection and credentials",
						Toast.LENGTH_LONG
					).show()

					vRefreshLayout.isRefreshing = false
				}
			}
		}.start()
	}
}
