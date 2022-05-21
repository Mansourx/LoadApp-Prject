package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.webkit.URLUtil
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


private const val NOTIFICATION_ID = 0
const val EXTRA_FILENAME = "com.udacity.LoadApp.FILENAME"
const val EXTRA_STATUS = "com.udacity.LoadApp.STATUS"

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private var downloadUrl: String = ""
    private var fileName: String = ""
    private lateinit var status: String
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        custom_download_button.setOnClickListener {
            startDownloading()
        }

        url_et.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                radio_group.clearCheck()
            }
        }

        createChannel(getString(R.string.notification_channel_id), getString(R.string.notification_channel_name))
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(id!!))
            if (cursor.moveToNext()) {
                val currStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                cursor.close()
                if (currStatus == DownloadManager.STATUS_FAILED) {
                    status = getString(R.string.status_failed)
                } else if (currStatus == DownloadManager.STATUS_SUCCESSFUL) {
                    status = getString(R.string.status_success)
                }
            }

            notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendNotification(
                applicationContext,
                fileName,
                status
            )
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            when (view.getId()) {
                R.id.glide_button -> {
                    clearUrlEditText()
                    downloadUrl = URL_GLIDE
                    fileName = applicationContext.getString(R.string.glide_button_text)
                }
                R.id.loadapp_button -> {
                    clearUrlEditText()
                    downloadUrl = URL_LOAD_APP
                    fileName = applicationContext.getString(R.string.loadapp_button_text)
                }
                R.id.retrofit_button -> {
                    clearUrlEditText()
                    downloadUrl = URL_RETROFIT
                    fileName = applicationContext.getString(R.string.retrofit_button_text)
                }
            }
        }
    }

    private fun clearUrlEditText() {
        if (url_et.text.isNullOrBlank()) return
        url_et.text = null
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }

            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.repo_downloaded)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun NotificationManager.sendNotification(
        applicationContext: Context, fileName: String, status: String) {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java).apply {
            putExtra(EXTRA_FILENAME, fileName)
            putExtra(EXTRA_STATUS, status)
        }

        val contentPendingIntent = PendingIntent.getActivity(applicationContext,
            NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(
                applicationContext.getText(R.string.notification_description).toString()
            )
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_assistant_black_24dp,
                applicationContext.getString(R.string.notification_btn_text),
                contentPendingIntent
            ).setPriority(NotificationCompat.PRIORITY_HIGH)

        notify(NOTIFICATION_ID, builder.build())
    }

    private fun download() {
        val urlFromEditText = url_et.text.toString()
        if (urlFromEditText.isNotEmpty() || urlFromEditText.isNotBlank()) {
            val url = setUrlAfterValidation(urlFromEditText)
            if (url.isNotBlank()) {
                downloadUrl = url
            }
        }
        if (downloadUrl.isNotBlank()) {
            disableDownloadButton()
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        } else {
            Toast.makeText(applicationContext,getString(R.string.invalid_link_msg), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUrlAfterValidation(urlFromEditText: String): String {
        if (!URLUtil.isValidUrl(urlFromEditText)) return ""
        return urlFromEditText
    }

    private fun startDownloading() {
        val selectedRd = radio_group?.checkedRadioButtonId
        if (selectedRd == -1 && url_et.text.isNullOrBlank()) {
            Toast.makeText(applicationContext, getString(R.string.option_not_selected_msg), Toast.LENGTH_SHORT).show()
        } else {
            download()
        }
    }

    private fun disableDownloadButton() {
        custom_download_button.buttonState = ButtonState.Loading
        custom_download_button.isClickable = false

    }

    companion object {
        private const val URL_LOAD_APP = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE = "https://github.com/bumptech/glide.git"
        private const val URL_RETROFIT = "https://github.com/square/retrofit.git"
        private const val CHANNEL_ID = "channelId"
    }

}
