//Start of legendary app

package com.example.kabans

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.example.kabans.databinding.ActivityMainBinding
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings


class MainActivity : AppCompatActivity() {

    lateinit var bindClass : ActivityMainBinding

    internal class Rarity(var probability: Double, var name: String){}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindClass = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindClass.root)

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val gift = bindClass.giftImage
        val photo = bindClass.kabanImage
        val getKaban = bindClass.getKabanText
        val resultKaban = bindClass.resultKabanText
        val statsButton = bindClass.statsButton
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


        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(2) // Fetch at most once every hour
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

        fun getRarity() : String {
            var cumulativeProbability = 0.0
            val p = Math.random()
            var name = ""
            for(rarity in rarities) {
                cumulativeProbability += rarity.probability
                if (p <= cumulativeProbability) {
                    name = rarity.name
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

        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    gift.setOnClickListener {

                        val commonImagesAmount = firebaseRemoteConfig.getString("common_counter")
                        val rareImagesAmount = firebaseRemoteConfig.getString("rare_counter")
                        val epicImagesAmount = firebaseRemoteConfig.getString("epic_counter")
                        val legendaryImagesAmount = firebaseRemoteConfig.getString("legendary_counter")

                        val rarity = getRarity()
                        when (rarity) {
                            "common" -> {
                                val randNumber = (1..commonImagesAmount.toInt()).random()
                                val cardUrl = firebaseRemoteConfig.getString("common_${randNumber}")
                                Glide.with(this)
                                    .load(cardUrl)
                                    .into(photo)
                                commonCounter += 1
                                text = "Вы получили обычную карту! Всего ${commonCounter} из ${commonImagesAmount} обычных карт"
                            }

                            "rare" -> {
                                val randNumber = (1..rareImagesAmount.toInt()).random()
                                val cardUrl = firebaseRemoteConfig.getString("rare_${randNumber}")
                                Glide.with(this)
                                    .load(cardUrl)
                                    .into(photo)
                                rareCounter += 1
                                text = "Вы получили редкую карту! Всего ${rareCounter} из ${rareImagesAmount} редких карт"
                            }

                            "epic" -> {
                                val randNumber = (1..epicImagesAmount.toInt()).random()
                                val cardUrl = firebaseRemoteConfig.getString("epic_${randNumber}")
                                Glide.with(this)
                                    .load(cardUrl)
                                    .into(photo)
                                epicCounter += 1
                                text = "Вы получили эпическую карту! Существует только одна такая"
                            }

                            "legendary" -> {
                                val randNumber = (1..legendaryImagesAmount.toInt()).random()
                                val cardUrl = firebaseRemoteConfig.getString("legendary_${randNumber}")
                                Glide.with(this)
                                    .load(cardUrl)
                                    .into(photo)
                                legendaryCounter += 1
                                text = "Вы получили ЛЕГЕНДАРНУЮ карту!!! Ты нашел единственную легендарную карту!"
                            }
                        }


                        gift.visibility = View.INVISIBLE
                        getKaban.visibility = View.INVISIBLE
                        statsButton.visibility = View.INVISIBLE
                        photo.visibility = View.VISIBLE
                        resultKaban.text = text
                        resultKaban.visibility = View.VISIBLE
                    }

                    photo.setOnClickListener {
                        photo.visibility = View.INVISIBLE
                        resultKaban.visibility = View.INVISIBLE
                        gift.visibility = View.VISIBLE
                        getKaban.visibility = View.VISIBLE
                        statsButton.visibility = View.VISIBLE
                    }

                    statsButton.setOnClickListener {
                        val intent = Intent(this, popupStatistics::class.java)
                        intent.putExtra("popuptitle", "Статистика")
                        intent.putExtra(
                            "popuptext", "Ваша статистика:\nОбычных карт: ${commonCounter}" +
                                    "\nРедких : ${rareCounter}\nЭпичных карт: ${epicCounter}\n" +
                                    "ЛЕГЕНДАРНЫХ карт: ${legendaryCounter}"
                        )
                        intent.putExtra("popupbtn", "OK")
                        startActivity(intent)
                    }
                }
        }
    }
}