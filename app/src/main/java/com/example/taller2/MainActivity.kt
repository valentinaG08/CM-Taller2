package com.example.taller2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openContactsActivity(view: View) {
        val intent = Intent(this, ContactsActivity::class.java)
        startActivity(intent)
    }

    fun openGalleryActivity(view: View) {
        val intent = Intent(this, GalleryActivity::class.java)
        startActivity(intent)
    }

    fun openMapActivity(view: View) {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }
}
