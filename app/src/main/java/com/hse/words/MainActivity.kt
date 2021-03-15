package com.hse.words

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
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
    var listWordsView: TableLayout? = null
    var loadedState: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //load dictionaries in coroutine
        GlobalScope.launch{
            longWordsDictionary = loadDictionaryAsArray("wordsLong.txt").await()
            update()
            dictionary = loadDictionaryAsSet("words.txt").await()
            loadedState = !dictionary.isNullOrEmpty()
            runOnUiThread({checkButton?.setEnabled(loadedState)})
        }

        //load Views
        setContentView(R.layout.activity_main)
        newWordAlertInit()
        buttonMain = findViewById(R.id.word)
        buttonMain?.setText("Loading...")
        newGame = findViewById(R.id.new_game)
        checkButton = findViewById(R.id.check)
        checkButton?.setOnClickListener { check()
        checkButton?.setEnabled(false) }
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
    }


    // load new word input dialog
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

    // return loaded dictionari as a set from async coroutine result
    // load from set is needed as search at set is faster
    fun loadDictionaryAsSet(resource: String): Deferred<SortedSet<String>>{
        return GlobalScope.async {
            runOnUiThread({checkButton?.setEnabled(false)})
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


    // check and add new word
    fun newWord(word: String): Boolean{
        when (match(word)){
            true -> {
                if (!listWords.contains(word)) {
                    listWords.add(word)
                    addWordonBoard(word)
                }
            }
            false -> return false
        }
        return true
    }


    // find new long word
    fun update(){
        listWords.clear()
        listWordsWrong.clear()
        listWordsView?.removeAllViews()
        score = 0
        longWord = longWordsDictionary[Random.nextInt(28460)]
        runOnUiThread({
                buttonMain?.setText(longWord)
                newGame?.setEnabled(true)
                newWord?.setEnabled(true)
                checkButton?.isEnabled = loadedState
        })
    }


    //check each word from the list and prepare result
    fun check(){
        newGame?.setEnabled(false);
        checkButton?.setEnabled(false)
        newWord?.setEnabled(false)
        GlobalScope.launch {
            if (listWords.size != 0) {
                for (i in listWords) {
                    if (match(i) && dictionary.contains(i)) {
                        score += 1
                    } else {
                        listWordsWrong.add(i)
                    }
                }
            }
            runOnUiThread(
                    {
                        newGame?.setEnabled(true)
                        buttonMain?.setText("YOUR SCORE IS : "+score.toString())
                        markWordsOnBoard()
                    })
        }
    }

    // check that small word is a subset of long word
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

    // add word on TableLayout
    fun addWordonBoard(string: String){
        val tview = TextView(this)
        tview.setText(listWordsView?.childCount?.toString() + " > " + string)
        tview.setTextSize(20f)
        listWordsView?.addView(tview)
    }

    // if word is wrong - amke it RED, else - GREEN
    fun markWordsOnBoard(){
        for (i in (0..listWordsView!!.childCount)){
            val tr = listWordsView!!.getChildAt(i) as? TextView
            if (tr != null) {
                if (listWordsWrong.contains(listWords.removeAt(0))) {
                    tr.setTextColor(Color.RED)
                } else {
                    tr.setTextColor(Color.GREEN)
                }
            }
        }
    }

}