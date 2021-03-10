package com.hse.words

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.io.File

class MainActivity : AppCompatActivity() {
    var globalString  :String = ""
    var dictionary: File? = null
    var listWords: List<String>? = null
    var score: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun addWord(word: String): Boolean{
        var result: Boolean = true
        result = result and(match(word))

        return false
    }

    fun update(){
        score = 0
        loadRandomWord()
    }

    fun check(){

    }

    fun match(substring : String): Boolean{
        return false
    }

    fun findWord(word: String): Boolean{
        return false
    }

    fun loadRandomWord(){
        globalString = "Sample"
    }
}