package com.example

import com.example.data.SeedData
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun verifyExactChapterCounts() {
        val chapters = SeedData.defaultChapters
        val physicsCount = chapters.count { it.subjectId == "sub_physics" }
        val chemCount = chapters.count { it.subjectId == "sub_chemistry" }
        val zooCount = chapters.count { it.subjectId == "sub_zoology" }
        val botCount = chapters.count { it.subjectId == "sub_botany" }

        assertEquals(30, physicsCount)
        assertEquals(25, chemCount)
        assertEquals(16, zooCount)
        assertEquals(17, botCount)
        assertEquals(88, chapters.size)
    }

    @Test
    fun verifyRoutineBlocksCount() {
        assertEquals(16, SeedData.defaultRoutine.size)
    }

    @Test
    fun verifyDefaultRewardsCount() {
        assertEquals(5, SeedData.defaultRewards.size)
    }

    @Test
    fun verifySubjectsCount() {
        assertEquals(4, SeedData.subjects.size)
    }
}
