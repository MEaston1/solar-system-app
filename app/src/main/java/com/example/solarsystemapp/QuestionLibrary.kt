package com.example.solarsystemapp

class QuestionLibrary {
    private val mQuestions = arrayOf(
        "What is the closest planet to the sun?",
        "Which Galaxy is our Solar System in?",
        "What object is between Mars and Jupiter?",
        "How many Moons does Mars have?",
        "Pluto is a...",
        "What is the largest Planet in our Solar System?",
        "What type of Planet is Venus?",
        "Which Planets is the furthest from the Sun",
        "Which of these Planets does not have Rings?",
        "What percent of the mass in our Solar System does the sun take up?"
    )
    private val mChoices =
        arrayOf(
            arrayOf("Mercury", "Venus", "Mars"),
            arrayOf("Andromeda", "Milky Way", "Twirl"),
            arrayOf("Saturn", "Asteroid Belt", "Neptune"),
            arrayOf("1", "2", "3"),
            arrayOf("Planet", "Moon", "Dwarf Planet"),
            arrayOf("Neptune", "Jupiter", "Venus"),
            arrayOf("Terrestrial", "Gas Giant", "Ice Giant"),
            arrayOf("Uranus", "Neptune", "Saturn"),
            arrayOf("Saturn", "Uranus", "Mercury"),
            arrayOf("99", "75", "50")
        )
    private val mCorrectAnswers =
        arrayOf("Mercury", "Milky Way", "Asteroid Belt", "2", "Dwarf Planet", "Jupiter", "Terrestrial", "Neptune", "Mercury", "99")

    fun getQuestion(a: Int): String {
        return mQuestions[a]
    }

    fun getOption1(a: Int): String {
        return mChoices[a][0]
    }

    fun getOption2(a: Int): String {
        return mChoices[a][1]
    }

    fun getOption3(a: Int): String {
        return mChoices[a][2]
    }

    fun getCorrectAnswer(a: Int): String {
        return mCorrectAnswers[a]
    }
}