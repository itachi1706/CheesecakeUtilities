package com.itachi1706.cheesecakeutilities.modules.unicodekeyboard

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.databinding.ActivityViewpagerFragBinding

class UnicodeActivity : BaseModuleActivity() {
    override val helpDescription: String
        get() {
            var message = "List of Unicode emojis that you can click on to copy to your clipboard for use everywhere else :D"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) message += "\n\nYou can even drag and drop emojis into textboxes when you are in Android's Multi Window mode by long clicking the emoji!"
            return message
        }

    private lateinit var binding: ActivityViewpagerFragBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewpagerFragBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.viewPager.adapter = UnicodeTabAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position -> tab.text = tabs[position] }.attach()
    }

    class UnicodeTabAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int { return 43 } // 4 tabs
        override fun createFragment(position: Int): Fragment { return UnicodeFragment.newInstance(getUnicodeStringList(position)) }
    }

    companion object {
        val tabs = arrayOf("Emojis", "Kaomojis", "Flip Table", "Misc", "Joy", "Love", "Embarrasment", "Sympathy", "Dissatisfaction", "Anger", "Sadness", "Pain", "Fear", "Indifference", "Confusion", "Doubt",
                "Surprise", "Greeting", "Hugging", "Winking", "Apologizing", "Nosebleeding", "Hiding", "Writing", "Running", "Sleeping", "Cat", "Bear", "Dog", "Rabbit",
                "Pig", "Bird", "Fish", "Spider", "Friends", "Enemies", "Weapons", "Magic", "Food", "Music", "Games", "Faces", "Special")
        fun getUnicodeStringList(position: Int): Array<String> {
            return when (position) {
                0 -> emojis
                1 -> kaomojiOrig
                2 -> flipTable
                3 -> misc
                4 -> Kaomojis.joy
                5 -> Kaomojis.love
                6 -> Kaomojis.embarrasment
                7 -> Kaomojis.sympathy
                8 -> Kaomojis.dissatisfaction
                9 -> Kaomojis.anger
                10 -> Kaomojis.sadness
                11 -> Kaomojis.pain
                12 -> Kaomojis.fear
                13 -> Kaomojis.indifference
                14 -> Kaomojis.confusion
                15 -> Kaomojis.doubt
                16 -> Kaomojis.surprise
                17 -> Kaomojis.greeting
                18 -> Kaomojis.hugging
                19 -> Kaomojis.winking
                20 -> Kaomojis.apologizing
                21 -> Kaomojis.nosebleeding
                22 -> Kaomojis.hidingKaomoji
                23 -> Kaomojis.writingKaomoji
                24 -> Kaomojis.runningKaomoji
                25 -> Kaomojis.sleepingKaomoji
                26 -> Kaomojis.cat
                27 -> Kaomojis.bear
                28 -> Kaomojis.dog
                29 -> Kaomojis.rabbit
                30 -> Kaomojis.pig
                31 -> Kaomojis.bird
                32 -> Kaomojis.fish
                33 -> Kaomojis.spider
                34 -> Kaomojis.friends
                35 -> Kaomojis.enemies
                36 -> Kaomojis.weapons
                37 -> Kaomojis.magic
                38 -> Kaomojis.food
                39 -> Kaomojis.musicKaomoji
                40 -> Kaomojis.gamesKaomoji
                41 -> Kaomojis.faces
                42 -> Kaomojis.special
                else -> arrayOf()
            }
        }

        private val emojis = arrayOf("☻", "☹", "♡", "♥", "❤", "⚘", "❀", "❃", "❁", "✼", "♫", "♪", "☃", "❄", "❅", "❆", "☕", "☂", "★")
        private val kaomojiOrig = arrayOf(
                "◕‿◕", "｡◕‿◕｡", "｡◕‿‿◕｡", "^̮^",
                "(◕‿◕)", "(｡◕‿◕｡)", "(｡◕‿‿◕｡)", "(^̮^)",
                "ʘ‿ʘ", "ಠ_ಠ", "ಠ⌣ಠ", "ಠ‿ಠ",
                "(ʘ‿ʘ)", "(ಠ_ಠ)", "(ಠ⌣ಠ)", "(ಠ‿ಠ)",
                "♥‿♥", "◔̯◔", "٩◔̯◔۶", "⊙﹏⊙", "(¬_¬)", "(；一_一)",
                "(･.◤)", "◕‿↼", "(¬‿¬)", "(づ￣ ³￣)づ",
                "ب_ب", "(ಥ_ಥ)", "(ಥ﹏ಥ)", "ლ(ಠ益ಠლ)",
                "ʕ•ᴥ•ʔ", "°Д°", "﴾͡๏̯͡๏﴿ O'RLY?", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧",
                "(ᵔᴥᵔ)", "(•ω•)", "☜(⌒▽⌒)☞", "〆(・∀・＠)",
                "◔ ⌣ ◔", "ლ(´ڡ`ლ)", "ლ,ᔑ•ﺪ͟͠•ᔐ.ლ", "ᕙ(⇀‸↼‶)ᕗ",
                "(づ｡◕‿‿◕｡)づ", "ᄽὁȍ ̪ őὀᄿ", "╭∩╮(-_-)╭∩╮", "凸(-_-)凸",
                " ̿ ̿'̿'\\̵͇̿̿\\з=(•_•)=ε/̵͇̿̿/'̿'̿ ̿", "(´・ω・)っ由", "（´・ω・ `）",
                "٩(⁎❛ᴗ❛⁎)۶", "(͡° ͜ʖ ͡°)", "¯\\_(ツ)_/¯", "(° ͜ʖ °)",
                "¯\\(°_o)/¯", "( ﾟヮﾟ)", "ヽ༼ຈل͜ຈ༽ﾉ", "(︺︹︺)", "／人 ◕ ‿‿ ◕ 人＼")
        private val flipTable = arrayOf("(╯°□°）╯︵ ┻━┻","(┛◉Д◉)┛彡┻━┻","(ﾉ≧∇≦)ﾉ ﾐ ┸━┸","(ノಠ益ಠ)ノ彡┻━┻","(╯ರ ~ ರ）╯︵ ┻━┻","(┛ಸ_ಸ)┛彡┻━┻","(ﾉ´･ω･)ﾉ ﾐ ┸━┸",
                "(ノಥ,_｣ಥ)ノ彡┻━┻","(┛✧Д✧))┛彡┻━┻","┻━┻ ︵ヽ(`Д´)ﾉ︵ ┻━┻","┻━┻ ︵ ¯\\(ツ)/¯ ︵ ┻━┻","(ノTДT)ノ ┫:･’.::･┻┻:･’.::･","(ノ｀⌒´)ノ ┫：・’.：：・┻┻：・’.：：・",
                "(ﾉ*｀▽´*)ﾉ ⌒┫ ┻ ┣ ┳","┻━┻ミ＼(≧ﾛ≦＼)","┻━┻︵└(՞▃՞ └)","┻━┻︵└(´▃｀└)","─=≡Σ((((╯°□°）╯︵ ┻━┻","(ノ｀´)ノ ~┻━┻","(-_- )ﾉ⌒┫ ┻ ┣","(ノ￣皿￣）ノ ⌒=== ┫",
                "ノ｀⌒´)ノ ┫：・’.：：・┻┻","༼ﾉຈل͜ຈ༽ﾉ︵┻━┻","ヽ༼ຈل͜ຈ༽ﾉ⌒┫ ┻ ┣","ﾐ┻┻(ﾉ>｡<)ﾉ",".::･┻┻☆()ﾟOﾟ)","Take that! (ﾉ｀A”)ﾉ ⌒┫ ┻ ┣ ┳☆(x x)","(ノ｀m´)ノ ~┻━┻ (/o＼)",
                "⌒┫ ┻ ┣ ⌒┻☆)ﾟ⊿ﾟ)ﾉWTF!","(ﾉ≧∇≦)ﾉ ﾐ ┸┸)`νﾟ)･;’.","ミ(ノ￣^￣)ノ≡≡≡≡≡━┳━☆()￣□￣)/","（╯°□°）╯︵(\\ .o.)\\","┣ﾍ(^▽^ﾍ)Ξ(ﾟ▽ﾟ*)ﾉ┳━┳ There we go~♪","┬──┬ ノ( ゜-゜ノ)","┬──┬ ¯\\_(ツ)",
                "(ヘ･_･)ヘ┳━┳","ヘ(´° □°)ヘ┳━┳","┣ﾍ(≧∇≦ﾍ)… (≧∇≦)/┳━┳","┬─┬\uFEFF ︵ /(.□. \\）")
        private val misc = arrayOf(" ̳ ̳ ̳ ̳ ͙ ڪ ", "Ƹ̵̡Ӝ̵̨̄Ʒ", "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]", "▄︻̷̿┻̿═━一", "⌐╦╦═─", "┌─┐\n┴─┴\nಠ_ರೃ", "~~~ ╔͎═͓═͙╗\n~~~ ╚̨̈́═̈́﴾ ̥̂˖̫˖̥  ̂ )", "•_•)\n( •_•)>⌐■-■\n(⌐■_■)")
    }
}