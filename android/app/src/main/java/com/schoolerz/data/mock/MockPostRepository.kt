package com.schoolerz.data.mock

import com.schoolerz.domain.model.ExperienceLevel
import com.schoolerz.domain.model.Post
import com.schoolerz.domain.model.PostType
import com.schoolerz.domain.model.RateType
import com.schoolerz.domain.model.ServiceType
import com.schoolerz.domain.repository.PostRepository
import kotlinx.coroutines.delay

class MockPostRepository : PostRepository {
    private val posts = mutableListOf(
        Post(
            type = PostType.OFFER,
            authorId = "1",
            authorName = "Alex Kim",
            neighborhood = "Downtown",
            body = "Available for dog walking after school! Experienced with all breeds including large dogs. I have my own transport and can pick up/drop off. References available from 3 families I currently walk for.",
            likeCount = 12,
            commentCount = 3,
            rateAmount = 15.0,
            rateMax = 20.0,
            rateType = RateType.HOURLY,
            availableDays = listOf("Monday", "Wednesday", "Friday", "Saturday"),
            availableTimeStart = "3:00 PM",
            availableTimeEnd = "6:00 PM",
            serviceType = ServiceType.DOG_WALKING,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            skillTags = listOf("All Breeds", "Own Transport", "Pet First Aid")
        ),
        Post(
            type = PostType.REQUEST,
            authorId = "2",
            authorName = "Sarah Johnson",
            neighborhood = "Westside",
            body = "Looking for a teen to help with yard work this weekend. Tasks include mowing, weeding, and general cleanup. About 3 hours of work.",
            likeCount = 5,
            commentCount = 2,
            rateAmount = 15.0,
            rateType = RateType.HOURLY,
            availableDays = listOf("Saturday", "Sunday"),
            availableTimeStart = "9:00 AM",
            availableTimeEnd = "12:00 PM",
            serviceType = ServiceType.LAWN_CARE
        ),
        Post(
            type = PostType.OFFER,
            authorId = "3",
            authorName = "Marcus Chen",
            neighborhood = "Eastside",
            body = "Math tutoring available! Currently in AP Calculus, can help with algebra through calculus. I've helped 5 students improve their grades by at least one letter grade. Patient and can explain concepts in multiple ways.",
            likeCount = 24,
            commentCount = 7,
            rateAmount = 25.0,
            rateMax = 35.0,
            rateType = RateType.HOURLY,
            availableDays = listOf("Tuesday", "Thursday", "Saturday"),
            availableTimeStart = "4:00 PM",
            availableTimeEnd = "8:00 PM",
            serviceType = ServiceType.TUTORING,
            experienceLevel = ExperienceLevel.EXPERIENCED,
            skillTags = listOf("AP Calculus", "Algebra", "Patient", "SAT Prep")
        ),
        Post(
            type = PostType.REQUEST,
            authorId = "4",
            authorName = "Emily Rodriguez",
            neighborhood = "Northgate",
            body = "Need someone to walk my kids home from school (3pm pickup). Must be 16+ and reliable. Two kids, ages 8 and 10. About a 15 minute walk.",
            likeCount = 8,
            commentCount = 4,
            rateAmount = 20.0,
            rateType = RateType.PER_TASK,
            availableDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
            availableTimeStart = "3:00 PM",
            availableTimeEnd = "3:30 PM",
            serviceType = ServiceType.BABYSITTING
        ),
        Post(
            type = PostType.OFFER,
            authorId = "5",
            authorName = "Jordan Lee",
            neighborhood = "Downtown",
            body = "Tech help for seniors! Can assist with phones, tablets, computers. Patient and friendly. I can help set up devices, troubleshoot issues, teach you how to video call family, and more. References available.",
            likeCount = 31,
            commentCount = 9,
            rateAmount = 20.0,
            rateType = RateType.HOURLY,
            availableDays = listOf("Monday", "Tuesday", "Thursday"),
            availableTimeStart = "4:00 PM",
            availableTimeEnd = "8:00 PM",
            serviceType = ServiceType.TECH_HELP,
            experienceLevel = ExperienceLevel.EXPERIENCED,
            skillTags = listOf("Patient", "Experienced", "Speaks Spanish")
        ),
        Post(
            type = PostType.OFFER,
            authorId = "6",
            authorName = "Mia Thompson",
            neighborhood = "Lakeside",
            body = "Babysitting services available! CPR certified and have experience with toddlers through elementary age. Can help with homework and prepare simple meals.",
            likeCount = 18,
            commentCount = 5,
            rateAmount = 18.0,
            rateMax = 22.0,
            rateType = RateType.HOURLY,
            availableDays = listOf("Friday", "Saturday"),
            availableTimeStart = "5:00 PM",
            availableTimeEnd = "11:00 PM",
            serviceType = ServiceType.BABYSITTING,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            skillTags = listOf("CPR Certified", "Homework Help", "Meal Prep")
        ),
        Post(
            type = PostType.OFFER,
            authorId = "7",
            authorName = "Jake Williams",
            neighborhood = "Hillcrest",
            body = "Car washing and detailing services! I bring all my own supplies. Interior vacuum, exterior wash, tire shine included. Can come to your location.",
            likeCount = 15,
            commentCount = 4,
            rateAmount = 30.0,
            rateMax = 50.0,
            rateType = RateType.PER_TASK,
            availableDays = listOf("Saturday", "Sunday"),
            availableTimeStart = "10:00 AM",
            availableTimeEnd = "5:00 PM",
            serviceType = ServiceType.OTHER,
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            skillTags = listOf("Mobile Service", "Own Supplies", "Detailing")
        )
    )

    override suspend fun fetchPosts(): Result<List<Post>> {
        delay(500)
        return Result.success(posts.sortedByDescending { it.createdAt })
    }

    override suspend fun createPost(post: Post): Result<Unit> {
        delay(300)
        posts.add(0, post)
        return Result.success(Unit)
    }
}
