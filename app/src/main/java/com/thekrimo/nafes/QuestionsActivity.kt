package com.thekrimo.nafes

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.thekrimo.nafes.databinding.ActivityGetStartedBinding
import com.thekrimo.nafes.databinding.ActivityQuestionsBinding

class QuestionsActivity : BaseActivity() {
    private lateinit var binding: ActivityQuestionsBinding
    private var currentQuestionIndex = 0
    private var backPressedOnce = false
    val questionsList = listOf(
        Question(
            "What is your gender?",
            listOf(
                Answer("Male", 0, 1),
                Answer("Female", 1, 1)
            )
        ),
        Question(
            "How old are you?",
            listOf(
                Answer("", -2, 2) // Numerical Answer
            )
        ),
        Question(
            "What is your residency? (Please specify your state of residence)",
            listOf(
                Answer("", -2, 3) // Numerical Answer (Based on the state number)
            )
        ),
        Question(
            "Have you ever had therapy before?",
            listOf(
                Answer("Yes", -1, 4), // If Yes, next question
                Answer("No", 2, 5)    // If No, next question
            )
        ),
        Question(
            "Was it helpful?",
            listOf(
                Answer("Yes", 0, 5),
                Answer("No", 1, 5)
            )
        ),
        Question(
            "Do you consider yourself religious? (Do you actively practice your religion as it should be?)",
            listOf(
                Answer("Yes", -1, 6), // If yes, next question
                Answer("No", 3, 7)    // If No, next question
            )
        ),
        Question(
            "How often do you practice?",
            listOf(
                Answer("Daily", 0, 7),
                Answer("Several times a week", 1, 7),
                Answer("Occasionally", 2, 7)
            )
        ),
        Question(
            "What is your parental status?",
            listOf(
                Answer("Both parents alive", -1, 8), 
                Answer("Divorced parents", -1, 9),
                Answer("Father deceased", -1, 10),
                Answer("Mother deceased", -1, 11),
                Answer("Both parents deceased", 12, 12)
            )
        ),
        Question(
            "How would you describe your relationship with them?",
            listOf(
                Answer("Good", 0, 12),
                Answer("Normal", 1, 12),
                Answer("Poor", 2, 12)
            )
        ),
        Question(
            "How would you describe your relationship with them?",
            listOf(
                Answer("Good", 3, 12),
                Answer("Normal", 4, 12),
                Answer("Poor", 5, 12)
            )
        ),
        Question(
            "How would you describe your relationship with them?",
            listOf(
                Answer("Good", 6, 12),
                Answer("Normal", 7, 12),
                Answer("Poor", 8, 12)
            )
        ),
        Question(
            "How would you describe your relationship with them?",
            listOf(
                Answer("Good", 9, 12),
                Answer("Normal", 10, 12),
                Answer("Poor", 11, 12)
            )
        ),
        Question(
            "How can you describe your status?",
            listOf(
                Answer("Employed", 0, 13),
                Answer("Student", 1, 13),
                Answer("None of them", 2, 13)
            )
        ),
        Question(
            "How can you describe your relationship status?",
            listOf(
                Answer("Married", 0, 14),
                Answer("Single", 1, 14),
                Answer("Engaged or in a relationship", 2, 14),
                Answer("Divorced", 3, 14)
            )
        ),
        Question(
            "What do you need the most from a therapist?",
            listOf(
                Answer("Help", 0, 15),
                Answer("Listening", 1, 15),
                Answer("Solutions finding", 2, 15)
            )
        ),
        Question(
            "Do you have any preferences for your therapist?",
            listOf(
                Answer("Male", 0, 16),
                Answer("Female", 1, 16),
                Answer("Same Age", 2, 16),
                Answer("Old and experienced", 3, 16),
                Answer("Religious", 4, 16),
                Answer("Not from your state", 5, 16)
            )
        ),
        Question(
            "How would you describe your current financial situation?",
            listOf(
                Answer("Good", 0, 17),
                Answer("Poor", 1, 17)
            )
        ),
        Question(
            "How would you rate your physical health?",
            listOf(
                Answer("Good", 0, 18),
                Answer("Fair", 1, 18),
                Answer("Poor", 2, 18)
            )
        ),
        Question(
            "How would you describe your eating behavior?",
            listOf(
                Answer("Good", 0, 19),
                Answer("Fair", 1, 19),
                Answer("Poor", 2, 19)
            )
        ),
        Question(
            "How would you describe your feelings?",
            listOf(
                Answer("Sad", 0, 20),
                Answer("Grieving", 1, 20),
                Answer("Depressed", 2, 20),
                Answer("Happy", 3, 20),
                Answer("I don't know", 4, 20)
            )
        ),
        Question(
            "Have you had any suicidal thoughts?",
            listOf(
                Answer("Yes", -1, 21), // If yes, next question
                Answer("No", 3, 22)    // If No, next question
            )
        ),
        Question(
            "How often?",
            listOf(
                Answer("Too much recently", 0, 22),
                Answer("Sometimes", 1, 22),
                Answer("Rarely", 2, 22)
            )
        ),
        Question(
            "Where do you see yourself having a problem?",
            listOf(
                Answer("Hopefulness", 0, 23),
                Answer("Energy levels", 1, 23),
                Answer("Excitement", 2, 23),
                Answer("Self-esteem", 3, 23),
                Answer("Motivation", 4, 23),
                Answer("Stress management", 5, 23),
                Answer("Relationships", 6, 23),
                Answer("Work-life balance", 7, 23),
                Answer("Coping with emotions", 8, 23),
                Answer("Decision-making", 9, 23),
                Answer("Time management", 10, 23),
                Answer("Setting boundaries", 11, 23),
                Answer("Communication skills", 12, 23),
                Answer("Physical health", 13, 23),
                Answer("Mental health", 14, 23)
            )
        ),
        Question(
            "Do you consume alcohol or drugs?",
            listOf(
                Answer("Yes", 0, 24),
                Answer("No", 1, 24),
                Answer("I was, but I am clean now", 2, 24)
            )
        )
    )
    private val userAnswers = mutableListOf<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showQuestion(currentQuestionIndex)

        // age dropdown list
        val ageAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.age_array,
            android.R.layout.simple_spinner_item
        )
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.ageSpinner.adapter = ageAdapter

        // state dropdown list
        val stateAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.state_array,
            android.R.layout.simple_spinner_item
        )
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.stateSpinner.adapter = stateAdapter


        binding.back.setOnClickListener { onBackPressed() }
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

        if (index == 1) {
            // Handle age selection
            binding.answersRadioGroup.visibility = View.GONE
            binding.ageStateLayout.visibility = View.VISIBLE
            binding.stateSpinner.visibility = View.GONE
            binding.ageSpinner.visibility = View.VISIBLE

            binding.nextButton.setOnClickListener {
                val age = binding.ageSpinner.selectedItem.toString()
                userAnswers.add( age.toInt())
                currentQuestionIndex = 2
                showQuestion(currentQuestionIndex)
            }
        } else if (index == 2) {
            // Handle state selection
            binding.answersRadioGroup.visibility = View.GONE
            binding.ageStateLayout.visibility = View.VISIBLE
            binding.stateSpinner.visibility = View.VISIBLE
            binding.ageSpinner.visibility = View.GONE
            binding.donthave.visibility = View.GONE
            binding.nextButton.setOnClickListener {
                val statePosition = binding.stateSpinner.selectedItemPosition
                val stateNumber = statePosition + 1
                userAnswers.add(stateNumber)
                currentQuestionIndex = 3
                showQuestion(currentQuestionIndex)
            }
        } else {
            // Show regular question and answers
            binding.answersRadioGroup.visibility = View.VISIBLE
            binding.ageStateLayout.visibility = View.GONE
            for (answer in question.answers) {
                val button = Button(this)
                button.text = answer.answerText
                button.setBackgroundResource(R.drawable.bttn)
                button.layoutParams = layoutParams

                button.setOnClickListener {
                    if(answer.weight!=-1)userAnswers.add( answer.weight)
                    currentQuestionIndex = answer.nextQuestionIndex
                    if (currentQuestionIndex == 69420) {
                        openLink("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    }
                    if (currentQuestionIndex == 24) {
                        Log.d("UserAnswers", userAnswers.toString())
                        startActivity(Intent(this, MainActivity::class.java))


                    }
                    if ((currentQuestionIndex < questionsList.size) && currentQuestionIndex != -1) {
                        showQuestion(currentQuestionIndex)
                    } else {
                        // Handle end of questions
                    }
                }
                binding.answersRadioGroup.addView(button)
            }
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
        } else {
            if (backPressedOnce) {
                super.onBackPressed()
                return
            }

            backPressedOnce = true
            showToast("Press back again")
            Handler(Looper.getMainLooper()).postDelayed({
                backPressedOnce = false
            }, 2000) // Reset the flag after 2 seconds
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        startActivity(intent)
    }
}