package com.tvdi.fitness

import android.util.Patterns
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

@BindingAdapter("emailError", "emailText")
fun setErrorText(view: TextInputLayout, err: String, text: String) {
    val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(text).matches()
    if (isValidEmail) view.error = null
    else view.error = err
}