package com.tvdi.fitness.ui.user

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat

class UserViewModel : ViewModel(), Observable {

    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    override fun addOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) {
        callbacks.remove(callback)
    }

    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    init {
        val user = User("samuelcychan")
        user.apply {
            familyName = "Chan"
            givenName = "Chih Yuan"
            password = "Abcd_123"
            email = "p399975@gmail.com"
            preferredName = "Samuel"
            gender = "female"
        }
        _user.value = user
    }

    @Bindable
    fun getPreferredName(): String = user.value?.preferredName ?: ""
    fun setPreferredName(preferredName: String) {
        _user.value?.preferredName = preferredName
    }

    @Bindable
    fun getFamilyName(): String = user.value?.familyName ?: ""
    fun setFamilyName(familyName: String) {
        _user.value?.familyName = familyName
    }

    @Bindable
    fun getGivenName(): String {
        return if (user.value == null) "" else user.value!!.givenName
    }

    fun setGivenName(givenName: String) {
        // TODO: 2021/4/15 fix this syntax
        if (_user.value != null &&
            _user.value!!.familyName != givenName
        ) {
            _user.value!!.familyName = givenName
        }
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
        // TODO: 2021/4/16 genders
        when (gender) {
            Gender.MALE -> user.value!!.gender = "male"
            Gender.FEMALE -> user.value!!.gender = "female"
        }
    }

    @Bindable
    fun getBirthdate(): String =
        if (user.value == null) "" else SimpleDateFormat("MM/dd/yy").format(user.value!!.birthdate.time)

    fun setBirthdate(dateInput: String) {
        val formatter = SimpleDateFormat("MM/dd/yy")
        user.value?.birthdate = formatter.parse(dateInput)
    }
}