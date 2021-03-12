package com.hse.words

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
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
    var longWord: String? = null
    lateinit var newWordAlert: AlertDialog
    var listWordsView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch{
            longWordsDictionary = loadDictionaryAsArray("wordsLong.txt").await()
            update()
            runOnUiThread { checkButton?.setEnabled(false) }
            dictionary = loadDictionaryAsSet("words.txt").await()
            runOnUiThread { checkButton?.setEnabled(true) }
        }
        setContentView(R.layout.activity_main)
        newWordAlertInit()
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
        newWord?.setOnClickListener {
            newWordAlert.setMessage("Enter new subword from '"+longWord?.toUpperCase(Locale.getDefault())+"' :");
            newWordAlert.show() }
        exitButton = findViewById(R.id.exit)
        exitButton?.setOnClickListener { this.finishAndRemoveTask() }
        listWordsView = findViewById(R.id.list_words)
        listWordsView?.setBackgroundColor(Color.GREEN)
        val newWTmp  = TextView(this)
        newWTmp.setText("sdgashjawrhwraywertrwtwrtwerwerwer")
        newWTmp.setTextColor(Color.WHITE)
        listWordsView?.addFooterView(newWTmp)
    }

    fun newWordAlertInit(){
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("New Word")
        alert.setMessage("Enter new subword from :")
        val input = EditText(this)
        alert.setView(input)
        alert.setCancelable(false)

        val alertWarn: AlertDialog = AlertDialog.Builder(this).setTitle("Wrong, try agagin!").create()

        alert.setPositiveButton("Ok", { dialog, whichButton ->
            val value: String = input.getText().toString().toLowerCase(Locale.getDefault()).replace(" ","")
            if (!newWord(value)){
                alertWarn.show()
            }
            input.setText("")
        })
        alert.setNegativeButton("Cancel", { dialog, whichButton ->
            dialog.cancel()
        })
        newWordAlert = alert.create()
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
        when (match(word)){
            true -> {
                listWords.add(word)

            }
            false -> return false
        }
        return true
    }

    fun update(){
        newGame?.setEnabled(false)
        checkButton?.setEnabled(false)
        listWords.clear()
        listWordsWrong.clear()
        score = 0
        GlobalScope.launch {
            longWord = loadRandomWord()
            runOnUiThread({
                buttonMain?.setText(longWord)
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
                    {
                        newGame?.setEnabled(true)
                        buttonMain?.setText("YUOR SCORE IS : "+score.toString())
                    })
        }
    }

    fun match(substring: String): Boolean{
        if (substring.equals(longWord)){ return false }
        val cMap = HashMap<Char, Int>()
        for (s in substring){
            val temp_count = cMap.get(s)
            if (temp_count != null){
                cMap.put(s, temp_count + 1)
            }
            else{
                cMap.put(s, 1)
            }
        }
        for (s in longWord?:""){
            val temp_count = cMap.get(s)
            if (temp_count != null){
                cMap.put(s, temp_count - 1)
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