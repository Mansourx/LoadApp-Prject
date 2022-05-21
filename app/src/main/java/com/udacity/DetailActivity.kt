package com.udacity

import android.app.NotificationManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        val fileName = intent.getStringExtra(EXTRA_FILENAME)
        findViewById<TextView>(R.id.file_name).apply { text = fileName }
        val status = intent.getStringExtra(EXTRA_STATUS)
        findViewById<TextView>(R.id.status_text_view).apply { text = status }
        ok_button.setOnClickListener{ finish() }
    }

    override fun onStart() {
        super.onStart()
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()
    }

}
