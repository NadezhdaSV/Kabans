package com.example.kabans

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.kabans.databinding.ActivityMainBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    lateinit var bindClass : ActivityMainBinding

    internal class Rarity(var probability: Double, var name: String){}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindClass = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindClass.root)

        val gift = bindClass.giftImage
        val photo = bindClass.kabanImage
        val getKaban = bindClass.getKabanText
        val resultKaban = bindClass.resultKabanText
        val statsButton = bindClass.statsButton
        var image = 0
        var text = ""
        var commonCounter = 0
        var rareCounter = 0
        var epicCounter = 0
        var legendaryCounter = 0

        val common = Rarity(Constants.commonProb, "common")
        val rare = Rarity(Constants.rareProb, "rare")
        val epic = Rarity(Constants.epicProb, "epic")
        val legendary = Rarity(Constants.legendaryProb, "legendary")

        val rarities = listOf(common, rare, epic, legendary)

        fun getRarity() : String {
            var cumulativeProbability = 0.0
            val p = Math.random()
            var name = ""
            for(rarity in rarities) {
                cumulativeProbability += rarity.probability
                if (p <= cumulativeProbability) {
                    name = rarity.name;
                    break
                }
            }
            return name
        }

        class NamedDrawable(val name: String, val drawable: Drawable) {
            override fun toString(): String = name
        }

        fun getAllDrawables(): List<NamedDrawable> {
            return R.drawable::class.java.fields.mapNotNull { field ->
                ResourcesCompat.getDrawable(resources, field.getInt(null), null)
                    ?.let { NamedDrawable(field.name, it) }
            }
        }

        val imagesWithRarityList = getAllDrawables()

        fun getImageWithRarity(rarity:String) : Int {
            val rarityPattern = rarity.toRegex()
            var result: Sequence<MatchResult>
            var imagesWithRarityAmount = 0
            imagesWithRarityList.forEach {
                result = rarityPattern.findAll(it.toString())
                result.forEach()
                {
                    imagesWithRarityAmount += 1
                }
            }
            return imagesWithRarityAmount
        }

//        val commonImagesAmount = readDataRarity("common")
//        val rareImagesAmount = readDataRarity("rare")
//        val epicImagesAmount = readDataRarity("epic")
//        val legendaryImagesAmount = readDataRarity("legendary")

        val commonImagesAmount = getImageWithRarity("common")
        val rareImagesAmount = getImageWithRarity("rare")
        val epicImagesAmount = getImageWithRarity("epic")
        val legendaryImagesAmount = getImageWithRarity("legendary")

        gift.setOnClickListener{

            val rarity = getRarity()
            when (rarity) {
                "common" -> {
                    val randNumber = (1..commonImagesAmount).random()
                    val drawableName = rarity + "_" + randNumber.toString()
                    image = this.getResources().getIdentifier(drawableName,
                                        "drawable", this.getPackageName());
                    commonCounter += 1
                    var test = readDataRarity("common")
                    text = "Вы получили обычную карту. Всего карт $test обычных, $rareImagesAmount редких" +
                            "$epicImagesAmount эпичных и $legendaryImagesAmount легендарных"
                }
                "rare" -> {
                    val randNumber = (1..rareImagesAmount).random()
                    val drawableName = rarity + "_" + randNumber.toString()
                    image = this.getResources().getIdentifier(drawableName,
                                        "drawable", this.getPackageName());
                    rareCounter += 1
                    text = "Вы получили редкую карту!"
                }
                "epic" -> {
                    val randNumber = (1..epicImagesAmount).random()
                    val drawableName = rarity + "_" + randNumber.toString()
                    image = this.getResources().getIdentifier(drawableName,
                                        "drawable", this.getPackageName());
                    epicCounter += 1
                    text = "Вы получили эпическую карту!!!"
                }
                "legendary" -> {
                    val randNumber = (1..legendaryImagesAmount).random()
                    val drawableName = rarity + "_" + randNumber.toString()
                    image = this.getResources().getIdentifier(drawableName,
                                        "drawable", this.getPackageName());
                    legendaryCounter += 1
                    text = "Вы получили ЛЕГЕНДАРНУЮ карту!!!"
                }
            }

            photo.setImageResource(image)
            gift.visibility = View.INVISIBLE
            getKaban.visibility = View.INVISIBLE
            statsButton.visibility = View.INVISIBLE
            photo.visibility = View.VISIBLE
            resultKaban.text = text
            resultKaban.visibility = View.VISIBLE
        }

        photo.setOnClickListener{
            photo.visibility = View.INVISIBLE
            resultKaban.visibility = View.INVISIBLE
            gift.visibility = View.VISIBLE
            getKaban.visibility = View.VISIBLE
            statsButton.visibility = View.VISIBLE
        }

        statsButton.setOnClickListener {
            val intent = Intent(this, popupStatistics::class.java)
            intent.putExtra("popuptitle", "Статистика")
            intent.putExtra("popuptext", "Ваша статистика:\nОбычных карт: ${commonCounter}" +
                    "\nРедких : ${rareCounter}\nЭпичных карт: ${epicCounter}\n" +
                    "ЛЕГЕНДАРНЫХ карт: ${legendaryCounter}")
            intent.putExtra("popupbtn", "OK")
            startActivity(intent)
        }

    }


    fun readDataRarity(rarity: String): Int {
        val database = Firebase.database.reference
        val ref = database.child("count")

        Log.d("Debugging", "$ref")
        var rarity_counts: Int = -1

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Обработка считанных данных здесь
                val value = dataSnapshot.child(rarity).getValue(Int::class.java)
                println(value)
                println(dataSnapshot)
                rarity_counts = value ?: -1
                Log.d("Debugging", "value: $value")
                Log.d("Debugging", "dataSnapshot: $dataSnapshot")
                Log.d("Debugging", "rarity_counts: $rarity_counts")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки чтения данных здесь
                println("Ошибка чтения данных из базы данных: ${databaseError.message}")
                Log.d("Debugging", "loadPost:onCancelled", databaseError.toException())
            }
        }

        ref.addListenerForSingleValueEvent(valueEventListener)

        return rarity_counts
    }

}