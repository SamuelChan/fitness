package com.tvdi.fitness.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.tvdi.fitness.R
import com.tvdi.fitness.ui.user.UserViewModel

class VerifyDialogFragment : DialogFragment() {

    private val model: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val v = inflater.inflate(R.layout.fragment_verify_dialog, null)
            val edit = v.findViewById<EditText>(R.id.confirm_code)
            builder.setView(v)
                .setTitle(R.string.confirm_code_title)
                .setPositiveButton(
                    R.string.sign_up_dialog_button
                ) { _, _ ->
                    if (edit != null) model.code.value = edit.text.toString()
                }
                .setNegativeButton(
                    R.string.cancel_dialog_button
                ) { _, _ ->
                    dialog?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}