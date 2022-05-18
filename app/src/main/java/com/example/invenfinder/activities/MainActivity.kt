package com.example.invenfinder.activities

import android.app.Activity
import android.os.Bundle
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

		loadData()

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


	private fun loadData() {
		Thread {
			vRefreshLayout.isRefreshing = true

			try {
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
						this, "Unable to connect to database, " +
								"please check the connection and refresh",
						Toast.LENGTH_SHORT
					).show()

					vRefreshLayout.isRefreshing = false
				}
			}
		}.start()
	}
}
