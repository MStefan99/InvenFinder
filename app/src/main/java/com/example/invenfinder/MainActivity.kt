package com.example.invenfinder

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import com.example.invenfinder.adapters.ComponentAdapter
import com.example.invenfinder.data.Component
import java.sql.DriverManager


class MainActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main);

		val searchField = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
		val componentList = findViewById<ListView>(R.id.component_list)

		Thread {
			val conn =
				DriverManager.getConnection(
					"jdbc:mariadb://10.0.2.2:3306/invenfinder",
					"root",
					"test"
				)
			val st = conn.createStatement();
			val res = st.executeQuery("select * from components")

			res.last()
			val components = Array(res.row) { Component("", "", 0, 0, 0) }
			res.beforeFirst()

			while (res.next()) {
				val idx = res.row - 1
				components[idx].name = res.getString("name");
				components[idx].description = res.getString("description")
				components[idx].drawer = res.getInt("drawer")
				components[idx].col = res.getInt("col")
				components[idx].row = res.getInt("row")
			}
			st.close()
			conn.close()

			runOnUiThread {
				val componentAdapter = ComponentAdapter(this, R.layout.component, components)

				searchField.setAdapter(componentAdapter)
				componentList.setAdapter(componentAdapter)
			}
		}.start()
	}
}
