package com.tvdi.fitness.ui.user

import java.util.*

data class User(val username: String) {
    // TODO: 2021/4/16
    var familyName: String = ""
    var givenName: String = ""
    var password: String = ""
    var email: String = ""
    var preferredName: String = ""
    var gender: String = ""
    var birthdate: Date = Date()
}