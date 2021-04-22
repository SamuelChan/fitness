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
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.step.AuthSignInStep
import com.amplifyframework.auth.result.step.AuthSignUpStep
import com.amplifyframework.core.Amplify
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.tvdi.fitness.R
import com.tvdi.fitness.databinding.FragmentUserBinding
import com.tvdi.fitness.ui.VerifyDialogFragment
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "UserFragment"

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
                signOutWithModel()
            }
        }
        updateButton = _binding?.updateButton.also {
            it?.setOnClickListener {
                if (model.signedIn.value == true) {
                    updateWithModel()
                }
            }
        }
        model.signedUp.observe(viewLifecycleOwner, { signup ->
            if (signup && model.signedIn.value == false) {
                signInWithModel()
            }
        })
        model.code.observe(viewLifecycleOwner, { code ->
            if (model.signedIn.value == false && model.signedUp.value == false) {
                signUpConfirmWithModel(code)
            }
        })
        model.signedIn.observe(viewLifecycleOwner, {
            if (it) retrieveAttributeAfterSignedIn()
            updateUiBySignedInState(it)
        })
        checkUiWithSession()
        return _binding?.root
    }

    private fun checkUiWithSession() {
        Amplify.Auth.fetchAuthSession(
            {
                Log.i(TAG, "fetchAuthSession = $it")
                requireActivity().runOnUiThread {
                    model.signedIn.value = it.isSignedIn
                }
            }, {
                Log.e(TAG, "fetchAuthSession failed")
                requireActivity().runOnUiThread {
                    model.signedIn.value = false
                }
            }
        )
    }

    private fun signUpConfirmWithModel(code: String) {
        Amplify.Auth.confirmSignUp(
            model.getEmail(), code,
            {
                Log.i(TAG, "signUpConfirmWithModel = $it")
                requireActivity().runOnUiThread {
                    model.signedUp.value = true
                }
            }, { Log.e(TAG, "signUpConfirmWithModel failed = $it") })
    }

    private fun updateWithModel() {
        val attributes = listOf(
            AuthUserAttribute(AuthUserAttributeKey.familyName(), model.getFamilyName()),
            AuthUserAttribute(AuthUserAttributeKey.givenName(), model.getGivenName()),
            AuthUserAttribute(AuthUserAttributeKey.preferredUsername(), model.getPreferredName()),
            AuthUserAttribute(AuthUserAttributeKey.gender(), model.user.value?.gender ?: ""),
            AuthUserAttribute(AuthUserAttributeKey.birthdate(), model.getBirthdate()),
            AuthUserAttribute(AuthUserAttributeKey.phoneNumber(), model.getPhone())
        )
        Amplify.Auth.updateUserAttributes(attributes,
            { Log.i(TAG, "updateWithModel, $it") },
            { Log.e(TAG, "updateWithModel failed, $it") }
        )
    }

    private fun signOutWithModel() {
        Amplify.Auth.signOut(
            {
                Log.i(TAG, "Sign out succeeded")
                requireActivity().runOnUiThread {
                    model.signedIn.value = false
                }
            }, {
                Log.e(TAG, "Sign out failed, $it")
                requireActivity().runOnUiThread {
                    model.signedIn.value = false
                }
            }
        )
    }

    private fun signInWithModel() {
        Amplify.Auth.signIn(
            model.getEmail(),
            model.getPassword(),
            {
                Log.i(TAG, "signInWithModel, $it")
                if (it.isSignInComplete &&
                    it.nextStep.signInStep == AuthSignInStep.DONE
                ) requireActivity().runOnUiThread {
                    retrieveAttributeAfterSignedIn()
                    model.signedIn.value = true
                }
            }, {
                Log.e(TAG, "signInWithModel failed, $it")
                requireActivity().runOnUiThread {
                    model.signedIn.value = false
                }
            }
        )
    }

    private fun retrieveAttributeAfterSignedIn() {
        Amplify.Auth.fetchUserAttributes(
            { attributes ->
                Log.i(TAG, "retrieveAttributeAfterSignedIn, $attributes")
                retrieveToModel(attributes)
            }, { Log.e(TAG, "retrieveAttributeAfterSignedIn failed, $it") }
        )
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
        Amplify.Auth.signUp(model.getEmail(), model.getPassword(), options, {
            Log.i(TAG, "signUpWithModel, $it")
            if (it.isSignUpComplete &&
                it.nextStep.signUpStep == AuthSignUpStep.DONE
            ) {
                requireActivity().runOnUiThread {
                    model.signedUp.value = true
                }
            }
            requireActivity().runOnUiThread {
                VerifyDialogFragment().show(
                    requireActivity().supportFragmentManager, "dialog"
                )
            }
        }, {
            Log.e(TAG, "signUpWithModel failed, $it")
            if (it is AuthException.UsernameExistsException)
                requireActivity().runOnUiThread { model.signedUp.value = true }
        }
        )
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.date_pick_title).build()
        picker.addOnPositiveButtonClickListener {
            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = it
            val formatted: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(utcCalendar.time)
            _binding?.birthdateInput?.setText(formatted)
        }
        picker.show(requireActivity().supportFragmentManager, picker.toString())
    }

    private fun updateUiBySignedInState(signedIn: Boolean) {
        loginOrSignupButton?.isEnabled = !signedIn
        logoutButton?.isEnabled = signedIn
        updateButton?.isEnabled = signedIn
    }

    private fun retrieveToModel(list: List<AuthUserAttribute>) {
        with(model) {
            requireActivity().runOnUiThread {
                list.forEach {
                    when (it.key) {
                        AuthUserAttributeKey.email() -> setEmail(it.value)
                        AuthUserAttributeKey.familyName() -> setFamilyName(it.value)
                        AuthUserAttributeKey.givenName() -> setGivenName(it.value)
                        AuthUserAttributeKey.preferredUsername() -> setPreferredName(it.value)
                        AuthUserAttributeKey.gender() -> {
                            user.value?.gender = it.value
                        }
                        AuthUserAttributeKey.birthdate() -> setBirthdate(it.value)
                        AuthUserAttributeKey.phoneNumber() -> setPhone(it.value)
                    }
                }
            }
        }
    }
}