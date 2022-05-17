package com.example.invenfinder

import android.app.Activity
import android.os.Bundle
import android.util.Log
import java.sql.DriverManager


class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        Thread {
            val conn =
                DriverManager.getConnection("jdbc:mariadb://10.0.2.2:3306/invenfinder", "root", "test")
            val st = conn.createStatement();
            val res = st.executeQuery("select name from components")

            res.last()
            val components = Array(res.row) { "" }
            res.beforeFirst()

            Log.d("DB", "Loaded data")

            while (res.next()) {
                val idx = res.row - 1
                components[idx] = res.getString("name");
                Log.d("Data", components[idx]);
            }
            st.close()
            conn.close()
            finish()
        }.start()
    }
}
