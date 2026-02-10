package com.tech.kojo.data.api


object Constants {
    //const val BASE_URL = "https://951350efff0f.ngrok-free.app/api/v1/"
    const val BASE_URL = "http://51.21.86.135:8000/api/v1/"
    const val BASE_URL_IMAGE = "http://51.21.86.135:8000"

    /**************** API LIST *****************/
    const val HEADER_API = "X-API-Key:lkcMuYllSgc3jsFi1gg896mtbPxIBzYkEL"
    // auth section api
    const val SIGNUP = "auth/register"
    const val LOGIN = "auth/login"
    const val SOCIAL_LOGIN = "auth/social-login"
    const val RESEND_OTP = "auth/resend-otp"
    const val VERIFY_OTP = "auth/verify-otp"
    const val FORGOT_EMAIL = "auth/forgot-password"
    const val RESET_PASSWORD = "auth/reset-password"
    const val LOGOUT = "auth/logout"
    const val DELETE_ACCOUNT = "auth/delete"
    const val CHANGE_PASSWORD = "auth/change-password"

    // profile
    const val GET_PROFILE = "auth/get-profile"
    const val UPDATE_PROFILE = "auth/update-profile"
    const val UPLOAD = "upload"

    // trick
    const val GET_TRICKS_VAULT_ALL = "tricks-vault/all"
    const val TRICKS_DATA = "tricks-data"
    const val POST_PROGRESS = "user-progress/create"
    const val MILESTONE_CATEGORY_UI = "milestone-category-ui"
    const val PROGRESS_TRACKER = "progress-tracker"

    // post
    const val GET_POST ="post"
    const val POST_LIKE ="post/like"
    const val POST_COMMENT ="post/comment"
    const val GET_COMMENTS ="post/comments"
    const val POST_PIN ="post/pin"
    const val POST_CREATE ="post/create"

    // combo
    const val GET_COMBO_GOALS ="combo-goals"
    const val COMBO_GOALS_CREATE ="combo-goals/create"
    const val COMBO_GOALS_UPDATE ="combo-goals/update"
    const val COMBO_GOALS_DELETE ="combo-goals/delete"

    // milestone
    const val TRICKING_MILESTONE ="tricking-milestone"
    const val TRICKING_MILESTONE_TRICKS ="tricking-milestone/tricks"
    const val TRICKING_MILESTONE_PROGRESS ="tricking-milestone/progress"

    // session planner
    const val SESSION_CREATE ="session-planner/create"
    const val SESSION_PLANNER_MONTH ="session-planner/month"
    const val SESSION_PLANNER_DATE ="session-planner/date"
    const val SESSION_PLANNER ="session-planner"
    const val SESSION_PLANNER_UPDATE ="session-planner/update"
    const val SESSION_PLANNER_DELETE ="session-planner/delete"

    // library
    const val VIDEO_LIBRARY ="video-data/dashboard-videos"
    const val GET_VIDEO_ID ="video-data/get-video"
    const val VIDEO_COMMENT ="video-comment"
    const val VIDEO_RELATED ="video-data/topic-video"
    const val VIDEO_COMMENTS ="video-comment"
    const val AUTH_GET_USER ="auth/get-user"
    const val VIDEO_DATA_GET_TOPIC ="video-data/get-topic"
    const val GET_ALL_CATEGORY ="video-data/all-category"



}