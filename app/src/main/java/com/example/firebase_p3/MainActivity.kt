package com.example.firebase_p3

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

private lateinit var messageTextView: TextView
class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        messageTextView = findViewById(R.id.messageTextView)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }     // Inicjalizacja TextView
// Firebase setup
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("message")

        // SensorManager setup
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        if (sensorPressure != null) {
            val sensorEventListenerPressure = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val pressureValue = event.values[0]

                    // Wysyłanie wartości do Firebase
                    myRef.setValue("Ciśnienie: $pressureValue hPa")
                        .addOnSuccessListener {
                            Log.d(TAG, "Ciśnienie zapisane w Firebase: $pressureValue hPa")
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Nie udało się zapisać ciśnienia w Firebase", it)
                        }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            }
            sensorManager.registerListener(sensorEventListenerPressure, sensorPressure, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Nasłuch na dane z Firebase
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(String::class.java)
                Log.d(TAG, "Value is: $value")
                messageTextView.text = "Otrzymane dane: $value"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        // Dodanie przycisku do testowego wywołania wyjątku
        val crashButton = Button(this)
        crashButton.text = "Test Crash"
        crashButton.setOnClickListener {
            throw RuntimeException("Test Crash") // Wymuszony błąd do testowania
        }

        addContentView(
            crashButton, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }
}