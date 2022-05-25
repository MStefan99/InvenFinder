package com.example.invenfinder.activities

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import com.example.invenfinder.R
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


class BarcodeActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val options = BarcodeScannerOptions.Builder()
			.setBarcodeFormats(
				Barcode.FORMAT_EAN_8
			)
			.build()

		val client = BarcodeScanning.getClient(options)
		val bitmap = BitmapFactory.decodeResource(resources, R.drawable.barcode)

		client
			.process(InputImage.fromBitmap(bitmap, 0))
			.addOnSuccessListener { barcodes ->
				for (barcode in barcodes) {
					Log.d("Barcodes", barcode.displayValue.toString())
				}
			}
	}
}
