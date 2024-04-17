package com.example.taller2

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.taller2.databinding.ActivityContactsBinding
import com.example.taller2.utils.Permissions
import com.example.taller2.utils.adapters.ContactsAdapter


class ContactsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityContactsBinding
    var mProjection: Array<String>? = null
    var mCursor : Cursor? = null
    var contactsAdapter : ContactsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mProjection =
            arrayOf(ContactsContract.Profile._ID, ContactsContract.Profile.DISPLAY_NAME_PRIMARY)

        contactsAdapter = ContactsAdapter(this, null, 0)

        binding.listContacts.adapter = contactsAdapter

        Permissions.requestPermission(
            this,
            android.Manifest.permission.READ_CONTACTS,
            "Para poder verificar los contactos",
            Permissions.MY_PERMISSION_REQUEST_READ_CONTACTS
        ) { success ->
            if (success) {
                initView()
            } else {
                // No renderizar nada
            }
        }

        initView()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Permissions.catchPermissionsResult(requestCode, grantResults, this){ success, code ->
            if (success && code == Permissions.MY_PERMISSION_REQUEST_READ_CONTACTS) {
                initView()
            } else {
                // No renderizar nada
            }
        }
    }

    private fun initView() {
        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED) {
            mCursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, mProjection, null, null, null
            )

            contactsAdapter?.changeCursor(mCursor)
        }
    }

}