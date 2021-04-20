package com.tvdi.fitness.ui.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.tvdi.fitness.R
import com.tvdi.fitness.databinding.FragmentUserBinding
import java.text.SimpleDateFormat
import java.util.*

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val model: UserViewModel by activityViewModels()
    private var emailInput: TextInputEditText? = null
    private var phoneInput: TextInputEditText? = null
    private var birthdateInput: TextInputEditText? = null
    private var preferredNameInput: TextInputEditText? = null
    private var passwordInput: TextInputEditText? = null
    private var loginOrSignupButton: Button? = null
    private var logoutButton: Button? = null
    private var updateButton: Button? = null
    private var signedIn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        _binding?.userViewModel = model
        _binding?.lifecycleOwner = viewLifecycleOwner
        preferredNameInput = _binding?.preferredNameInput
        emailInput = _binding?.emailInput
        birthdateInput = _binding?.birthdateInput.also {
            it?.setOnKeyListener(null)
            it?.setOnClickListener { showDatePicker() }
        }
        phoneInput = _binding?.phoneInput
        passwordInput = _binding?.passwordInput

        loginOrSignupButton = _binding?.loginOrSignupButton.also {
            it?.setOnClickListener { _ ->
                signUpWithModel()
            }
        }
        logoutButton = _binding?.logoutButton.also {
            it?.setOnClickListener {

            }
        }
        updateButton = _binding?.updateButton.also {
            it?.setOnClickListener {

            }
        }

        checkUiWithSession()
        return _binding?.root
    }

    private fun signUpWithModel() {
        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.familyName(), model.getFamilyName())
            .userAttribute(AuthUserAttributeKey.givenName(), model.getGivenName())
            .userAttribute(AuthUserAttributeKey.preferredUsername(), model.getPreferredName())
            .userAttribute(AuthUserAttributeKey.gender(), model.user.value?.gender ?: "")
            .userAttribute(AuthUserAttributeKey.email(), model.getEmail())
            .userAttribute(AuthUserAttributeKey.birthdate(), model.getBirthdate())
            .userAttribute(AuthUserAttributeKey.phoneNumber(), model.getPhone())
            .build()
        Amplify.Auth.signUp(
            model.getEmail(),
            model.getPassword(),
            options, {
                Log.i("AuthQuickStart", "Sign up succeeded: $it")

            }, {
                Log.e("AuthQuickStart", "Sign up failed", it)
            }
        )
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select birthdate")
            .build()
        picker.addOnPositiveButtonClickListener {
            val utcCalendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = it
            val formatted: String = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
                .format(utcCalendar.time)
            _binding?.birthdateInput?.setText(formatted)
        }
        picker.show(requireActivity().supportFragmentManager, picker.toString())
    }

    private fun checkUiWithSession() {
        Amplify.Auth.fetchAuthSession(
            {
                Log.i("AmplifyQuickstart", "Auth session = $it")
                requireActivity().runOnUiThread {
                    loginOrSignupButton?.isEnabled = !it.isSignedIn
                    logoutButton?.isEnabled = it.isSignedIn
                    updateButton?.isEnabled = it.isSignedIn
                    signedIn = true
                }
            }, {
                Log.e("AmplifyQuickstart", "Failed to fetch auth session")
                requireActivity().runOnUiThread {
                    loginOrSignupButton?.isEnabled = true
                    logoutButton?.isEnabled = false
                    updateButton?.isEnabled = false
                    signedIn = false
                }
            }
        )
    }
}