package com.example.notificationfilter

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.text.Editable
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificationfilter.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewAdapter: NotificationItemAdapter

    private val db: NotificationDatabase by lazy { NotificationDatabase.getDatabase(this) }
    private var notificationItemList: List<NotificationItem> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createView()

        setListener()

        search()
    }

    private fun createView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.run {
            logView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
            }

            editTextDateStart.text = Editable.Factory.getInstance()
                .newEditable(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
        }


    }

    private fun setListener() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel("normal", "Normal", NotificationManager.IMPORTANCE_DEFAULT)
        )

        binding.run {
            searchButton.setOnClickListener { search() }


            editTextDateStart.setOnClickListener {
                DateSetListener(editTextDateStart).also {
                    val (year, month, day) = it.date
                    DatePickerDialog(this@MainActivity, it, year, month, day).show()
                }
            }

            editTextDateEnd.setOnClickListener {
                DateSetListener(editTextDateEnd).also {
                    val (year, month, day) = it.date
                    DatePickerDialog(this@MainActivity, it, year, month, day).show()
                }
            }


            notiButton.setOnClickListener {
                manager.notify(
                    1,
                    NotificationCompat.Builder(this@MainActivity, "normal")
                        .apply {
                            setContentTitle("This is content title")
                            setContentText("This is text")
                            setSmallIcon(R.drawable.archives)
                            setAutoCancel(true)
                        }.build()
                )
            }

            startButton.setOnClickListener {
                if (!notificationPermission)
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) // 这句话是请求权限的…… NotificationListenerService.requestRebind(
                NotificationListenerService.requestRebind(
                    ComponentName(
                        applicationContext,
                        NotificationCatcher::class.java
                    )
                )
            }
            stopButton.setOnClickListener {
                sendBroadcast(Intent(NotificationCatcher.IntentStop))

            }

            swipeRefreshLayout.setOnRefreshListener {
                search()
                swipeRefreshLayout.isRefreshing = false
            }
        }

    }


    private fun search() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val startDate =
                    LocalDate.parse(binding.editTextDateStart.text, DateTimeFormatter.ISO_DATE)
                val endDate = if (binding.editTextDateEnd.text.isNotEmpty())
                    LocalDate.parse(binding.editTextDateStart.text, DateTimeFormatter.ISO_DATE)
                else
                    null
                val notifications = db.notificationDao().run {
                    if (endDate == null) db.notificationDao()
                        .getAfter(
                            LocalDateTime.of(
                                startDate,
                                LocalTime.of(0, 0)
                            )
                        ) else db.notificationDao()
                        .getBetween(
                            LocalDateTime.of(startDate, LocalTime.of(0, 0)),
                            LocalDateTime.of(endDate.also { it.plusDays(1) }, LocalTime.of(0, 0))
                        )
                }

                val keyword = binding.editTextSearch.text.toString().lowercase()
                Log.v(TAG, "keyword: $keyword")
                val pm = packageManager
                val l = mutableListOf<NotificationItem>()
                for (n in notifications)
                    try {
                        val ai = pm.getApplicationInfo(n.app, 0)
                        val name = ai.loadLabel(pm) as String
                        if (keyword.isNotEmpty() &&
                            !(name.lowercase().contains(keyword) ||
                                    n.app.lowercase().contains(keyword) ||
                                    n.title.lowercase().contains(keyword) ||
                                    n.content.lowercase().contains(keyword))
                        )
                            continue
                        l.add(
                            NotificationItem(
                                name,
                                n.title,
                                n.content,
                                n.time,
                                ai.loadIcon(pm)
                            )
                        )

                    } catch (e: PackageManager.NameNotFoundException) {
                        if (keyword.isNotEmpty() &&
                            !(n.app.lowercase().contains(keyword) ||
                                    n.title.lowercase().contains(keyword) ||
                                    n.content.lowercase().contains(keyword))
                        )
                            continue
                        l.add(
                            NotificationItem(
                                n.app,
                                n.title,
                                n.content,
                                n.time
                            )
                        )

                    }
                notificationItemList = l
            }

            withContext(Dispatchers.Main) {
                viewAdapter = NotificationItemAdapter(notificationItemList)
                binding.logView.adapter = viewAdapter
            }
        }
    }

    private class DateSetListener(val editText: EditText) : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
            Log.v(TAG, "year $year month $month day $day")
            editText.text = Editable.Factory.getInstance()
                .newEditable(LocalDate.of(year, month, day).format(DateTimeFormatter.ISO_DATE))
        }

        data class Date(val year: Int, val month: Int, val day: Int)

        val date: Date
            get() {
                val d = if (editText.text.isNotEmpty()) LocalDate.parse(
                    editText.text,
                    DateTimeFormatter.ISO_DATE
                )
                else LocalDate.now()
                return Date(d.year, d.monthValue, d.dayOfMonth)
            }

    }

    private val notificationPermission: Boolean
        get() {
            val pkgnames = NotificationManagerCompat.getEnabledListenerPackages(this)
            return pkgnames.contains(packageName)
        }

    companion object {
        val TAG = "NOTI-ACTIVITY"
    }
}