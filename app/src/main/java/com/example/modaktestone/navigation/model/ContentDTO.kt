package com.example.modaktestone.navigation.model

data class ContentDTO(
    var uid: String? = null,
    var userName: String? = null,
    var region: String? = null,
    var contentCategory: String? = null,
    var title: String? = null,
    var explain: String? = null,
    var imageUrl: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap(),
    var comments: MutableMap<String, Boolean> = HashMap(),
    var commentCount: Int = 0,
    var postCount: Int = 0,
    var anonymity: MutableMap<String, Boolean> = HashMap(),
    var anonymityCount: Int = 0,
    var anonymityCommentList: MutableMap<String, Boolean> = HashMap()
) {
    data class Comment(
        var uid: String? = null,
        var userName: String? = null,
        var profileUrl: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null,
        var favoriteCount: Int = 0,
        var favorites: MutableMap<String, Boolean> = HashMap(),
        var anonymity: MutableMap<String, Boolean> = HashMap(),
        var anonymityName: String? = null,
        var anonymityCount: Int = 1
    )
}
