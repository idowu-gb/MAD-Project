package com.example.madproject

import android.net.Uri
import android.os.Bundle
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberImagePainter
import com.example.madproject.ui.theme.MADProjectTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MADProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val database = UserDatabase.getDatabase(applicationContext)
                    val userDao = database.userDao()
                    val tripDao = database.tripDao()
                    val contactDao = database.contactDao()

                    val viewModel: UserViewModel = viewModel(
                        factory = ViewModelFactory(userDao, tripDao, contactDao, database.panicAlertDao())
                    )
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("signup") {
                            SignUpScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("home") {
                            HomeScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("logTrip") {
                            LogTripScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("profile") {
                            ProfileScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("contactLogin") {
                            ContactLoginScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("contactView") {
                            ContactViewScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("settings") {
                            SettingsScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("contactList") {
                            ContactListScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("camera/{tripId}") { backStackEntry ->
                            val tripId =
                                backStackEntry.arguments?.getString("tripId")?.toLongOrNull()
                            if (tripId != null) {
                                CameraScreen(
                                    navController = navController,
                                    viewModel = viewModel,
                                    tripId = tripId
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }


    @Composable
    fun LoginScreen(navController: NavController, viewModel: UserViewModel) {
        var isUserLogin by remember { mutableStateOf(true) }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf(false) }
        var passwordError by remember { mutableStateOf(false) }

        val currentUserId by viewModel.currentUserId.observeAsState()

        LaunchedEffect(currentUserId) {
            if (currentUserId != null && currentUserId != -1L) {
                println("Navigating to HomeScreen")
                navController.navigate("home")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("User Login")
                Switch(
                    checked = !isUserLogin,
                    onCheckedChange = { isUserLogin = !isUserLogin }
                )
                Text("Contact Login")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isUserLogin) {
                Text(
                    text = "User Login",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    isError = emailError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError) {
                    Text(
                        text = "Please enter a valid email",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = passwordError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (passwordError) {
                    Text(
                        text = "Password must be at least 6 characters",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        emailError = !isValidEmail(email)
                        passwordError = !isValidPassword(password)

                        if (!emailError && !passwordError) {
                            viewModel.login(email, password)
                            if (viewModel.currentUserId.value != -1L) { // To check if the currentUserId is set
                                navController.navigate("home")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login as User")
                }
            } else {
                Text(
                    text = "Contact Login",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.contactLogin(phoneNumber)
                        navController.navigate("contactView")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login as Contact")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isUserLogin) {
                TextButton(
                    onClick = {
                        navController.navigate("signup")
                    }
                ) {
                    Text("Don't have an account? Sign up")
                }
            }

            viewModel.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

    @Composable
    fun SignUpScreen(navController: NavController, viewModel: UserViewModel) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf(false) }
        var passwordError by remember { mutableStateOf(false) }
        var confirmPasswordError by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                isError = emailError,
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError) {
                Text(
                    text = "Please enter a valid email",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError,
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordError) {
                Text(
                    text = "Password must be at least 6 characters",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPasswordError,
                modifier = Modifier.fillMaxWidth()
            )
            if (confirmPasswordError) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    emailError = !isValidEmail(email)
                    passwordError = !isValidPassword(password)
                    confirmPasswordError = password != confirmPassword

                    if (!emailError && !passwordError && !confirmPasswordError) {
                        viewModel.signUp(email, password)
                        if (viewModel.currentUserId.value != -1L) { // To check if the currentUserId is set
                            navController.navigate("home")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("Already have an account? Login")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomeScreen(navController: NavController, viewModel: UserViewModel) {
        val trips by viewModel.trips.observeAsState(emptyList())
        val contacts by viewModel.emergencyContacts.observeAsState(emptyList())
        val context = LocalContext.current

        var showDeleteTripDialog by remember { mutableStateOf<Trip?>(null) }
        var showDeleteContactDialog by remember { mutableStateOf<Contact?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("StaySafe") },
                    actions = {
                        IconButton(onClick = { navController.navigate("profile") }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { navController.navigate("logTrip") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                ) {
                    Text("Start Trip")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (trips.isNotEmpty()) {
                            viewModel.triggerPanicAlert(trips[0].tripId)
                            Toast.makeText(context, "Panic alert sent!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No trips available to trigger panic alert.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Panic Button")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Recent Trips", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(trips) { trip ->
                        TripCard(
                            trip = trip,
                            onStatusChange = { newStatus ->
                                viewModel.updateTripStatus(trip.tripId, newStatus)
                            },
                            onDelete = {
                                showDeleteTripDialog = trip
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Emergency Contacts Section
                Text("Emergency Contacts", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(contacts) { contact ->
                        ContactCard(
                            contact = contact,
                            isAdded = true,
                            onAdd = {},
                            onDelete = {
                                viewModel.deleteContact(contact)
                            }
                        )
                    }
                }
            }
        }
        if (showDeleteTripDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteTripDialog = null },
                title = { Text("Delete Trip") },
                text = { Text("Are you sure you want to delete this trip?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTrip(showDeleteTripDialog!!.tripId)
                            showDeleteTripDialog = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteTripDialog = null }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        if (showDeleteContactDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteContactDialog = null },
                title = { Text("Delete Contact") },
                text = { Text("Are you sure you want to delete this contact?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteContact(showDeleteContactDialog!!)
                            showDeleteContactDialog = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteContactDialog = null }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen(navController: NavController, viewModel: UserViewModel) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    }


    @Composable
    fun TripCard(trip: Trip, onStatusChange: (String) -> Unit, onDelete: () -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        val statusOptions = listOf("Started", "Paused", "Completed")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Departure: ${trip.departure}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Destination: ${trip.destination}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(text = "ETA: ${trip.eta}", style = MaterialTheme.typography.bodySmall)
                trip.imageUri?.let { uri ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberImagePainter(data = uri),
                        contentDescription = "Trip Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Dropdown
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Status: ${trip.status}")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    onStatusChange(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Trip")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LogTripScreen(navController: NavController, viewModel: UserViewModel) {
        var departure by remember { mutableStateOf("") }
        var destination by remember { mutableStateOf("") }
        var eta by remember { mutableStateOf("") }

        val context = LocalContext.current
        val currentUserId by viewModel.currentUserId.observeAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Log Trip") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = departure,
                    onValueChange = { departure = it },
                    label = { Text("Departure Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = eta,
                    onValueChange = { eta = it },
                    label = { Text("Estimated Time of Arrival (ETA)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (departure.isNotEmpty() && destination.isNotEmpty() && eta.isNotEmpty()) {
                            val trip = Trip(
                                userId = currentUserId ?: -1,
                                departure = departure,
                                destination = destination,
                                eta = eta,
                                status = "Started"
                            )
                            viewModel.addTrip(trip)
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Trip")
                }
                Button(
                    onClick = {
                        val tripId = ""
                            navController.navigate("camera/$tripId")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Open Camera")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProfileScreen(navController: NavController, viewModel: UserViewModel) {
        var newContactName by remember { mutableStateOf("") }
        var newContactPhone by remember { mutableStateOf("") }
        val contacts by viewModel.emergencyContacts.observeAsState(emptyList())

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Add Contact Section
                Text("Add Emergency Contact", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newContactName,
                    onValueChange = { newContactName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newContactPhone,
                    onValueChange = { newContactPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (newContactName.isNotEmpty() && newContactPhone.isNotEmpty()) {
                            viewModel.addContact(newContactName, newContactPhone)
                            newContactName = ""
                            newContactPhone = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Contact")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Import Contacts Button
                Button(
                    onClick = { navController.navigate("contactList") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Import Contacts")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Emergency Contacts List
                Text("Emergency Contacts", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(contacts) { contact ->
                        ContactCard(
                            contact = contact,
                            isAdded = true,
                            onAdd = {},
                            onDelete = {
                                viewModel.deleteContact(contact)
                            }
                        )
                    }
                }
            }
        }
    }


    @Composable
    fun ContactCard(
        contact: Contact,
        isAdded: Boolean,
        onAdd: () -> Unit,
        onDelete: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = contact.name, style = MaterialTheme.typography.titleSmall)
                    Text(text = contact.phoneNumber, style = MaterialTheme.typography.bodySmall)
                }

                if (isAdded) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Contact")
                    }
                } else {
                    Button(onClick = onAdd) {
                        Text("Add")
                    }
                }
            }
        }
    }

    @Composable
    fun ContactLoginScreen(navController: NavController, viewModel: UserViewModel) {
        var phoneNumber by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.contactLogin(phoneNumber)
                    navController.navigate("contactView")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login as Contact")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ContactViewScreen(
        navController: NavController,
        viewModel: UserViewModel
    ) {
        val currentUserId = viewModel.currentUserId.value ?: -1
        val trips by viewModel.getTripsForContact(currentUserId).observeAsState(emptyList())
        val panicAlerts by viewModel.getPanicAlertsForContact(currentUserId).observeAsState(emptyList())

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Linked User's Trips") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            },
            bottomBar = {
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Logout", color = Color.White)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (trips.isEmpty()) {
                    Text(
                        "No trips found for your linked user",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn {
                        items(trips) { trip ->
                            TripDetailCard(
                                trip = trip,
                                onViewDetails = {
                                    navController.navigate("tripDetails/${trip.tripId}")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Panic Alerts", style = MaterialTheme.typography.titleMedium)
                if (panicAlerts.isEmpty()) {
                    Text("No panic alerts", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn {
                        items(panicAlerts) { alert ->
                            PanicAlertCard(alert = alert)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TripDetailCard(
        trip: Trip,
        onViewDetails: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { onViewDetails() }
            ) {
                Text("Trip Details", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                TripDetailRow("Departure:", trip.departure)
                TripDetailRow("Destination:", trip.destination)
                TripDetailRow("ETA:", trip.eta)
                TripDetailRow("Status:", trip.status)

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onViewDetails,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Details")
                }
            }
        }
    }

    @Composable
    fun TripDetailRow(label: String, value: String) {
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(label, modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
            Text(value)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ContactListScreen(
        navController: NavController,
        viewModel: UserViewModel
    ) {
        val context = LocalContext.current
        var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
        var addedContacts by remember { mutableStateOf<Set<Contact>>(emptySet()) }
        var hasContactsPermission by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf(false) }

        val currentUserId by viewModel.currentUserId.observeAsState()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Import Contacts") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (!hasContactsPermission) {
                    ContactsPermissionHandler(
                        onPermissionGranted = { hasContactsPermission = true },
                        onPermissionDenied = { showError = true }
                    )
                }

                if (showError) {
                    Text(
                        text = "Contacts permission denied. Please enable it in settings.",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (hasContactsPermission) {
                    LaunchedEffect(Unit) {
                        contacts = viewModel.fetchContacts(context, currentUserId!!)
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(contacts) { contact ->
                            ContactCard(
                                contact = contact,
                                isAdded = addedContacts.contains(contact),
                                onAdd = {
                                    viewModel.addContact(contact.name, contact.phoneNumber)
                                    addedContacts = addedContacts + contact
                                    Toast.makeText(context, "${contact.name} added", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    viewModel.deleteContact(contact)
                                    addedContacts = addedContacts - contact
                                    Toast.makeText(context, "${contact.name} removed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }

    @Composable
    fun ContactsPermissionHandler(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }

        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    @Composable
    fun PanicAlertCard(alert: PanicAlert) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Panic Alert", style = MaterialTheme.typography.titleSmall)
                Text(text = "Trip ID: ${alert.tripId}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Timestamp: ${alert.timestamp}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    @Composable
    fun CameraPermissionHandler(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }

        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    @Composable
    fun CameraPreview(
        onImageCaptured: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val imageCapture = remember {
            ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
        }

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraExecutor = ContextCompat.getMainExecutor(ctx)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        onError(e)
                    }
                }, cameraExecutor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = {
                    val photoFile = File(
                        context.externalCacheDir,
                        "${System.currentTimeMillis()}.jpg"
                    )

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    photoFile
                                )
                                onImageCaptured(uri)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception)
                            }
                        }
                    )
                }
            ) {
                Text("Capture")
            }
        }
    }

    @Composable
    fun CameraScreen(
        navController: NavController,
        viewModel: UserViewModel,
        tripId: Long
    ) {
        var hasCameraPermission by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf(false) }

        if (!hasCameraPermission) {
            CameraPermissionHandler(
                onPermissionGranted = { hasCameraPermission = true },
                onPermissionDenied = { showError = true }
            )
        }

        if (showError) {
            Text(
                text = "Camera permission denied. Please enable it in settings.",
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (hasCameraPermission) {
            CameraPreview(
                onImageCaptured = { uri ->
                    viewModel.updateTripImageUri(tripId, uri.toString())
                    navController.popBackStack()
                },
                onError = { error ->
                    println("Camera error: $error")
                }
            )
        }
    }
}
