package com.itachi1706.cheesecakeutilities.modules.unicodeKeyboard

import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.itachi1706.cheesecakeutilities.BaseModuleActivity
import com.itachi1706.cheesecakeutilities.R
import com.itachi1706.cheesecakeutilities.modules.unicodeKeyboard.recyclerAdapters.UnicodeMenuAdapter
import kotlinx.android.synthetic.main.fragment_recycler_view.*

class UnicodeActivity : BaseModuleActivity() {
    override val helpDescription: String
        get() {
            var message = "List of Unicode emojis that you can click on to copy to your clipboard for use everywhere else :D"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) message += "\n\nYou can even drag and drop emojis into textboxes when you are in Android's Multi Window mode by long clicking the emoji!"
            return message
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unicode)
        main_menu_recycler_view.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(this, 2)
        main_menu_recycler_view.layoutManager = gridLayoutManager
        main_menu_recycler_view.itemAnimator = DefaultItemAnimator()
        // Set up layout
        val menuitems = unicodeItems()
        val adapter = UnicodeMenuAdapter(menuitems)
        main_menu_recycler_view.adapter = adapter
    }

    // Format: "", "", "", "",

    @Deprecated("This is used for the full list, and should be replaced by the respective specialized versions eventually")
    private fun unicodeItems(): Array<String> { return unicodeEmojis() + unicodeKaomoji() + unicodeFliptable() + unicodeMisc() }

    private fun unicodeEmojis(): Array<String> {
        return arrayOf("☻", "☹", "♡", "♥", "❤", "⚘", "❀", "❃", "❁", "✼", "♫", "♪", "☃", "❄", "❅", "❆", "☕", "☂", "★")
    }

    private fun unicodeKaomoji(): Array<String> {
        return arrayOf(
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
    }

    private fun unicodeFliptable(): Array<String> {
        return arrayOf("(╯°□°）╯︵ ┻━┻", "┻━┻ ︵ヽ(`Д´)ﾉ︵ ┻━┻", "┬──┬ ノ( ゜-゜ノ)", "(ノಠ益ಠ)ノ彡┻━┻", "（╯°□°）╯︵(\\ .o.)\\", "┬─┬\uFEFF ︵ /(.□. \\）")
    }

    private fun unicodeMisc(): Array<String> {
        return arrayOf(" ̳ ̳ ̳ ̳ ͙ ڪ ", "Ƹ̵̡Ӝ̵̨̄Ʒ", "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]", "▄︻̷̿┻̿═━一", "⌐╦╦═─", "┌─┐\n┴─┴\nಠ_ರೃ", "~~~ ╔͎═͓═͙╗\n~~~ ╚̨̈́═̈́﴾ ̥̂˖̫˖̥  ̂ )", "•_•)\n( •_•)>⌐■-■\n(⌐■_■)")
    }
}