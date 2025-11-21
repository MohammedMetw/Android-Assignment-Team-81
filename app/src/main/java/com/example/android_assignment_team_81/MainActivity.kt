package com.example.android_assignment_team_81

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private var etName: EditText? = null
    private var etPhone: EditText? = null
    private var etCategory: EditText? = null
    private var btnSave: Button? = null
    private var listContacts: ListView? = null

    private var contactDao: ContactDao? = null

    private val contactStrings = mutableListOf<String>()
    private var contactsAdapter: ArrayAdapter<String>? = null

    private var currentContacts: List<Contact> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // views
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etCategory = findViewById(R.id.etCategory)
        btnSave = findViewById(R.id.btnSave)
        listContacts = findViewById(R.id.listContacts)

        // Room
        val db = ContactDatabase.getDatabase(this)
        contactDao = db.contactDao()

        // ListView + ArrayAdapter
        contactsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            contactStrings
        )
        listContacts?.adapter = contactsAdapter

        // Save button
        btnSave?.setOnClickListener {
            saveContact()
        }

        // observe all contacts and update UI
        observeAllContacts()
    }

    private fun saveContact() {
        val name = etName?.text?.toString()?.trim() ?: ""
        val phone = etPhone?.text?.toString()?.trim() ?: ""
        val category = etCategory?.text?.toString()?.trim() ?: ""

        // param validation
        if (name.isEmpty() || phone.isEmpty() || category.isEmpty()) {
            return
        }

        val newContact = Contact(
            name = name,
            phone = phone,
            category = category
        )

        // Insert using Room in background thread
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = contactDao
            if (dao != null) {
                dao.insert(newContact)
            }
        }

        // clear input fields
        etName?.text?.clear()
        etPhone?.text?.clear()
        etCategory?.text?.clear()
    }

    private fun observeAllContacts() {
        val dao = contactDao ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            dao.getAllContacts().collect { contacts ->
                currentContacts = contacts
                val toDisplay = contacts.map { it.name + " - " + it.category }

                withContext(Dispatchers.Main) {
                    contactStrings.clear()
                    contactStrings.addAll(toDisplay)
                    contactsAdapter?.notifyDataSetChanged()
                }
            }
        }
    }
}
