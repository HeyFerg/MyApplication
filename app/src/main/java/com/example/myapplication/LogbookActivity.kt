package com.example.myapplication


import kotlinx.android.synthetic.main.logbook.*

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button


class LogbookActivity : AppCompatActivity() {

    lateinit var helper: MyHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logbook)

        helper = MyHelper(this)

        addBtn.setOnClickListener{
            val day = etDay.text.toString()
            val steps = etSteps.text.toString().toLong()

            val id = helper.addSteps(day, steps)
            etID.setText("$id")

        }

        searchBtn.setOnClickListener{
            val day = etDay.text.toString()

            var b = helper.searchSteps(day)[0].steps

           etSteps.setText(b.toString())
        }
    }
}