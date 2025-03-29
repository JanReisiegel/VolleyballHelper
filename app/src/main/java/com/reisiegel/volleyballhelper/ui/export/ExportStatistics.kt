package com.reisiegel.volleyballhelper.ui.export

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.databinding.FragmentExportStatisticsBinding
import com.reisiegel.volleyballhelper.services.GoogleDriveService
import kotlinx.coroutines.launch

class ExportStatistics : Fragment() {

    private lateinit var authorizationLauncher: ActivityResultLauncher<Intent>
    private lateinit var viewModel: ExportStatisticsViewModel
    private var _binding: FragmentExportStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleDriveService: GoogleDriveService

    private lateinit var auth: FirebaseAuth
    private val TAG = "GoogleActivity"
    private lateinit var recycleTournamentsView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Authorization was successful, retry the operation
                createAndSaveSheet()
            } else {
                // Authorization was denied or canceled
                Toast.makeText(requireContext(), "Authorization failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ExportStatisticsViewModel::class.java)
        _binding = FragmentExportStatisticsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        if (auth.currentUser != null) {
            signOut()
        }

        googleDriveService = GoogleDriveService(requireContext(), requireActivity())

        viewModel.loadTournaments(requireContext())

        recycleTournamentsView = binding.tournamentList
        recycleTournamentsView.layoutManager = LinearLayoutManager(requireContext())

        val tournamentsAdapter = TournamentAdapter(viewModel.tournamentsItem.value?.toMutableList(), requireContext(), view, {viewModel.deleteTournament(it)}, {viewModel.exportTournament(it)})
        recycleTournamentsView.adapter = tournamentsAdapter

        binding.root.requestLayout()

//        binding.exportButton.setOnClickListener {
//            viewModel.setTournament(SelectedTournament.selectedTournament ?: return@setOnClickListener)
//            var user = auth.currentUser
//            if (user?.displayName != null) {
//                signOut()
//            } else {
//                signInRequest()
//                Log.d(TAG, "User is signed in: ${auth.currentUser?.email}")
//
//            }
//        }

    }

    private fun signInRequest(){
        // Instantiate a Google sign-in request
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false) // Set to false to allow new accounts
            .build()

        // Create the Credential Manager request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Begin the sign-in process
        beginSignIn(request)
    }

    private fun beginSignIn(request: GetCredentialRequest){
        val credentialManager = CredentialManager.create(requireContext())
        lifecycleScope.launch {
            try{
                val result = credentialManager.getCredential(requireActivity(), request)
                val credential = result.credential
                handleSignIn(credential)
            }catch (e: GetCredentialException) {
                if (e is NoCredentialException) {
                    Log.e(TAG, "Couldn't retrieve user's credentials: ${e.message}")
                    Log.e(TAG, "GetCredentialException: ${e.javaClass.name}: ${e.message}")
                } else {
                    Log.e(TAG, "GetCredentialException: ${e.javaClass.name}: ${e.message}")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun handleSignIn(credential: Credential) {
        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(binding.root.context.mainExecutor) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    //binding.exportButton.text = user?.email
                    lifecycleScope.launch {
                        googleDriveService.createGoogleSheet(auth, authorizationLauncher, viewModel.exportTournament.value)
                    }
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //updateUI(null)
                    binding.root.requestLayout()
                }
            }
    }

    private fun createAndSaveSheet() {
        lifecycleScope.launch {
            googleDriveService.createGoogleSheet(auth, authorizationLauncher, viewModel.exportTournament.value)
        }
    }

    private fun signOut() {
        // Firebase sign out
        auth.signOut()

        //binding.exportButton.text = "Export"
        // Update UI
        updateUI(null)

        Log.d(TAG, "User signed out")
    }

    private fun updateUI(user: FirebaseUser?) {
        // Update your UI based on the user's sign-in status
        if (user != null) {
            // User is signed in
            Log.d(TAG, "User is signed in: ${user.email}")
        } else {
            // User is signed out
            Log.d(TAG, "User is signed out")
        }
    }
}