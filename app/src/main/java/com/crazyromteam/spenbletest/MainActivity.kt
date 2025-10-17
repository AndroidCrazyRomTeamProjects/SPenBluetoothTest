package com.crazyromteam.spenbletest

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val connectBtn: Button = findViewById(R.id.S_pen_connect_btn)
        val spenLoading: ProgressBar = findViewById(R.id.connect_progres)
        connectBtn.setOnClickListener {
            spenLoading.visibility = View.VISIBLE
        }
    }
}