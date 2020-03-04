# GeoQuiz
**Summary of "Android Programming: The Big Nerd Ranch Guide, 4th Edition"**

###### Chapter 1 - 7

---

## Activity Lifecycle

- app initialized
  - `onCreate(...)` when activity initialized and/or configuration change

- app starting
  - `onStart()` not visible
  - `onResume()` visible and interactive

- app to go idle in background
  - `onPause()` visible but not interactive
  - `onStop()` not visible

- app exists
  - `onDestroy()` exit

---

## ViewModel

to store state in memory, but not when process death
```kotlin
class QuizViewModel: ViewModel() {}
```

link it to Activity
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    ...
    setContentView(R.layout.activity_main)
    
    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }
    ...
}
```

<br/>

**Handling process death**
> process death is when OS wipe the Activity and ViewModel from memory when app is not in active use, to allocate memory for other running & visible app

> to simulate process death, on android phone go to Settings -> Developer options -> Don't keep activities

store data in Activity Record in the OS with `onSaveInstanceState(Bundle)` it will be called after `onPause()` before `onStop()`

to store data in `onSaveInstanceState(Bundle)` will need a Key to recognize the data and call which kind of data type to store
```kotlin
override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putInt(KEY_INDEX, quizViewModel.currentIndex)
}
```

and then when user to start app again after process death, we will have to restore the stored data to Activity and ViewModel again on `onCreate(...)`
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    ...
    setContentView(R.layout.activity_main)
    
    val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
    quizViewModel.currentIndex = currentIndex
}
```

---

## Intent

Intent are actually pass through Activity Manager in OS the Activity Manager will start the target intent, how will Activity Manager know which intent? because we declared it in Android Manifest

**to pass data through intent *without waiting value* to pass back**
```kotlin
cheatButton.setOnClickListener {
    val intent = Intent(this, CheatActivity::class.java)
    intent.putExtra(EXTRA_THE_ANSWER, theAnswer)
    startActivity(intent)
}
```

to receive intent in target activity
```kotlin
class CheatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ...
        theAnswer = intent.getBooleanExtra(EXTRA_THE_ANSWER, false)
        ...
    }
}
```

**to pass data through intent *and wait a value* to pass back**
```kotlin
cheatButton.setOnClickListener {
    val intent = Intent(this, CheatActivity::class.java)
    intent.putExtra(EXTRA_THE_ANSWER, theAnswer)
    startActivity(intent, REQUEST_CODE_CHEAT)
}
```

to receive intent in target activity are **the same as above**, only on target intent we have to `setResult()` to pass back to source activity
```kotlin
private fun setAnswerShownResult(isAnswerShown: Boolean) {
    val data = Intent().apply {
        putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown)
    }
    setResult(Activity.RESULT_OK, data)
}
```

after that, on source activity we have to override `onActivityResult(...)` to receive the result passed back, with the request code that we pass to target
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode != Activity.RESULT_OK) {
        return
    }

    if (requestCode == REQUEST_CODE_CHEAT) {
        quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
    }
}
```




