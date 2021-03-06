package io.legado.app.help

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import io.legado.app.App
import io.legado.app.R
import io.legado.app.utils.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * 阅读界面配置
 */
object ReadBookConfig {
    const val readConfigFileName = "readConfig.json"
    private val configFilePath =
        App.INSTANCE.filesDir.absolutePath + File.separator + readConfigFileName
    val configList: ArrayList<Config> = arrayListOf()

    var styleSelect
        get() = App.INSTANCE.getPrefInt("readStyleSelect")
        set(value) = App.INSTANCE.putPrefInt("readStyleSelect", value)
    var bg: Drawable? = null

    init {
        upConfig()
    }

    @Synchronized
    fun getConfig(index: Int = styleSelect): Config {
        if (configList.size < 5) {
            reset()
        }
        return configList[index]
    }

    fun upConfig() {
        val configFile = File(configFilePath)
        val json = if (configFile.exists()) {
            configFile.readText()
        } else {
            String(App.INSTANCE.assets.open(readConfigFileName).readBytes())
        }
        try {
            GSON.fromJsonArray<Config>(json)?.let {
                configList.clear()
                configList.addAll(it)
            } ?: reset()
        } catch (e: Exception) {
            reset()
        }
    }

    fun upBg() {
        val resources = App.INSTANCE.resources
        val dm = resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        bg = getConfig().bgDrawable(width, height)
    }

    fun save() {
        GlobalScope.launch(IO) {
            val json = GSON.toJson(configList)
            val configFile = File(configFilePath)
            //获取流并存储
            try {
                BufferedWriter(FileWriter(configFile)).use { writer ->
                    writer.write(json)
                    writer.flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun reset() {
        val json = String(App.INSTANCE.assets.open(readConfigFileName).readBytes())
        GSON.fromJsonArray<Config>(json)?.let {
            configList.clear()
            configList.addAll(it)
        }
        save()
    }

    data class Config(
        var bgStr: String = "#EEEEEE",
        var bgStrNight: String = "#000000",
        var bgType: Int = 0,
        var bgTypeNight: Int = 0,
        var darkStatusIcon: Boolean = true,
        var darkStatusIconNight: Boolean = false,
        var letterSpacing: Float = 1f,
        var lineSpacingExtra: Int = 12,
        var lineSpacingMultiplier: Float = 1.2f,
        var paddingBottom: Int = 0,
        var paddingLeft: Int = 16,
        var paddingRight: Int = 16,
        var paddingTop: Int = 0,
        var textBold: Boolean = false,
        var textColor: String = "#3E3D3B",
        var textColorNight: String = "#adadad",
        var textSize: Int = 15
    ) {
        fun setBg(bgType: Int, bg: String) {
            if (AppConfig.isNightTheme) {
                bgTypeNight = bgType
                bgStrNight = bg
            } else {
                this.bgType = bgType
                bgStr = bg
            }
        }

        fun setTextColor(color: Int) {
            if (AppConfig.isNightTheme) {
                textColorNight = "#${color.hexString}"
            } else {
                textColor = "#${color.hexString}"
            }
        }

        fun setStatusIconDark(isDark: Boolean) {
            if (AppConfig.isNightTheme) {
                darkStatusIconNight = isDark
            } else {
                darkStatusIcon = isDark
            }
        }

        fun statusIconDark(): Boolean {
            return if (AppConfig.isNightTheme) {
                darkStatusIconNight
            } else {
                darkStatusIcon
            }
        }

        fun textColor(): Int {
            return if (AppConfig.isNightTheme) Color.parseColor(textColorNight)
            else Color.parseColor(textColor)
        }

        fun bgStr(): String {
            return if (AppConfig.isNightTheme) bgStrNight
            else bgStr
        }

        fun bgType(): Int {
            return if (AppConfig.isNightTheme) bgTypeNight
            else bgType
        }

        fun bgDrawable(width: Int, height: Int): Drawable {
            var bgDrawable: Drawable? = null
            val resources = App.INSTANCE.resources
            try {
                bgDrawable = when (bgType()) {
                    0 -> ColorDrawable(Color.parseColor(bgStr()))
                    1 -> {
                        BitmapDrawable(
                            resources,
                            BitmapUtils.decodeBitmap(
                                App.INSTANCE,
                                "bg" + File.separator + bgStr(),
                                width,
                                height
                            )
                        )
                    }
                    else -> BitmapDrawable(
                        resources,
                        BitmapUtils.decodeBitmap(bgStr(), width, height)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bgDrawable ?: ColorDrawable(App.INSTANCE.getCompatColor(R.color.background))
        }
    }
}