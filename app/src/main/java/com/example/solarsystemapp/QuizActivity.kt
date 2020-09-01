package com.example.solarsystemapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_quiz.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QuizActivity : BaseActivity() {

    private val mQuestionLibrary = QuestionLibrary()
    private var mQuestionView: TextView? = null
    private var mQuestionNumView: TextView? = null
    lateinit var mConfirmButton: Button
    lateinit var mButtonOption1: CheckBox
    lateinit var mButtonOption2: CheckBox
    lateinit var mButtonOption3: CheckBox
    lateinit var correctText: TextView
    lateinit var wrongText: TextView
    lateinit var tryAgainButton: Button
    lateinit var shareButton: Button
    private var mAnswer: String? = null
    private var mScore = 0
    private var mQuestionNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        updateTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        mQuestionView = findViewById(R.id.question)
        mQuestionNumView = findViewById(R.id.questionNumText)
        correctText = findViewById(R.id.correctText)
        wrongText = findViewById(R.id.wrongText)
        tryAgainButton = findViewById(R.id.againButton)
        shareButton = findViewById(R.id.shareButton)
        mConfirmButton = findViewById(R.id.confirmButton)
        mButtonOption1 = findViewById(R.id.option1Button)
        mButtonOption2 = findViewById(R.id.option2Button)
        mButtonOption3 = findViewById(R.id.option3Button)
        updateQuestion()
        resultsLayout.visibility = View.INVISIBLE

        mButtonOption1.setOnClickListener {
            if (mButtonOption1.isChecked == true){
                mButtonOption2.isChecked = false
                mButtonOption3.isChecked = false
            }
        }
        mButtonOption2.setOnClickListener {
            if (mButtonOption2.isChecked == true){
                mButtonOption1.isChecked = false
                mButtonOption3.isChecked = false
            }
        }
        mButtonOption3.setOnClickListener {
            if (mButtonOption3.isChecked == true){
                mButtonOption2.isChecked = false
                mButtonOption1.isChecked = false
            }
        }

        mConfirmButton.setOnClickListener {

            if (mQuestionNumber <= 10) {
                if (mButtonOption1.isChecked || mButtonOption2.isChecked || mButtonOption3.isChecked){
                    if (mButtonOption1.isChecked){
                        if (mButtonOption1.text == mAnswer) {
                            mScore++
                            correctText.visibility = View.VISIBLE
                            wrongText.visibility = View.INVISIBLE
                        } else {
                            correctText.visibility = View.INVISIBLE
                            wrongText.visibility = View.VISIBLE
                        }
                    } else if (mButtonOption2.isChecked){
                        if (mButtonOption2.text == mAnswer) {
                            mScore++
                            correctText.visibility = View.VISIBLE
                            wrongText.visibility = View.INVISIBLE
                        } else {
                            correctText.visibility = View.INVISIBLE
                            wrongText.visibility = View.VISIBLE
                        }
                    } else if (mButtonOption3.isChecked){
                        if (mButtonOption3.text == mAnswer) {
                            mScore++
                            correctText.visibility = View.VISIBLE
                            wrongText.visibility = View.INVISIBLE
                        } else {
                            correctText.visibility = View.INVISIBLE
                            wrongText.visibility = View.VISIBLE
                        }
                    }
                    updateQuestion()
                    mButtonOption1.isChecked = false
                    mButtonOption2.isChecked = false
                    mButtonOption3.isChecked = false
                } else Toast.makeText(getApplicationContext(),"Please enter an answer",Toast.LENGTH_SHORT).show();

            }

        }

        tryAgainButton.setOnClickListener {
            resultsLayout.visibility = View.GONE
            correctText.visibility = View.INVISIBLE
            wrongText.visibility = View.INVISIBLE
            mQuestionNumber = 0
            mScore = 0
            updateQuestion()
        }
        shareButton.setOnClickListener {
            val rootView = window.decorView.findViewById<View>(android.R.id.content)
            val screenshot = getScreenShot(rootView)
            saveBitmap(screenshot!!)
            shareResult()
        }
    }

    private fun updateQuestion() {
        if (mQuestionNumber < 10){
            mQuestionView!!.text = mQuestionLibrary.getQuestion(mQuestionNumber)
            mButtonOption1.text = mQuestionLibrary.getOption1(mQuestionNumber)
            mButtonOption2.text = mQuestionLibrary.getOption2(mQuestionNumber)
            mButtonOption3.text = mQuestionLibrary.getOption3(mQuestionNumber)
            mAnswer = mQuestionLibrary.getCorrectAnswer(mQuestionNumber)
            mQuestionNumView!!.text = (mQuestionNumber + 1).toString()
        } else {
            resultsLayout.visibility = View.VISIBLE
            resultsText.text = "You Got $mScore/10 Correct!"
        }
        mQuestionNumber++
    }

    private fun getScreenShot(view: View): Bitmap? {
        val screenView = view.rootView
        screenView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(screenView.drawingCache)
        screenView.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun saveBitmap(bitmap: Bitmap) {
        try {
            val cachePath = File(applicationContext.cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream = FileOutputStream("$cachePath/image.png") // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun shareResult(){
        val imagePath = File(applicationContext.getCacheDir(), "images")
        val newFile = File(imagePath, "image.png")
        val contentUri: Uri? =
            FileProvider.getUriForFile(applicationContext, "com.example.solarsystemapp.fileprovider", newFile)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, "I just got " + mScore + "/10 in the Solar System Quiz!!")
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }
    }
}