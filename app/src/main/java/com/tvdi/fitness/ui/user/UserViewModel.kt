package com.tvdi.fitness.ui.user

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class UserViewModel : ViewModel(), Observable {

    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    override fun addOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) = callbacks.add(callback)

    override fun removeOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) = callbacks.remove(callback)

    private fun notifyChange() = callbacks.notifyCallbacks(this, 0, null)

    private val _user = MutableLiveData<User>()
    var user: LiveData<User> = _user
    val signedUp = MutableLiveData(false)
    val signedIn = MutableLiveData(false)
    val code = MutableLiveData("")

    init {
        val user = User("")
        _user.value = user
    }

    @Bindable
    fun getPreferredName(): String = user.value?.preferredName ?: ""
    fun setPreferredName(preferredName: String) {
        _user.value?.preferredName = preferredName
        notifyChange()
    }

    @Bindable
    fun getFamilyName(): String = user.value?.familyName ?: ""
    fun setFamilyName(familyName: String) {
        _user.value?.familyName = familyName
        notifyChange()
    }

    @Bindable
    fun getGivenName(): String {
        return if (user.value == null) "" else user.value!!.givenName
    }
    fun setGivenName(givenName: String) {
        _user.value?.givenName = givenName
        notifyChange()
    }

    enum class Gender { MALE, FEMALE }
    fun getGender(gender: Gender): Boolean {
        if (user.value == null) return false
        val userGender = user.value!!.gender
        return when (gender) {
            Gender.MALE -> userGender.equals("male", true)
            Gender.FEMALE -> userGender.equals("female", true)
        }
    }
    fun setGender(gender: Gender) {
        if (user.value == null) return

        when (gender) {
            Gender.MALE -> user.value!!.gender = "male"
            Gender.FEMALE -> user.value!!.gender = "female"
        }
        notifyChange()
    }

    @Bindable
    fun getBirthdate(): String =
        if (user.value == null) "" else
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(user.value!!.birthdate.time)

    fun setBirthdate(dateInput: String) {
        user.value?.birthdate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .parse(dateInput) ?: Date()
        notifyChange()
    }

    @Bindable
    fun getDistance(): Long = user.value?.distance ?: 0
    fun setDistance(newDistance: Long) {
        _user.value!!.distance += newDistance
        notifyChange()
    }

    @Bindable
    fun getStepCount(): Long = user.value?.stepCount ?: 0
    fun setStepCount(steps: Long) {
        _user.value!!.stepCount += steps
        notifyChange()
    }

    @Bindable
    fun getEmail(): String = user.value?.email ?: ""
    fun setEmail(email: String) {
        _user.value!!.email = email
        notifyChange()
    }

    @Bindable
    fun getPhone(): String = user.value?.phone ?: ""
    fun setPhone(phone: String) {
        _user.value!!.phone = phone
        notifyChange()
    }

    @Bindable
    fun getPassword(): String = user.value?.password ?: ""
    fun setPassword(password: String) {
        _user.value!!.password = password
        notifyChange()
    }
}

