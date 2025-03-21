package com.reisiegel.volleyballhelper.ui.export

import android.accounts.Account
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.databinding.FragmentExportStatisticsBinding
import com.reisiegel.volleyballhelper.services.GoogleDriveService
import kotlinx.coroutines.launch

class ExportStatistics : Fragment() {

    companion object {
        private const val TAG = "GoogleActivity"
    }

    private lateinit var viewModel: ExportStatisticsViewModel
    private var _binding: FragmentExportStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleDriveService: GoogleDriveService

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(ExportStatisticsViewModel::class.java)
        _binding = FragmentExportStatisticsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(root.context)

        googleDriveService = GoogleDriveService(root.context)

        binding.exportButton.setOnClickListener {
            launchCredentialManager()
            //auth.signInWithCustomToken(root.context.getString(R.string.default_web_client_id))
        }

        val currentUser = auth.currentUser

        return root
    }

    private fun launchCredentialManager() {
        val googleIdOption = GetSignInWithGoogleOption.Builder(binding.root.context.getString(R.string.default_web_client_id))
            //.setServerClientId(binding.root.context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                // Launch Credential Manager UI
                val result = credentialManager.getCredential(
                    context = binding.root.context,
                    request = request
                )

                Log.d(TAG, "Credential Manager Result: $result") // Loguj celý výsledek
                if (result.credential != null) {
                    handleSignIn(result.credential)
                    Log.d(TAG, "Credential: ${result.credential}")
                } else {
                    Log.e(TAG, "Credential is null")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Přihlášení zrušeno uživatelem.")
                // Zobraz uživateli zprávu, že přihlášení bylo zrušeno
                Toast.makeText(binding.root.context, "Přihlášení zrušeno.", Toast.LENGTH_SHORT).show()
            }catch (e: GetCredentialException) {
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
                Log.e(TAG, "GetCredentialException: $e") // Loguj celou výjimku
            } catch (e: Exception) {
                Log.e(TAG, "General Exception: ${e.localizedMessage}")
                Log.e(TAG, "Exception: $e")
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
                    if (user != null) {
                        val account = user.providerData.find { it.providerId == "google.com" }?.let {
                            Account(it.email!!, "com.google")
                        }
                        if (account != null) {
                            googleDriveService.createGoogleSheet(account)
                        } else {
                            Log.e(TAG, "Couldn't retrieve user's account")
                        }
                    }
                    binding.root.requestLayout()
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //updateUI(null)
                    binding.root.requestLayout()
                }
            }
    }

    private fun signOut() {
        // Firebase sign out
        auth.signOut()

        // When a user signs out, clear the current user credential state from all credential providers.
        lifecycleScope.launch {
            try {
                val clearRequest = androidx.credentials.ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                binding.root.requestLayout()//updateUI(null)
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }
    }
}