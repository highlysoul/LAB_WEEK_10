package com.example.lab_week_10

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.database.TotalObject
import com.example.lab_week_10.viewmodels.TotalViewModel
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val db by lazy { prepareDatabase() }

    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeValueFromDatabase()
        prepareViewModel()
    }

    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text =
            getString(R.string.text_total, total)
    }

    private fun prepareViewModel() {
        viewModel.total.observe(this) {
            updateText(it)
        }

        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    private fun prepareDatabase(): TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java,
            "total-database"
        ).allowMainThreadQueries()
            .build()
    }

    private fun initializeValueFromDatabase() {
        val total = db.totalDao().getTotal(ID)

        if (total.isEmpty()) {
            db.totalDao().insert(
                Total(
                    id = ID,
                    total = TotalObject(
                        value = 0,
                        date = Date().toString()
                    )
                )
            )
        } else {
            viewModel.setTotal(total.first().total.value)
        }
    }

    override fun onStart() {
        super.onStart()

        val t = db.totalDao().getTotal(ID)
        if (t.isNotEmpty()) {
            val date = t.first().total.date
            Toast.makeText(this, "Last updated: $date", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()

        val currentValue = viewModel.total.value ?: 0

        db.totalDao().update(
            Total(
                id = ID,
                total = TotalObject(
                    value = currentValue,
                    date = Date().toString()
                )
            )
        )
    }

    companion object {
        const val ID: Long = 1
    }
}
