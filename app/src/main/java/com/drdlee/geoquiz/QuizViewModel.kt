package com.drdlee.geoquiz

import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"

class QuizViewModel: ViewModel() {

    var currentIndex = 0
    var isCheater = false

    private val questionBank = listOf(
        Question(R.string.question_holland, true, answered = false),
        Question(R.string.question_oceans, true, answered = false),
        Question(R.string.question_mideast, false, answered = false),
        Question(R.string.question_africa, false, answered = false),
        Question(R.string.question_americas, true, answered = false),
        Question(R.string.question_asia, true, answered = false)
    )

    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer

    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId

    val isCurrentQuestionAnswered: Boolean
        get() = questionBank[currentIndex].answered

    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBank.size
    }

    fun moveToPrev() {
        currentIndex = when (currentIndex) {
            0 -> questionBank.lastIndex
            else -> currentIndex - 1
        }
    }

    fun updateIsAnswered() {
        questionBank[currentIndex].answered = true
    }

}