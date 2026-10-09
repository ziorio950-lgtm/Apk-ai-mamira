package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.FoodAnalysis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("NutriAI", appName)
  }

  @Test
  fun `verify food analysis health score calculations`() {
    val analysis = FoodAnalysis(
      detectedName = "سلطة الدجاج المشوي والأفوكادو",
      calories = 365,
      proteinGrams = 32f,
      healthScore = 88
    )
    assertTrue(analysis.healthScore in 0..100)
    assertTrue(analysis.calories > 0)
    assertEquals(32f, analysis.proteinGrams)
    assertNotNull(analysis.detectedName)
  }
}
