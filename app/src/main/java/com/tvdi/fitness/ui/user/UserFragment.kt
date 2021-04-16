package com.tvdi.fitness.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.tvdi.fitness.R
import com.tvdi.fitness.databinding.FragmentUserBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val model: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        _binding?.userViewModel = model
        _binding?.lifecycleOwner = this
        _binding?.birthdateInput?.setOnKeyListener(null)
        _binding?.birthdateInput?.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select birthdate")
                .build()
            picker.addOnPositiveButtonClickListener {
                val utcCalendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                utcCalendar.timeInMillis = it
                val format = SimpleDateFormat("MM/dd/yy")
                val formatted: String = format.format(utcCalendar.time)
                _binding?.birthdateInput?.setText(formatted)
            }
            picker.show(requireActivity().supportFragmentManager, picker.toString())
        }
        return _binding?.root
    }
}