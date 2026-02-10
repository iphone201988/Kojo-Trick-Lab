package com.tech.kojo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * common api response
 */
data class CommonApiResponse(
    val message: String?, val success: Boolean?
)

/**
 * pinned api response
 */

data class PinnedApiResponse(
    val isPinned: Boolean?, val message: String?, val pinnedCount: Int?, val success: Boolean?
)


/**
 * liked api response
 */
data class LikedApiResponse(
    val likesCount: Int?, val message: String?, val success: Boolean?
)


/**
 * upload profile api response
 */
data class UploadProfileApiResponse(
    val fileName: String?, val success: Boolean?, val url: String?
)

/**
 * login api response
 */

data class LoginApiResponse(
    val message: String?,
    val success: Boolean?,
    val user: LoginUser?
)

data class LoginUser(
    val __v: Int?,
    val _id: String?,
    val bestTrick: String?,
    val clipReviews: Int?,
    val createdAt: String?,
    val deviceToken: String?,
    val deviceType: Int?,
    val email: String?,
    val favouriteTrick: String?,
    val isDeleted: Boolean?,
    val isEmailVerified: Boolean?,
    val name: String?,
    val newVideoAlert: Boolean?,
    val notificationAlert: Boolean?,
    val personalBest: PersonalBest?,
    val profilePicture: String?,
    val sesionReminderAlert: Boolean?,
    val skin: String?,
    val socialLinkedAccounts: List<SocialLinkedAccount?>?,
    val statVisibility: StatVisibility?,
    val status: String?,
    val token: String?,
    val updatedAt: String?
)


data class SocialLinkedAccount(
    val _id: String?,
    val id: String?,
    val provider: Int?
)

data class StatVisibility(
    val showBestTrick: Boolean?,
    val showFavouriteTrick: Boolean?,
    val showMostPracticedTrick: Boolean?,
    val showPBs: Boolean?,
    val showTimeSubscribed: Boolean?,
    val showTimeTricking: Boolean?,
    val showTrickingLevel: Boolean?
)

/**
 * login api response
 */

data class GetProfileResponse(
    val levelData: LevelData?,
    val message: String?,
    val success: Boolean?,
    val user: ProfileUser?
)

data class ProfileUser(
    val __v: Int?,
    val _id: String?,
    val bestTrick: String?,
    val clipReviews: Int?,
    val country: String?,
    val createdAt: String?,
    val deviceToken: String?,
    val deviceType: Int?,
    val dreamTrick: String?,
    val email: String?,
    val favouriteTrick: String?,
    val instagramLink: String?,
    val isDeleted: Boolean?,
    val isEmailVerified: Boolean?,
    val name: String?,
    val newVideoAlert: Boolean?,
    val notificationAlert: Boolean?,
    val personalBest: PersonalBest?,
    val profilePicture: String?,
    val sesionReminderAlert: Boolean?,
    val signatureTrick: String?,
    val skin: String?,
    val socialLinkedAccounts: List<SocialLinkedAccount?>?,
    val statVisibility: ProfileStatVisibility?,
    val status: String?,
    val tiktockLink: String?,
    val timeTricking: String?,
    val trickingNickname: String?,
    val updatedAt: String?,
    val youtubeLink: String?
)

data class PersonalBest(
    val _id: String?,
    val corks: Any?,
    val gainerSwitch: Any?
)


data class ProfileStatVisibility(
    val showBestTrick: Boolean?,
    val showFavouriteTrick: Boolean?,
    val showMostPracticedTrick: Boolean?,
    val showPBs: Boolean?,
    val showTimeSubscribed: Boolean?,
    val showTimeTricking: Boolean?,
    val showTrickingLevel: Boolean?
)


/**
 * Home Trick api response
 */
data class HomeTrickApiResponse(
    val message: String?, val success: Boolean?, val trickVaults: List<HomeTrickVault?>?
)

@Parcelize
data class HomeTrickVault(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val description: String?,
    val imageLink: String?,
    val isFeatured: Boolean?,
    val name: String?,
    val types: List<HomeType?>?,
    val updatedAt: String?
) : Parcelable

@Parcelize
data class HomeType(
    val _id: String?, val name: String?, var check: Boolean = false
) : Parcelable


/**
 * get post api response
 */

data class GetPostApiResponse(
    val page: Int?,
    val posts: List<PostData?>?,
    val success: Boolean?,
    val totalPages: Int?,
    val totalPosts: Int?
)

@Parcelize
data class PostData(
    val _id: String?,
    val createdAt: String?,
    val description: String?,
    val imageLink: String?,
    val isCommented: Boolean?,
    val isLiked: Boolean?,
    var isPinned: Boolean?,
    val postType: String?,
    val title: String?,
    val totalComments: Int?,
    var totalLikes: Int?,
    val updatedAt: String?,
    val userData: UserData?,
    val userId: String?,
    val videoLink: String?
) : Parcelable

@Parcelize
data class UserData(
    val _id: String?, val name: String?, val profilePicture: String?
) : Parcelable


/**
 * comment api response
 */

data class AddCommentApiResponse(
    val comment: Comment?, val message: String?, val success: Boolean?
)

data class Comment(
    val createdAt: String?, val message: String?, val user: User?, val userId: String?
)

data class User(
    val _id: String?, val email: String?, val name: String?, val profilePicture: String?
)


/**
 * get comment api response
 */
data class GetCommentsApiResponse(
    val comments: List<GetCommentData?>?,
    val limit: Int?,
    val page: Int?,
    val total: Int?,
    val totalPages: Int?
)

data class GetCommentData(
    val _id: String?, val createdAt: String?, val message: String?, val user: CommentUser?
)

data class CommentUser(
    val _id: String?, val email: String?, val name: String?, val profilePicture: String?
)

/**
 * get trick By Id Data api response
 */
data class GetTrickByIdApiResponse(
    val count: Int?, val `data`: List<TrickByIdData?>?, val success: Boolean?
)

data class TrickByIdData(
    val _id: String?,
    val description: String?,
    val image: String?,
    val name: String?,
    val userId: UserId?
)

data class UserId(
    val _id: String?, val email: String?, val name: String?
)

/**
 * home progress api response
 */
data class HomeProgressApiResponse(
    val `data`: HomeProgressData?, val message: String?, val success: Boolean?
)

data class HomeProgressData(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val description: String?,
    val image: String?,
    val name: String?,
    val steps: List<HomeProgressStep?>?,
    val trickVaultId: String?,
    val typeId: String?,
    val updatedAt: String?,
    val userId: String?
)

@Parcelize
data class HomeProgressStep(
    val _id: String?,
    val keypoints: List<HomeKeypoint?>?,
    val progress: Progress?,
    val title: String?,
    val videoLinks: List<VideoLink?>?
) : Parcelable

@Parcelize
data class HomeKeypoint(
    val _id: String?, val text: String?
) : Parcelable

@Parcelize
data class Progress(
    val isSaved: Boolean?, val repsCount: Int?, val status: String?, val timeTaken: Int?
) : Parcelable

@Parcelize
data class VideoLink(
    val _id: String?, val link: String?, val type: String?
) : Parcelable


/**
 * user progress api response
 */
data class UserProgressApiResponse(
    val `data`: UserProgressData?, val message: String?, val success: Boolean?
)

data class UserProgressData(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val progress: List<UserProgres?>?,
    val trickDataId: String?,
    val trickVaultId: String?,
    val typeId: String?,
    val updatedAt: String?,
    val userId: String?
)

data class UserProgres(
    val isSaved: Boolean?,
    val repsCount: Int?,
    val status: String?,
    val stepId: String?,
    val timeTaken: Int?
)

/**
 * get tracker api response
 */
data class GetTrackerApiResponse(
    val `data`: List<GetTrackerData?>?, val success: Boolean?
)

data class GetTrackerData(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val imageUrl: String?,
    val title: String?,
    val updatedAt: String?
)

/**
 * trained recently api response
 */
data class GetTrainedRecentlyAPiResponse(
    val `data`: List<RecentlyData?>?, val success: Boolean?
)

data class RecentlyData(
    val __v: Int?,
    val _id: String?,
    val completedSteps: Int?,
    val createdAt: String?,
    val description: String?,
    val image: String?,
    val name: String?,
    val status: String?,
    val steps: List<Step?>?,
    val totalSteps: Int?,
    val trickVaultId: String?,
    val typeId: String?,
    val updatedAt: String?,
    val userId: String?
)

data class Step(
    val _id: String?,
    val keypoints: List<Keypoint?>?,
    val title: String?,
    val videoLinks: List<VideoLink?>?
)

data class Keypoint(
    val _id: String?, val text: String?
)

/**
 * get combo api response
 */
data class GetComboApiResponse(
    val `data`: List<GetComboData?>?, val pagination: Pagination?, val success: Boolean?
)

@Parcelize
data class GetComboData(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val goal: String?,
    val notes: String?,
    val updatedAt: String?,
    val userId: String?
) : Parcelable

@Parcelize
data class Pagination(
    val limit: Int?, val page: Int?, val total: Int?, val totalPages: Int?
) : Parcelable

/**
 * update notes api response
 */

data class UpdateNotesApiResponse(
    val `data`: NotesData?, val success: Boolean?
)

data class NotesData(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val goal: String?,
    val notes: String?,
    val updatedAt: String?,
    val userId: String?
)

/**
 *  milestones api response
 */
data class MilestonesApiResponse(
    val levelData: LevelData?, val levels: List<MilestonesLevel?>?, val success: Boolean?
)

data class LevelData(
    val completedTricks: Int?,
    val level: Int?,
    val timeSubscribed: String?,
    val timeTricking: String?
)

data class MilestonesLevel(
    val _id: String?,
    val completedTasks: Int?,
    val createdAt: String?,
    val imageUrl: String?,
    val level: Int?,
    val pendingTasks: Int?,
    val progressStatus: String?,
    val title: String?,
    val totalTasks: Int?,
    val updatedAt: String?
)

/**
 *  Category By Id api response
 */
data class GetCategoryByIdApiResponse(
    val count: Int?, val success: Boolean?, val tricks: List<CategoryTrick?>?
)

data class CategoryTrick(
    val _id: String?,
    val createdAt: String?,
    val description: String?,
    val imageUrl: String?,
    var progressStatus: String?,
    val title: String?,
    val trickingMilestoneLevelId: String?,
    val updatedAt: String?,
)

/**
 *  get month api response
 */
data class GetMonthApiResponse(
    val `data`: MonthSessionData?, val success: Boolean?
)

data class MonthSessionData(
    val `2025-11-22`: List<String?>?,
    val `2025-11-24`: List<String?>?,
    val `2025-11-26`: List<String?>?,


    )


/**
 *  get next date api response
 */
data class GetNextDateAPiResponse(
    val `data`: List<NextSessionData?>?, val success: Boolean?
)

@Parcelize
data class NextSessionData(
    val __v: Int?,
    val _id: String?,
    val color: String?,
    val createdAt: String?,
    val date: String?,
    val note: String?,
    val review: String?,
    val title: String?,
    val updatedAt: String?,
    val userId: String?
) : Parcelable


/**
 *  get past date api response
 */
data class GetPastSessionAPiResponse(
    val `data`: List<PastSessionData?>?,
    val page: Int?,
    val success: Boolean?,
    val total: Int?,
    val totalPages: Int?
)

@Parcelize
data class PastSessionData(
    val __v: Int?,
    val _id: String?,
    val color: String?,
    val createdAt: String?,
    val date: String?,
    val note: String?,
    val review: String?,
    val title: String?,
    val updatedAt: String?,
    val userId: String?
) : Parcelable

/**
 *  create session  api response
 */
data class CreateSessionApiResponse(
    val `data`: NextSessionData?, val message: String?, val success: Boolean?
)

/**
 *  get library video api response
 */


data class LibraryVideoResponse(
    val `data`: VideoData?, val success: Boolean?
)

data class VideoData(
    val series: List<LibrarySery>?, val topics: List<LibraryTopic>?
)

data class LibrarySery(
    val _id: String?,
    val categoryId: String?,
    val imageUrl: String?,
    val title: String?,
    val type: String?,
    val videoCount: Int?,
    val videos: List<LibraryVideoX?>?
)

data class LibraryTopic(
    val _id: String?,
    val categoryId: String?,
    val imageUrl: String?,
    val title: String?,
    val type: String?,
    val videoCount: Int?,
    val videos: List<LibraryVideoX>?

)

data class LibraryVideoX(
    val __v: Int?,
    val _id: String?,
    val categoryId: String?,
    val createdAt: String?,
    val description: String?,
    val thumbnailUrl: String?,
    val title: String?,
    val topicId: String?,
    val updatedAt: String?,
    val userId: UserId?,
    val videoUrl: String?,
    val views: Int?
)

/**
 *  get video by id api response
 */
data class GetVideoByIdResponse(
    val `data`: GetVideoData?, val success: Boolean?
)

data class GetVideoData(
    val __v: Int?,
    val _id: String?,
    val categoryId: CategoryId?,
    val createdAt: String?,
    val description: String?,
    val thumbnailUrl: String?,
    val title: String?,
    val topicId: TopicId?,
    val updatedAt: String?,
    val userId: UserId?,
    val videoUrl: String?,
    val views: Int?
)

data class CategoryId(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val imageUrl: String?,
    val title: String?,
    val updatedAt: String?
)

data class TopicId(
    val __v: Int?,
    val _id: String?,
    val categoryId: String?,
    val createdAt: String?,
    val imageUrl: String?,
    val title: String?,
    val updatedAt: String?
)

/**
 * get user comments
 */

data class GetUserCommentsData(
    val success: Boolean?, val `data`: List<CommentsData?>?
)

data class CommentsData(
    val __v: Int?,
    val _id: String?,
    val comment: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val userId: UserIdProfile?,
    val videoId: String?
)

data class UserIdProfile(
    val _id: String?, val name: String?, val profilePicture: String?
)

/**
 * get related video
 */

data class GetRelatedVideoData(
    val `data`: List<RelatedVideoData?>?, val pagination: Pagination?, val success: Boolean?
)

data class RelatedVideoData(
    val __v: Int?,
    val _id: String?,
    val categoryId: RelatedCategoryId?,
    val createdAt: String?,
    val description: String?,
    val thumbnailUrl: String?,
    val title: String?,
    val topicId: RelatedTopicId?,
    val updatedAt: String?,
    val userId: UserId?,
    val videoUrl: String?,
    val views: Int?
)

data class RelatedCategoryId(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val imageUrl: String?,
    val title: String?,
    val updatedAt: String?
)

data class RelatedTopicId(
    val __v: Int?,
    val _id: String?,
    val categoryId: String?,
    val createdAt: String?,
    val imageUrl: String?,
    val title: String?,
    val updatedAt: String?
)

/**
 * user post color
 */
data class PostCommentsData(
    val `data`: CommentsDataX?, val message: String?, val success: Boolean?
)

data class CommentsDataX(
    val __v: Int?,
    val _id: String?,
    val comment: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val userId: UserIdProfile?,
    val videoId: String?
)

/**
 * get user profile
 */

data class GetOtherUserProfile(
    val `data`: OtherUserData?, val success: Boolean?
)

data class OtherUserData(
    val _id: String?,
    val bestTrick: String?,
    val country: String?,
    val favouriteTrick: String?,
    val mostPracticedTrick: String?,
    val name: String?,
    val personalBest: OtherPersonalBest?,
    val profilePicture: String?,
    val timeSubscribed: String?,
    val timeTricking: String?,
    val trickingLevel: Int?,
    val trickingNickname: String?
)

data class OtherPersonalBest(
    val _id: String?, val corks: Any?, val gainerSwitch: Any?
)

/**
 * get category data
 */

data class GetVideoCategoryData(
    val `data`: List<CategoryData?>?, val success: Boolean?
)

data class CategoryData(
    val __v: Int?,
    val _id: String?,
    val categoryId: String?,
    val createdAt: String?,
    val imageUrl: String?,
    val title: String?,
    val updatedAt: String?,
    val videoCount: Int?
)

/**
 * get category data
 */

data class GetTopicCategoryData(
    val `data`: List<TopicCategoryData?>?,
    val success: Boolean?
)

data class TopicCategoryData(
    val __v: Int?,
    val _id: String?,
    val createdAt: String?,
    val imageUrl: String?,
    val title: String?,
    val updatedAt: String?
)