package com.drdlee.geoquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

const val EXTRA_ANSWER_SHOWN = "com.drdlee.geoquiz.answer_shown"
private const val EXTRA_THE_ANSWER = "com.drdlee.geoquiz.answer_is_true"
private const val KEY_ANSWER_STATE = "cheat"

class CheatActivity : AppCompatActivity() {

    private lateinit var answerTextView: TextView
    private lateinit var showAnswerButton: Button
    private lateinit var apiLevelTextView: TextView

    private var theAnswer = false
    private var isAnswerShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cheat)

        theAnswer = intent.getBooleanExtra(EXTRA_THE_ANSWER, false)

        val answerState = savedInstanceState?.getBoolean(KEY_ANSWER_STATE, false) ?: false
        isAnswerShown = answerState
        if (isAnswerShown) setAnswerShownResult()

        answerTextView = findViewById(R.id.answer_text_view)
        showAnswerButton = findViewById(R.id.show_answer_button)
        apiLevelTextView = findViewById(R.id.api_level_text_view)

        init()
        setListener()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(KEY_ANSWER_STATE, this.isAnswerShown)
    }

    private fun setAnswerShownResult() {
        val data = Intent().apply {
            putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown)
        }
        setResult(Activity.RESULT_OK, data)
    }

    private fun init() {
        if (isAnswerShown) {
            val answerText = when {
                theAnswer -> R.string.true_button
                else -> R.string.false_button
            }
            answerTextView.setText(answerText)
        }

        val apiText = getString(R.string.api_level_text, Build.VERSION.SDK_INT)
        apiLevelTextView.text = apiText
    }

    private fun setListener() {
        showAnswerButton.setOnClickListener {
            val answerText = when {
                theAnswer -> R.string.true_button
                else -> R.string.false_button
            }
            answerTextView.setText(answerText)
            isAnswerShown = true
            setAnswerShownResult()
        }
    }

    companion object {
        fun newIntent(packageContext: Context, theAnswer: Boolean): Intent {
            return Intent(packageContext, CheatActivity::class.java).apply {
                putExtra(EXTRA_THE_ANSWER, theAnswer)
            }
        }
    }
}
