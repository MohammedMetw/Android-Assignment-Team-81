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
import android.widget.Spinner
import android.widget.Toast
import android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {

    private var etName: EditText? = null
    private var etPhone: EditText? = null
    private var etCategory: EditText? = null
    private var btnSave: Button? = null
    private var listContacts: ListView? = null

    private lateinit var contactDao: ContactDao

    private val contactStrings = mutableListOf<String>()
    private var contactsAdapter: ArrayAdapter<String>? = null

    private var currentContacts: List<Contact> = emptyList()

    private lateinit var categorySpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // views
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etCategory = findViewById(R.id.etCategory)
        btnSave = findViewById(R.id.btnSave)
        listContacts = findViewById(R.id.listContacts)


        categorySpinner = findViewById(R.id.spinnerCategories)

        val btnFilter: Button = findViewById(R.id.btnFilter)
        val btnShowAll: Button = findViewById(R.id.btnShowAll)

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

        btnFilter.setOnClickListener {
            filterByCategory()
        }

        btnShowAll.setOnClickListener {
            observeAllContacts()
        }


        // observe all contacts and update UI
        observeAllContacts()
        loadCategories()
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
        lifecycleScope.launch(Dispatchers.IO) {
            contactDao.getAllContacts().collect { contacts ->
                displayContacts(contacts)
            }
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            contactDao.getAllCategories().collect { categories ->
                runOnUiThread {
                    val categoryList = mutableListOf("Select Category")
                    categoryList.addAll(categories)

                    val adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_spinner_item,
                        categoryList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = adapter
                }
            }
        }
    }

    private fun filterByCategory() {
        val selectedCategory = categorySpinner.selectedItem as? String
        if (selectedCategory == null || selectedCategory == "Select Category") {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            contactDao.getContactsByCategory(selectedCategory).collect { contacts ->
                runOnUiThread {
                    displayContacts(contacts)
                }
            }
        }
    }

    private fun displayContacts(contacts: List<Contact>) {
        currentContacts = contacts
        val toDisplay = contacts.map { "${it.name} - ${it.category}" }

        runOnUiThread {
            contactStrings.clear()
            contactStrings.addAll(toDisplay)
            contactsAdapter?.notifyDataSetChanged()
        }
    }
}




