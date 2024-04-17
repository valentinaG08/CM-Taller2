package com.example.taller2.utils.adapters

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CursorAdapter
import android.widget.TextView
import com.example.taller2.R

class ContactsAdapter(context: Context?, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.custom_contacts, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val idContact = view?.findViewById<TextView>(R.id.idContact)
        val contactName = view?.findViewById<TextView>(R.id.contactName)

        val idNum = cursor?.getInt(0);
        val idName = cursor?.getString(1)

        idContact?.text = idNum.toString()
        contactName?.text = idName
    }
}