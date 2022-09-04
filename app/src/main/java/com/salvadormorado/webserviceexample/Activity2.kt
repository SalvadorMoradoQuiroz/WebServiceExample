package com.salvadormorado.webserviceexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class Activity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        val name = intent.getStringExtra("nameUser").toString()
        val id = intent.getStringExtra("idUser").toString()
        val textView_NameUser = findViewById<TextView>(R.id.textView_NameUser)

        textView_NameUser.text = name + " con ID " + id

    }
}