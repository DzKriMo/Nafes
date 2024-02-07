package com.thekrimo.nafes

data class Answer(
    val answerText: String,
    val nextQuestionIndex: Int
)
data class Response(
    val question: String,
    val answer: String
)

data class Question(
    val questionText: String,
    val answers: List<Answer>
)
