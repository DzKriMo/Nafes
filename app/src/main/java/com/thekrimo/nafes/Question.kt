package com.thekrimo.nafes

data class Answer(
    val answerText: String,
    val weight:Int,
    val nextQuestionIndex: Int
)


data class Question(
    val questionText: String,
    val answers: List<Answer>
)
data class Response(
    val question: String,
    val answer: String
)