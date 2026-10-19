package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Two Hearts", appName)
  }

  @Test
  fun `normalizeUrl handles diverse inputs and path segments cleanly`() {
    val raw1 = "  https://kxxdqektnssyltnmzgre.supabase.co/  "
    assertEquals("https://kxxdqektnssyltnmzgre.supabase.co", com.example.data.SupabaseClient.normalizeUrl(raw1))

    val raw2 = "https://kxxdqektnssyltnmzgre.supabase.co/rest/v1/subjects?select=id&limit=1"
    assertEquals("https://kxxdqektnssyltnmzgre.supabase.co", com.example.data.SupabaseClient.normalizeUrl(raw2))

    val raw3 = "kxxdqektnssyltnmzgre.supabase.co"
    assertEquals("https://kxxdqektnssyltnmzgre.supabase.co", com.example.data.SupabaseClient.normalizeUrl(raw3))
  }

  @Test
  fun `sanitizeKey removes quotes and newlines`() {
    val key = "  \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\" \n "
    assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", com.example.data.SupabaseClient.sanitizeKey(key))
  }

  @Test
  fun `progress and points persist across app restarts and repository recreation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Session 1: User works in the app
    val repo1 = com.example.data.TwoHeartsRepository(context)
    val startPoints = repo1.totalPoints.value
    
    // User earns points via admin bonus / study / routine
    repo1.awardAdminBonus(50, "Great practice on kinematics!")
    assertEquals(startPoints + 50, repo1.totalPoints.value)

    // User marks a chapter completed
    val targetChapterId = repo1.chapters.value.first().id
    repo1.updateChapterStatus(targetChapterId, com.example.model.ChapterStatus.COMPLETED)
    assertEquals(com.example.model.ChapterStatus.COMPLETED, repo1.chapters.value.first { it.id == targetChapterId }.status)

    // Session 2: User leaves the app and reopens it (simulated by creating a fresh repository instance)
    val repo2 = com.example.data.TwoHeartsRepository(context)
    assertEquals("Points must be preserved across app exits!", startPoints + 50, repo2.totalPoints.value)
    val restoredChapter = repo2.chapters.value.first { it.id == targetChapterId }
    assertEquals("Chapter completion must be preserved across app exits!", com.example.model.ChapterStatus.COMPLETED, restoredChapter.status)
  }

  @Test
  fun `flashcards and doubts features work and persist properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo1 = com.example.data.TwoHeartsRepository(context)
    val startPoints = repo1.totalPoints.value

    // 1. Flashcard mastery adds points and persists
    val card = repo1.flashcards.value.first()
    repo1.updateFlashcardMastery(card.id, com.example.model.FlashcardMastery.MASTERED)
    assertEquals(startPoints + 5, repo1.totalPoints.value)

    // 2. Logging and resolving doubts awards +20 points
    repo1.addDoubt("sub_physics", "Physics", "Thermodynamics", "Carnot engine efficiency vs real gas?")
    val createdDoubt = repo1.doubts.value.first { it.chapterTitle == "Thermodynamics" }
    repo1.resolveDoubt(createdDoubt.id, "Carnot cycle represents maximum theoretical upper limit η = 1 - T_c / T_h.")
    assertEquals(startPoints + 25, repo1.totalPoints.value)

    // 3. Recreate repository (simulate app reopen)
    val repo2 = com.example.data.TwoHeartsRepository(context)
    assertEquals(startPoints + 25, repo2.totalPoints.value)
    val restoredCard = repo2.flashcards.value.first { it.id == card.id }
    assertEquals(com.example.model.FlashcardMastery.MASTERED, restoredCard.mastery)
    val restoredDoubt = repo2.doubts.value.first { it.id == createdDoubt.id }
    assertTrue(restoredDoubt.isResolved)
    assertTrue(restoredDoubt.adminSolution.contains("Carnot cycle"))
  }
}
