package com.example.data

import com.example.model.BlockType
import com.example.model.Chapter
import com.example.model.ChapterStatus
import com.example.model.Milestone
import com.example.model.MilestoneCategory
import com.example.model.Reward
import com.example.model.RoutineBlock
import com.example.model.Subject

object SeedData {

    val subjects: List<Subject> = listOf(
        Subject(
            id = "sub_physics",
            name = "Physics",
            code = "PHY",
            orderIndex = 1,
            colorHex = "#3949AB", // Indigo
            iconName = "bolt"
        ),
        Subject(
            id = "sub_chemistry",
            name = "Chemistry",
            code = "CHEM",
            orderIndex = 2,
            colorHex = "#00897B", // Teal
            iconName = "science"
        ),
        Subject(
            id = "sub_zoology",
            name = "Zoology",
            code = "ZOO",
            orderIndex = 3,
            colorHex = "#D81B60", // Rose Pink
            iconName = "pets"
        ),
        Subject(
            id = "sub_botany",
            name = "Botany",
            code = "BOT",
            orderIndex = 4,
            colorHex = "#43A047", // Leaf Green
            iconName = "eco"
        )
    )

    // Exact chapter counts: Physics (30), Chemistry (25), Zoology (16), Botany (17) -> Total: 88 chapters
    val defaultChapters: List<Chapter> by lazy {
        val list = mutableListOf<Chapter>()

        // Physics: 30 chapters
        for (i in 1..30) {
            val numStr = String.format("%02d", i)
            list.add(
                Chapter(
                    id = "chap_phy_$i",
                    subjectId = "sub_physics",
                    chapterNumber = i,
                    title = "Physics Chapter $numStr",
                    status = if (i <= 2) ChapterStatus.COMPLETED else if (i == 3) ChapterStatus.IN_PROGRESS else ChapterStatus.NOT_STARTED,
                    notes = if (i == 1) "Completed kinematics equations & projectile motion review." else ""
                )
            )
        }

        // Chemistry: 25 chapters
        for (i in 1..25) {
            val numStr = String.format("%02d", i)
            list.add(
                Chapter(
                    id = "chap_chem_$i",
                    subjectId = "sub_chemistry",
                    chapterNumber = i,
                    title = "Chemistry Chapter $numStr",
                    status = if (i == 1) ChapterStatus.COMPLETED else ChapterStatus.NOT_STARTED,
                    notes = if (i == 1) "Periodic trends and chemical bonding notes compiled." else ""
                )
            )
        }

        // Zoology: 16 chapters
        for (i in 1..16) {
            val numStr = String.format("%02d", i)
            list.add(
                Chapter(
                    id = "chap_zoo_$i",
                    subjectId = "sub_zoology",
                    chapterNumber = i,
                    title = "Zoology Chapter $numStr",
                    status = ChapterStatus.NOT_STARTED
                )
            )
        }

        // Botany: 17 chapters
        for (i in 1..17) {
            val numStr = String.format("%02d", i)
            list.add(
                Chapter(
                    id = "chap_bot_$i",
                    subjectId = "sub_botany",
                    chapterNumber = i,
                    title = "Botany Chapter $numStr",
                    status = ChapterStatus.NOT_STARTED
                )
            )
        }

        list
    }

    // Default 16 routine blocks exactly matching prompt
    val defaultRoutine: List<RoutineBlock> = listOf(
        RoutineBlock("rt_1", "06:00", "06:15", "Wake up & Mindful Morning", BlockType.WAKE_UP, 10, 1),
        RoutineBlock("rt_2", "06:15", "08:15", "Morning Deep Study Block 1", BlockType.STUDY, 15, 2),
        RoutineBlock("rt_3", "08:15", "08:30", "Refreshing Break & Hydration", BlockType.BREAK, 0, 3),
        RoutineBlock("rt_4", "08:30", "09:00", "Morning Focused Study Block 2", BlockType.STUDY, 15, 4),
        RoutineBlock("rt_5", "09:00", "09:30", "Healthy Breakfast & Nourishment", BlockType.MEAL, 5, 5),
        RoutineBlock("rt_6", "09:30", "11:30", "Pre-Noon Study Block 3", BlockType.STUDY, 15, 6),
        RoutineBlock("rt_7", "11:30", "11:45", "Eye Rest & Quick Stretch Break", BlockType.BREAK, 0, 7),
        RoutineBlock("rt_8", "11:45", "13:00", "Midday Study Block 4", BlockType.STUDY, 15, 8),
        RoutineBlock("rt_9", "13:00", "13:30", "Nourishing Lunch & Pause", BlockType.MEAL, 5, 9),
        RoutineBlock("rt_10", "13:30", "15:30", "Afternoon Study Block 5", BlockType.STUDY, 15, 10),
        RoutineBlock("rt_11", "15:30", "15:50", "Afternoon Tea / Walk Break", BlockType.BREAK, 0, 11),
        RoutineBlock("rt_12", "15:50", "17:50", "Late Afternoon Study Block 6", BlockType.STUDY, 15, 12),
        RoutineBlock("rt_13", "17:50", "18:10", "Evening Refresh Break", BlockType.BREAK, 0, 13),
        RoutineBlock("rt_14", "18:10", "19:55", "Evening Study Block 7", BlockType.STUDY, 15, 14),
        RoutineBlock("rt_15", "20:00", "20:30", "Cozy Dinner & Downtime", BlockType.MEAL, 5, 15),
        RoutineBlock("rt_16", "20:30", "21:00", "Night Final Review & Summary", BlockType.REVIEW, 15, 16)
    )

    // 5 default rewards
    val defaultRewards: List<Reward> = listOf(
        Reward("rew_1", "Cute Selfie / Photo", "A sweet personal photo just for you ❤️", 100, "photo_camera"),
        Reward("rew_2", "Sweet Voice Note", "A comforting audio note reminding you how loved you are 🎙️", 150, "mic"),
        Reward("rew_3", "Long Love Message", "A heartwarming, heartfelt letter celebrating your dedication 💌", 200, "favorite"),
        Reward("rew_4", "Personal Video Message", "A dedicated encouraging video cheer for your hard work 📹", 300, "videocam"),
        Reward("rew_5", "Custom Special Request", "Choose any personalized surprise, date idea, or gift ✨", 500, "stars")
    )

    // Milestones: streaks and chapters
    val defaultMilestones: List<Milestone> = listOf(
        Milestone("ms_s7", "7-Day Dedication Streak", MilestoneCategory.STREAK, 7, 100),
        Milestone("ms_s14", "14-Day Consistency Master", MilestoneCategory.STREAK, 14, 150),
        Milestone("ms_s30", "30-Day Unstoppable Power", MilestoneCategory.STREAK, 30, 250),
        Milestone("ms_s50", "50-Day Legendary Focus", MilestoneCategory.STREAK, 50, 400),
        Milestone("ms_s100", "100-Day Centurion of Love & Study", MilestoneCategory.STREAK, 100, 1000),

        Milestone("ms_c10", "10 Chapters Conquered", MilestoneCategory.CHAPTERS, 10, 50),
        Milestone("ms_c25", "Quarter Mastery (25 Chapters)", MilestoneCategory.CHAPTERS, 25, 125),
        Milestone("ms_c50", "Halfway Hero (50 Chapters)", MilestoneCategory.CHAPTERS, 50, 250),
        Milestone("ms_c88", "Complete Syllabus Victory (88 Chapters)", MilestoneCategory.CHAPTERS, 88, 500)
    )

    val defaultFlashcards: List<com.example.model.Flashcard> = listOf(
        com.example.model.Flashcard("fc_1", "sub_physics", "Kinematics", "What is the formula for range of a projectile on horizontal ground?", "R = (u² · sin 2θ) / g. Maximum range occurs at θ = 45° where R_max = u² / g.", com.example.model.FlashcardMastery.GOOD),
        com.example.model.Flashcard("fc_2", "sub_physics", "Thermodynamics", "State the First Law of Thermodynamics and sign convention for work done.", "ΔQ = ΔU + ΔW. In physics, ΔW = P·ΔV (positive when gas expands).", com.example.model.FlashcardMastery.NEED_REVIEW),
        com.example.model.Flashcard("fc_3", "sub_chemistry", "Chemical Bonding", "What determines the bond angle in NH₃ vs H₂O using VSEPR theory?", "NH₃ has 1 lone pair (bond angle ~107°), while H₂O has 2 lone pairs (bond angle ~104.5°) due to greater lone pair - lone pair repulsion.", com.example.model.FlashcardMastery.MASTERED),
        com.example.model.Flashcard("fc_4", "sub_chemistry", "Equilibrium", "What is Le Chatelier's Principle for an exothermic reaction under increasing temperature?", "Increasing temperature shifts the equilibrium in the endothermic (reverse) direction, decreasing the equilibrium constant K.", com.example.model.FlashcardMastery.GOOD),
        com.example.model.Flashcard("fc_5", "sub_zoology", "Cell Cycle", "In which phase of Meiosis does crossing over occur?", "Pachytene stage of Prophase I, mediated by the enzyme recombinase between non-sister chromatids.", com.example.model.FlashcardMastery.MASTERED),
        com.example.model.Flashcard("fc_6", "sub_botany", "Photosynthesis", "What is the primary CO₂ acceptor molecule in C₄ plants?", "Phosphoenolpyruvate (PEP), catalyzed by PEP carboxylase in mesophyll cells.", com.example.model.FlashcardMastery.NEED_REVIEW)
    )

    val defaultDoubts: List<com.example.model.DoubtEntry> = listOf(
        com.example.model.DoubtEntry(
            id = "dbt_1",
            subjectId = "sub_physics",
            subjectName = "Physics",
            chapterTitle = "Ray Optics",
            questionText = "When does Total Internal Reflection occur and why is refractive index ratio inverted in sin(C) = 1/μ?",
            adminSolution = "TIR occurs ONLY when light travels from Denser -> Rarer medium and angle of incidence i > critical angle C. By Snell's law: μ₁·sin(C) = μ₂·sin(90°) = 1, so sin(C) = μ₂/μ₁ = 1/μ (where μ is relative refractive index of denser medium w.r.t rarer medium). You've got this Surbie! ❤️",
            isResolved = true,
            createdAt = "Yesterday"
        ),
        com.example.model.DoubtEntry(
            id = "dbt_2",
            subjectId = "sub_chemistry",
            subjectName = "Chemistry",
            chapterTitle = "Electrochemistry",
            questionText = "How to quickly remember cathode vs anode in electrolytic vs galvanic cells?",
            adminSolution = "Mnemonic: 'An Ox, Red Cat' (Anode is ALWAYS Oxidation, Cathode is ALWAYS Reduction). In Galvanic, Anode is negative. In Electrolytic, Anode is positive. Remember: Oxidation happens at the anode no matter what!",
            isResolved = false,
            createdAt = "Today, 10:15 AM"
        )
    )

    val defaultExamTarget = com.example.model.ExamTarget(
        id = "exam_neet_2026",
        title = "NEET 2026 Medical Entrance",
        examDateStr = "May 3, 2026",
        daysRemaining = 195,
        targetChaptersPerWeek = 4,
        motivationalQuote = "Every single chapter conquered is one step closer to your white coat & stethoscope 🩺✨ You are brilliant!"
    )

    val defaultSoundscapes: List<com.example.model.SoundscapeTrack> = listOf(
        com.example.model.SoundscapeTrack("snd_lofi", "Cozy Lofi Beats", "Gentle study rhythms & Rhodes chords", "music_note", isPlaying = true, volume = 0.6f),
        com.example.model.SoundscapeTrack("snd_rain", "Soft Monsoon Rain", "Soothing rainfall on windowpane", "water_drop", isPlaying = false, volume = 0.5f),
        com.example.model.SoundscapeTrack("snd_cafe", "Parisian Study Cafe", "Subtle coffee house murmur & cups", "local_cafe", isPlaying = false, volume = 0.4f),
        com.example.model.SoundscapeTrack("snd_fire", "Warm Fireplace", "Crackle of embers in a cozy study cabin", "local_fire_department", isPlaying = false, volume = 0.5f)
    )

    val defaultQuests: List<com.example.model.StudyQuest> = listOf(
        com.example.model.StudyQuest(
            id = "qst_weekend_power",
            title = "Weekend High-Yield Sprint",
            description = "Complete 3 chapters in Physics or Chemistry before Sunday night",
            targetType = "CHAPTERS",
            targetGoal = 3,
            currentProgress = 1,
            pointsBounty = 150,
            deadline = "Sunday, 10:00 PM",
            isCompleted = false
        ),
        com.example.model.StudyQuest(
            id = "qst_flashcard_drill",
            title = "Master 5 Flashcard Decks",
            description = "Review and mark 5 tricky formula cards as 'Mastered'",
            targetType = "FLASHCARDS",
            targetGoal = 5,
            currentProgress = 2,
            pointsBounty = 75,
            deadline = "Tomorrow, 8:00 PM",
            isCompleted = false
        )
    )

    val defaultMysteryGifts: List<com.example.model.MysteryGift> = listOf(
        com.example.model.MysteryGift(
            id = "gift_surprise_1",
            title = "A Special Surprise For My Hardworking Girl 🎁",
            message = "Surbie, seeing your dedication inspires me every single day. Take this special bonus boost and treat yourself!",
            pointsAmount = 150,
            perkDescription = "VIP Late-Night Ice Cream & Study Break Pass 🍨",
            isClaimed = false
        )
    )

    val defaultNudges: List<com.example.model.AdminNudge> = listOf(
        com.example.model.AdminNudge(
            id = "ndg_1",
            type = "HUG",
            message = "Zaid sent you a tight virtual hug! You are doing amazing Surbie, don't stress! 💖",
            senderName = "Zaid",
            timestamp = "Just now",
            isAcknowledged = false
        )
    )
}

