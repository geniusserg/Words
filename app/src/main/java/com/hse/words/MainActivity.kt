package com.hse.words

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    var listWords: ArrayList<String> = ArrayList<String>()
    var listWordsWrong: ArrayList<String> = ArrayList<String>()
    var score: Int = 0
    var buttonMain : Button? = null
    var newGame : Button? = null
    var checkButton: Button? = null
    var exitButton: Button? = null
    var newWord: Button? = null
    lateinit var dictionary: Set<String>
    lateinit var longWordsDictionary: Array<String>
    lateinit var longWord: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonMain = findViewById(R.id.word)
        buttonMain?.setText("Loading...")
        newGame = findViewById(R.id.new_game)
        checkButton = findViewById(R.id.check)
        checkButton?.setOnClickListener { check() }
        checkButton?.setEnabled(false)
        newGame?.setEnabled(false)
        newGame?.setOnClickListener { update() }
        newWord = findViewById(R.id.new_word)
        newWord?.setEnabled(false)
        newWord?.setOnClickListener { listWords.add("а") }
        exitButton = findViewById(R.id.exit)
        exitButton?.setOnClickListener { this.finishAndRemoveTask() }
        GlobalScope.launch{
            dictionary = loadDictionaryAsSet("words.txt").await()
            longWordsDictionary = loadDictionaryAsArray("wordsLong.txt").await()
            update()
        }
        listWords.add("а")
    }

    fun loadDictionaryAsSet(resource: String): Deferred<SortedSet<String>>{
        return GlobalScope.async {
            val res = assets.open(resource)
            val resstd = res.bufferedReader().readLines()
            val setOfWords = resstd.toSortedSet()
            return@async setOfWords
        }
    }

    fun loadDictionaryAsArray(resource: String): Deferred<Array<String>>{
        return GlobalScope.async {
            val res = assets.open(resource)
            val resstd = res.bufferedReader().readLines()
            val setOfWords = resstd.toTypedArray()
            return@async setOfWords
        }
    }

    fun newWord(word: String): Boolean{
        if (match(word)) {
            listWords.add(word)
            return true
        }
        return false
    }

    fun update(){
        newGame?.setEnabled(false)
        checkButton?.setEnabled(false)
        listWords.clear()
        listWordsWrong.clear()
        score = 0
        GlobalScope.launch {
            longWord = loadRandomWord()
            runOnUiThread({ buttonMain?.setText(longWord)
                newGame?.setEnabled(true)
                checkButton?.setEnabled(true)
                newWord?.setEnabled(true)
            })
        }
    }

    fun check(){
        newGame?.setEnabled(false);
        checkButton?.setEnabled(false)
        newWord?.setEnabled(false)
        GlobalScope.launch {
            if (listWords.size != 0) {
                for (i in listWords) {
                    if (match(i) && findWord(i)) {
                        score += 1
                    } else {
                        listWordsWrong.add(i)
                    }
                }
            }
            runOnUiThread(
                {newGame?.setEnabled(true)
                buttonMain?.setText(score.toString())
                })
        }
    }

    fun match(substring : String): Boolean{
        if (substring.equals(longWord)){ return false }
        val cMap = HashMap<Char, Int>()
        for (s in substring){
            val temp_count = cMap.get(s)
            if (temp_count != null){
                cMap.put(s, temp_count+1)
            }
            else{
                cMap.put(s, 1)
            }
        }
        for (s in longWord){
            val temp_count = cMap.get(s)
            if (temp_count != null){
                cMap.put(s, temp_count-1)
            }
        }
        val result = cMap.filterValues { a -> a > 0 }
        return result.isEmpty()
    }

    fun findWord(word: String): Boolean{
        return dictionary.contains(word)
    }

    fun loadRandomWord() : String{
        return longWordsDictionary[Random.nextInt(28460)]
    }


}