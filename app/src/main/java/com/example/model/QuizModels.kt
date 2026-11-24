package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AnimalAccent
import com.example.ui.theme.AnimalContainer
import com.example.ui.theme.ColorShapeAccent
import com.example.ui.theme.ColorShapeContainer
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.FruitAccent
import com.example.ui.theme.FruitContainer
import com.example.ui.theme.IndonesiaAccent
import com.example.ui.theme.IndonesiaContainer
import com.example.ui.theme.MathAccent
import com.example.ui.theme.MathContainer
import com.example.ui.theme.ScienceAccent
import com.example.ui.theme.ScienceContainer

enum class QuizCategoryId(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val accentColor: Color,
    val containerColor: Color
) {
    ANIMALS(
        title = "Dunia Hewan",
        subtitle = "Satwa lucu & habitatnya",
        emoji = "🦁",
        accentColor = AnimalAccent,
        containerColor = AnimalContainer
    ),
    MATH(
        title = "Hitung Ceria",
        subtitle = "Angka, tambah & kurang",
        emoji = "🧮",
        accentColor = MathAccent,
        containerColor = MathContainer
    ),
    SCIENCE(
        title = "Alam & Sains",
        subtitle = "Bumi, luar angkasa & cuaca",
        emoji = "🚀",
        accentColor = ScienceAccent,
        containerColor = ScienceContainer
    ),
    COLORS_SHAPES(
        title = "Warna & Bentuk",
        subtitle = "Pelangi, bangun & pola",
        emoji = "🎨",
        accentColor = ColorShapeAccent,
        containerColor = ColorShapeContainer
    ),
    FRUITS_VEGGIES(
        title = "Buah & Sayur",
        subtitle = "Makanan sehat bergizi",
        emoji = "🍓",
        accentColor = FruitAccent,
        containerColor = FruitContainer
    ),
    INDONESIA(
        title = "Nusantara Kita",
        subtitle = "Budaya & fakta Indonesia",
        emoji = "🇮🇩",
        accentColor = IndonesiaAccent,
        containerColor = IndonesiaContainer
    ),
    DAILY_MIX(
        title = "Tantangan Harian",
        subtitle = "Campuran seru semua topik",
        emoji = "🌟",
        accentColor = CoralPrimary,
        containerColor = CoralContainer
    );

    companion object {
        val playableCategories: List<QuizCategoryId>
            get() = listOf(ANIMALS, MATH, SCIENCE, COLORS_SHAPES, FRUITS_VEGGIES, INDONESIA)

        fun fromName(name: String): QuizCategoryId {
            return entries.firstOrNull { it.name == name } ?: DAILY_MIX
        }
    }
}

enum class DifficultyLevel(val label: String, val badge: String) {
    ALL("Semua Usia", "Campur"),
    EASY("5–7 Tahun", "Mudah"),
    CHALLENGING("8–10 Tahun", "Seru");

    companion object {
        fun fromName(name: String): DifficultyLevel {
            return entries.firstOrNull { it.name == name } ?: ALL
        }
    }
}

enum class MascotBuddy(
    val displayName: String,
    val greeting: String,
    val accentColor: Color
) {
    BERUANG("Bibo si Beruang", "Siap bertualang hari ini?", Color(0xFFFF7849)),
    KUCING("Kiki si Kucing", "Meow! Ayo tebak kuis seru!", Color(0xFF0EA5E9)),
    BURUNG_HANTU("Ozi si Burung Hantu", "Hoo-hoo! Pengetahuan menanti!", Color(0xFF8B5CF6)),
    KELINCI("Rara si Kelinci", "Lompat ke kuis baru yuk!", Color(0xFFEC4899));

    companion object {
        fun fromName(name: String): MascotBuddy {
            return entries.firstOrNull { it.name == name } ?: BERUANG
        }
    }
}

data class CategorySummary(
    val category: QuizCategoryId,
    val questionCount: Int
)

data class QuizQuestion(
    val id: Int,
    val category: QuizCategoryId,
    val difficulty: DifficultyLevel,
    val prompt: String,
    val emojiCue: String,
    val options: List<String>,
    val correctIndex: Int,
    val funFact: String
)

data class AnsweredQuestion(
    val question: QuizQuestion,
    val selectedIndex: Int,
    val isCorrect: Boolean
)
