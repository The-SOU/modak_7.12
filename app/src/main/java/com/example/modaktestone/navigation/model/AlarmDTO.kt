package com.example.modaktestone.navigation.model

data class AlarmDTO(
    var destinationUid: String? = null,
    var userName: String? = null,
    var uid: String? = null,
    //0: 공감하기
    //1: 댓글달기
    var kind: Int? = null,
    var message: String? = null,
    var timestamp: Long? = null,
    var contentUid: String? = null,
    var didYouRead: MutableMap<String, Boolean> = HashMap()
)
