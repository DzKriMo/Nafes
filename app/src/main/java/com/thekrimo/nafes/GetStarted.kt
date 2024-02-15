package com.thekrimo.nafes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.thekrimo.nafes.databinding.ActivityGetStartedBinding
import kotlin.math.log

class GetStarted : BaseActivity() {
    private lateinit var binding: ActivityGetStartedBinding
    private var currentQuestionIndex = 0
    private var backPressedOnce = false
    private val questionsList = listOf(
        Question(
            "What Type Of Therapy Are You Looking For?.",
            listOf(
                Answer("Individual  (for Myself)", 1),
                Answer("Couples  (For Me And My Partner) ", 1),
                Answer("Teen  (For My Child) ", 1)
            )
        ),
        Question(
            "What Is Your Gender?",
            listOf(
                Answer("Male", 2),
                Answer("Female", 2)
            )
        ),

        Question(
            "What Is Your Age Groupe?",
            listOf(
                Answer("18-25", 3),
                Answer("25-35", 3),
                Answer("35-50", 3),
                Answer("50+", 3)
            )
        ),

        Question(
            "What Is Your relationship status?",
            listOf(
                Answer("Single", 4),
                Answer("Married", 4),
                Answer("Divorced", 4),
                Answer("Widowed", 4)
            )
        ),

        Question(
            "Do You Consider Yourself To Be Religious?",
            listOf(
                Answer("Yes", 5),
                Answer("Not Really", 5),
                Answer("No", 5)
            )
        ),

        Question(
            "Have You Ever Been To Therapy Before?",
            listOf(
                Answer("Yes", 6),
                Answer("No", 6)
            )
        ),


        Question(
            "What Led You To Consider Therapy Today?",
            listOf(
                Answer("I've Been Feeling Depressed", 7),
                Answer("I Feel Anxious or overwhelmed", 7),
                Answer("My mood is interfering with my job/school performance", 7),
                Answer("I Struggle with building or maintaining relationships", 7),
                Answer("I can't find purpose and meaning in my life", 7),
                Answer("I'm grieving", 7),
                Answer("I have experienced Trauma ", 7),
                Answer("I need to talk through a specific challenge", 7),
                Answer("I want to gain self confidence", 7),
                Answer("I want to improve myself but i don't know where to start", 7),
                Answer("Recommended to me (friend, family, doctor)", 7),
                Answer("Just Exploring", 7),
                Answer("Other", 7),
            )
        ),

        Question(
            "I'm Looking For A Therapist Who...",
            listOf(
                Answer("Listens", 8),
                Answer("Explores my past", 8),
                Answer("Teaches Me new Skills", 8),
                Answer("Challenges my beliefs", 8),
                Answer("Assigns me Homework", 8),
                Answer("Guides me to Set Goals", 8),
                Answer("Proactively checks on me ", 8),
                Answer("Other", 8),
                Answer("I don't Know", 8),

            )
        ),


        Question(
            "When Was The Last Time You Thought About Suicide?",
            listOf(
                Answer("Never", 9),
                Answer("Over a year Ago", 9),
                Answer("Over 3 months ago", 9),
                Answer("Over a month ago", 9),
                Answer("Over 2 weeks ago", 9),
                Answer("In the last 2 Weeks", 69420),


                )
        ),

    





        // The Questions and answers achref prepares
    )
    private val userAnswers = mutableListOf<Response>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.signup.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            for (response in userAnswers) {
                Log.d("UserAnswers", "Question: ${response.question}, Answer: ${response.answer}")
            }

        }



        showQuestion(currentQuestionIndex)



    }

    private fun showQuestion(index: Int) {

            val question = questionsList[index]
            binding.questionText.text = question.questionText
            binding.answersRadioGroup.removeAllViews()
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.button_top_margin)
            for (answer in question.answers) {
                val button = Button(this)
                button.text = answer.answerText
                button.setBackgroundResource(R.drawable.bttn)
                button.layoutParams = layoutParams

                button.setOnClickListener {
                    // Save the question and selected answer in the HashMap for achref to work with later
                    userAnswers.add(Response(question.questionText, answer.answerText))
                    // next question 3la 7sab l answer
                    currentQuestionIndex = answer.nextQuestionIndex
                    if (currentQuestionIndex == 69420) {
                        startActivity(Intent(this, suicide::class.java))

                    }
                    if ((currentQuestionIndex < questionsList.size) && currentQuestionIndex != -1) {
                        showQuestion(currentQuestionIndex)
                    } else {
                        // once the questions are done nro7o llkhdma t3 l ai and finding the therapist
                    }
                }
                binding.answersRadioGroup.addView(button)
            }
        }


    override fun onBackPressed() {
        if (currentQuestionIndex != 0) {
            currentQuestionIndex -= 1
            if (userAnswers.isNotEmpty()) {
                userAnswers.removeAt(userAnswers.size - 1)
            }
            showQuestion(currentQuestionIndex)
            return
        }
        else{

        if (backPressedOnce) {
            super.onBackPressed()
            return
        }

        backPressedOnce = true
        showToast("Press back again to exit")
        Handler(Looper.getMainLooper()).postDelayed({
            backPressedOnce = false
        }, 2000) // Reset the flag after 2 seconds
    }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }




}