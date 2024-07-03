package com.example.idea1

import android.widget.TextView
import kotlin.random.Random

class MathProblemGenerator {

    var correctAnswer: Int = 0

    fun generateRandomMathProblem(mathProblemTextView: TextView) {
        val num1 = Random.nextInt(1, 10)
        val num2 = Random.nextInt(1, 10)
        correctAnswer = num1 * num2
        mathProblemTextView.text = "$num1 x $num2 = ?"
    }

    fun checkAnswer(answer: String): Boolean {
        return answer == correctAnswer.toString()
    }
}
