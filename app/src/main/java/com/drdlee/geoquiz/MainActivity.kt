package com.drdlee.geoquiz

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val KEY_SCORE = "score"
private const val KEY_ANSWERED = "answered_count"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    private lateinit var questionTextView: TextView
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var cheatButton: Button
    private lateinit var scoreTextView: TextView

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: called")
        setContentView(R.layout.activity_main)

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        val score = savedInstanceState?.getInt(KEY_SCORE, 0) ?: 0
        val answeredCount = savedInstanceState?.getInt(KEY_ANSWERED, 0) ?: 0
        quizViewModel.apply {
            this.currentIndex = currentIndex
            this.score = score
            this.answeredCount = answeredCount
        }

        questionTextView = findViewById(R.id.question_text_view)
        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        prevButton = findViewById(R.id.prev_button)
        cheatButton = findViewById(R.id.cheat_button)
        scoreTextView = findViewById(R.id.score_text_view)

        trueButton.setOnClickListener {
            checkAnswer(true)
            quizViewModel.updateIsAnswered()
            checkIsAllAnswered()
            disableAnswerButton()
        }

        falseButton.setOnClickListener {
            checkAnswer(false)
            quizViewModel.updateIsAnswered()
            checkIsAllAnswered()
            disableAnswerButton()
        }

        nextButton.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
        }

        prevButton.setOnClickListener {
            quizViewModel.moveToPrev()
            updateQuestion()
        }

        questionTextView.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
        }

        cheatButton.setOnClickListener {
            val theAnswer = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, theAnswer)

            // Check which version the device and run the function accordingly
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val options = ActivityOptions.makeClipRevealAnimation(it, 0,0, it.width, it.height)
                startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
            } else {
                startActivityForResult(intent, REQUEST_CODE_CHEAT)
            }
        }

        updateQuestion()
        val scoreString = getString(R.string.score_text, quizViewModel.score, quizViewModel.questionCount)
        scoreTextView.text = scoreString
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: called")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState")

        outState.putInt(KEY_INDEX, quizViewModel.currentIndex)
        outState.putInt(KEY_SCORE, quizViewModel.score)
        outState.putInt(KEY_ANSWERED, quizViewModel.answeredCount)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: called")
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)

        disableAnswerButton()
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer

        // Show Toast
        val messageResId = when {
            quizViewModel.isCheater -> R.string.judgement_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()

        // Add Score
        if (correctAnswer == userAnswer) {
            quizViewModel.addScore()
        }

        val scoreString = getString(R.string.score_text, quizViewModel.score, quizViewModel.questionCount)
        scoreTextView.text = scoreString

        quizViewModel.isCheater = false
    }

    private fun disableAnswerButton() {
        if (quizViewModel.isCurrentQuestionAnswered) {
            trueButton.isEnabled = false
            falseButton.isEnabled = false
            cheatButton.isEnabled = false
        } else {
            trueButton.isEnabled = true
            falseButton.isEnabled = true
            cheatButton.isEnabled = true
        }
    }

    private fun checkIsAllAnswered() {
        if (quizViewModel.answeredCount == quizViewModel.questionCount) {
            Toast.makeText(this, R.string.finish_text, Toast.LENGTH_LONG).show()
        }
    }
}
