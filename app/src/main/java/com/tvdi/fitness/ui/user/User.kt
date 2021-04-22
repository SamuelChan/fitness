package com.tvdi.fitness.ui.user

import java.util.*

data class User(val username: String = "") {
    var familyName: String = ""
    var givenName: String = ""
    var password: String = ""
    var email: String = ""
    var phone: String = ""
    var preferredName: String = ""
    var gender: String = ""
    var birthdate: Date = Date()
    var distance: Long = 0
    var stepCount: Long = 0
}