package com.example.madproject

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val userDao: UserDao, private val tripDao: TripDao, private val contactDao: ContactDao, private val panicAlertDao: PanicAlertDao) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _emergencyContacts = MutableLiveData<List<Contact>>()
    val emergencyContacts: LiveData<List<Contact>> get() = _emergencyContacts

    private val _currentUserId = MutableLiveData<Long>(-1)
    val currentUserId: LiveData<Long> get() = _currentUserId

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> get() = _trips

    private fun setCurrentUserId(userId: Long) {
        _currentUserId.value = userId
        println("Current User ID set to: $userId")
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }

                if (user != null && user.password == password) {
                    setCurrentUserId(user.userId)
                    loadUserData(user.userId)
                    println("Login successful. User ID: ${user.userId}")
                } else {
                    errorMessage = "Invalid email or password"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to login: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val existingUser = withContext(Dispatchers.IO) {
                    userDao.getUserByEmail(email)
                }

                if (existingUser != null) {
                    errorMessage = "Email already registered"
                } else {
                    val newUser = User(email = email, password = password)
                    val userId = withContext(Dispatchers.IO) {
                        userDao.insert(newUser)
                    }
                    setCurrentUserId(userId)
                    println("Sign-up successful. User ID: $userId")
                }
            } catch (e: Exception) {
                errorMessage = "Failed to create user: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadUserData(userId: Long) {
        viewModelScope.launch {
            try {
                val userTrips = withContext(Dispatchers.IO) {
                    tripDao.getTripsByUser(userId)
                }
                _trips.value = userTrips

                val userContacts = withContext(Dispatchers.IO) {
                    contactDao.getContactsByUser(userId)
                }
                _emergencyContacts.value = userContacts
            } catch (e: Exception) {
                errorMessage = "Failed to load user data: ${e.message}"
            }
        }
    }

    fun contactLogin(phoneNumber: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val contact = withContext(Dispatchers.IO) {
                    contactDao.getContactByPhoneNumber(phoneNumber)
                }

                if (contact != null) {
                    setCurrentUserId(contact.userId)
                    println("Contact login successful. Linked User ID: ${contact.userId}")
                } else {
                    errorMessage = "Contact not found"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to login as contact: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }


    fun logout() {
        _currentUserId.value = -1
        _trips.value = emptyList()
        _emergencyContacts.value = emptyList()
        errorMessage = null
    }

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    tripDao.insert(trip)
                }
                currentUserId.value?.let { userId ->
                    val updatedTrips = withContext(Dispatchers.IO) {
                        tripDao.getTripsByUser(userId)
                    }
                    _trips.value = updatedTrips
                }
            } catch (e: Exception) {
                errorMessage = "Failed to add trip: ${e.message}"
            }
        }
    }

    fun updateTripStatus(tripId: Long, status: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    tripDao.updateStatus(tripId, status)
                }
                currentUserId.value?.let { userId ->
                    val updatedTrips = withContext(Dispatchers.IO) {
                        tripDao.getTripsByUser(userId)
                    }
                    _trips.value = updatedTrips
                }
            } catch (e: Exception) {
                errorMessage = "Failed to update trip status: ${e.message}"
            }
        }
    }

    fun deleteTrip(tripId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    tripDao.deleteTrip(tripId)
                }
                currentUserId.value?.let { userId ->
                    val updatedTrips = withContext(Dispatchers.IO) {
                        tripDao.getTripsByUser(userId)
                    }
                    _trips.value = updatedTrips
                }
            } catch (e: Exception) {
                errorMessage = "Failed to delete trip: ${e.message}"
            }
        }
    }

    fun updateTripImageUri(tripId: Long, imageUri: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    tripDao.updateImageUri(tripId, imageUri)
                }
                currentUserId.value?.let { userId ->
                    val updatedTrips = withContext(Dispatchers.IO) {
                        tripDao.getTripsByUser(userId)
                    }
                    _trips.value = updatedTrips
                }
            } catch (e: Exception) {
                errorMessage = "Failed to update trip image: ${e.message}"
            }
        }
    }

    private suspend fun loadUserContacts(userId: Long) {
        withContext(Dispatchers.IO) {
            _emergencyContacts.postValue(contactDao.getContactsByUser(userId))
        }
    }

    fun addContact(name: String, phoneNumber: String, linkedUserId: Long? = null, isEmergency: Boolean = false) {
        viewModelScope.launch {
            try {
                currentUserId.value?.let { userId ->
                    val contact = Contact(
                        userId = userId,
                        linkedUserId = linkedUserId ?: userId,
                        name = name,
                        phoneNumber = phoneNumber,
                        isEmergencyContact = isEmergency
                    )
                    withContext(Dispatchers.IO) {
                        contactDao.insert(contact)
                    }
                    loadUserContacts(userId)
                }
            } catch (e: Exception) {
                errorMessage = "Failed to add contact: ${e.message}"
            }
        }
    }

    fun linkContactToUser(contactId: Long, linkedUserId: Long) {
        viewModelScope.launch {
            try {
                val contact = withContext(Dispatchers.IO) {
                    contactDao.getContactById(contactId)
                }

                if (contact != null) {
                    val updatedContact = Contact(
                        contactId = contact.contactId,
                        userId = contact.userId,
                        linkedUserId = linkedUserId,
                        name = contact.name,
                        phoneNumber = contact.phoneNumber,
                        isEmergencyContact = contact.isEmergencyContact
                    )

                    withContext(Dispatchers.IO) {
                        contactDao.updateContact(updatedContact)
                    }

                    currentUserId.value?.let { userId ->
                        loadUserContacts(userId)
                    }
                } else {
                    errorMessage = "Contact not found"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to link contact: ${e.message}"
            }
        }
    }

    fun getTripsForContact(contactUserId: Long): LiveData<List<Trip>> {
        return tripDao.getTripsForContact(contactUserId)
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    contactDao.delete(contact)
                }
                currentUserId.value?.let { userId ->
                    val updatedContacts = withContext(Dispatchers.IO) {
                        contactDao.getContactsByUser(userId)
                    }
                    _emergencyContacts.value = updatedContacts
                }
            } catch (e: Exception) {
                errorMessage = "Failed to delete contact: ${e.message}"
            }
        }
    }

    fun fetchContacts(context: Context, userId: Long): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val phoneNumber = it.getString(phoneIndex)
                contacts.add(Contact(userId = userId, name = name, phoneNumber = phoneNumber, linkedUserId = userId))
            }
        }

        return contacts
    }

    fun triggerPanicAlert(tripId: Long) {
        viewModelScope.launch {
            try {
                val currentUserId = currentUserId.value ?: return@launch
                val panicAlert = PanicAlert(
                    alertId = 0,
                    userId = currentUserId,
                    tripId = tripId,
                    timestamp = System.currentTimeMillis()
                )
                withContext(Dispatchers.IO) {
                    panicAlertDao.insert(panicAlert)
                }
            } catch (e: Exception) {
                errorMessage = "Failed to trigger panic alert: ${e.message}"
            }
        }
    }

    fun getPanicAlertsForContact(contactId: Long): LiveData<List<PanicAlert>> {
        return panicAlertDao.getPanicAlertsForContact(contactId)
    }
}