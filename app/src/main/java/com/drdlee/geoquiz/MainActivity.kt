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
private const val KEY_CHEATED = "cheated_count"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {

    private lateinit var questionTextView: TextView
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var cheatButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var cheatedTexView: TextView

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: called")
        setContentView(R.layout.activity_main)

        initSavedInstance(savedInstanceState)
        initViews()
        initListeners()

        updateQuestion()
        updateScore()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }

        if (quizViewModel.isCheater) {
            quizViewModel.addCheatCount()
            updateScore()
            disableAnswerButton()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState")

        outState.putInt(KEY_INDEX, quizViewModel.currentIndex)
        outState.putInt(KEY_SCORE, quizViewModel.score)
        outState.putInt(KEY_ANSWERED, quizViewModel.answeredCount)
        outState.putInt(KEY_CHEATED, quizViewModel.cheatCount)
    }

    private fun initSavedInstance(savedInstanceState: Bundle?) {
        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        val score = savedInstanceState?.getInt(KEY_SCORE, 0) ?: 0
        val answeredCount = savedInstanceState?.getInt(KEY_ANSWERED, 0) ?: 0
        val cheatedCount = savedInstanceState?.getInt(KEY_CHEATED, 0) ?: 0
        quizViewModel.apply {
            this.currentIndex = currentIndex
            this.score = score
            this.answeredCount = answeredCount
            this.cheatCount = cheatedCount
        }
    }

    private fun initViews() {
        questionTextView = findViewById(R.id.question_text_view)
        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        prevButton = findViewById(R.id.prev_button)
        cheatButton = findViewById(R.id.cheat_button)
        scoreTextView = findViewById(R.id.score_text_view)
        cheatedTexView = findViewById(R.id.cheat_text_view)
    }

    private fun initListeners() {
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
            if (quizViewModel.cheatCount >= 3) {
                Toast.makeText(this, R.string.cheat_max_toast, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)

        disableAnswerButton()
    }

    private fun updateScore() {
        val scoreString = getString(R.string.score_text, quizViewModel.score, quizViewModel.questionCount)
        val cheatedString = getString(R.string.cheat_text, quizViewModel.cheatCount)
        scoreTextView.text = scoreString
        cheatedTexView.text = cheatedString
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

        updateScore()
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
            if (quizViewModel.cheatCount >= 3) {
                cheatButton.isEnabled = false
                return
            }
            cheatButton.isEnabled = true
        }
    }

    private fun checkIsAllAnswered() {
        if (quizViewModel.answeredCount == quizViewModel.questionCount) {
            Toast.makeText(this, R.string.finish_text, Toast.LENGTH_LONG).show()
        }
    }
}
