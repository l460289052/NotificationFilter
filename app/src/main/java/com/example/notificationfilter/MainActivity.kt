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
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
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
import com.example.notificationfilter.database.NotificationDatabase
import com.example.notificationfilter.database.NotificationFilter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewAdapter: NotificationItemAdapter
    private lateinit var manager: NotificationManager

    private val db: NotificationDatabase by lazy { NotificationDatabase.getDatabase(this) }
    private var notificationItemList: List<NotificationItem> = listOf()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel("normal", "Normal", NotificationManager.IMPORTANCE_DEFAULT)
        )
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.run {
            val switch: Switch = findItem(R.id.menu_switch).actionView as Switch
            switch.setOnCheckedChangeListener { compoundButton, enabled ->
                if (enabled) {
                    if (!notificationPermission)
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) // 这句话是请求权限的…… NotificationListenerService.requestRebind(
                    rebindListenerService()
                } else {
                    sendBroadcast(Intent(NotificationCatcher.IntentStop))
                }
                Log.v(TAG, "switch $enabled")
            }
        }
        return true
    }

    private fun rebindListenerService() {
        NotificationListenerService.requestRebind(
            ComponentName(
                applicationContext,
                NotificationCatcher::class.java
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_noti ->
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
            R.id.menu_filters ->
                startActivity(Intent(this, FilterActivity::class.java))
        }
        return true
    }

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
            }.also { registerForContextMenu(it) }

            editTextDateStart.text = Editable.Factory.getInstance()
                .newEditable(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.notification_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.context_menu_filter -> {
            GlobalScope.launch(Dispatchers.IO) {
                notificationItemList[viewAdapter.currentPosition].run {
                    db.filterDao().insert(
                        NotificationFilter("^$pkg\n$channel\n")
                    )
                }
            }
            rebindListenerService()
            true
        }
        R.id.context_menu_filter_delete -> {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val regex =
                        notificationItemList[viewAdapter.currentPosition].run { "^$pkg\n$channel\n" }
//                    db.filterDao().insert(NotificationFilter(regex))
//
//                    rebindListenerService(

                    val dao = db.notificationDao()
                    val reg = regex.toRegex()
                    val all = dao.getAll()
                    val toBeRemove = all.filter { reg.find(it.toBeRegex())?.value != null }
                    dao.delete(*toBeRemove.toTypedArray())
                }
                search()
            }
            true
        }
        else -> super.onContextItemSelected(item)
    }


    private fun setListener() {

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
                            !listOf(name, n.app, n.channel, n.title, n.content)
                                .joinToString("\n").lowercase().contains(keyword)
                        )
                            continue
                        l.add(
                            NotificationItem(
                                n.app, name, n.channel, n.title, n.content,
                                n.intent, n.time, ai.loadIcon(pm)
                            )
                        )

                    } catch (e: PackageManager.NameNotFoundException) {
                        if (keyword.isNotEmpty() &&
                            !listOf(n.app, n.app, n.channel, n.title, n.content)
                                .joinToString("\n").lowercase().contains(keyword)
                        )
                            continue
                        l.add(
                            NotificationItem(
                                n.app, n.app, n.channel, n.title,
                                n.content, n.intent, n.time
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