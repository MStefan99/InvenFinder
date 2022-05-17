package com.example.invenfinder

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.invenfinder.adapters.ComponentAdapter
import com.example.invenfinder.data.Component
import java.sql.DriverManager


class MainActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main);

		val componentList = findViewById<RecyclerView>(R.id.component_list)
		val searchField = findViewById<EditText>(R.id.search_field)


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
			val components = ArrayList<Component>()
			res.beforeFirst()

			while (res.next()) {
				components.add(
					Component(
						res.getString("name"),
						res.getString("description"),
						res.getInt("drawer"),
						res.getInt("col"),
						res.getInt("row")
					)
				)
			}
			st.close()
			conn.close()

			runOnUiThread {
				val componentAdapter = ComponentAdapter(components)

				searchField.doOnTextChanged { text, _, _, _ -> componentAdapter.filter(text.toString()) }

				componentList.layoutManager = LinearLayoutManager(this)
				componentList.adapter = componentAdapter
			}
		}.start()
	}
}
