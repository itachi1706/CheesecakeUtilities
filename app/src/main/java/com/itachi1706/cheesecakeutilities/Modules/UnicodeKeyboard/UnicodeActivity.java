package com.itachi1706.cheesecakeutilities.Modules.UnicodeKeyboard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.itachi1706.cheesecakeutilities.Modules.UnicodeKeyboard.RecyclerAdapters.UnicodeMenuAdapter;
import com.itachi1706.cheesecakeutilities.R;

public class UnicodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unicode);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_menu_recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            // Set up layout
            String[] menuitems = unicodeItems();
            UnicodeMenuAdapter adapter = new UnicodeMenuAdapter(menuitems);
            recyclerView.setAdapter(adapter);
        }
    }

    private String[] unicodeItems() {
        // "", "", "", "",
        return new String[] {
                "☺", "☻", "✌", "☹",
                "♡", "♥", "❤", "⚘",
                "❀", "❃", "❁", "✼",
                "☀", "✌", "♫", "♪",
                "☃", "❄", "❅", "❆",
                "☕", "☂", "★", "\uD83D\uDC8B",
                "◕‿◕", "｡◕‿◕｡", "｡◕‿‿◕｡", "^̮^",
                "(◕‿◕)", "(｡◕‿◕｡)", "(｡◕‿‿◕｡)", "(^̮^)",
                "ʘ‿ʘ", "ಠ_ಠ", "ಠ⌣ಠ", "ಠ‿ಠ",
                "(ʘ‿ʘ)", "(ಠ_ಠ)", "(ಠ⌣ಠ)", "(ಠ‿ಠ)",
                "♥‿♥", "◔̯◔", "٩◔̯◔۶", "⊙﹏⊙",
                " ̳ ̳ ̳ ̳ ͙ ڪ ", "Ƹ̵̡Ӝ̵̨̄Ʒ", "(¬_¬)", "(；一_一)",
                "(･.◤)", "◕‿↼", "(¬‿¬)", "(づ￣ ³￣)づ",
                "ب_ب", "(ಥ_ಥ)", "(ಥ﹏ಥ)", "ლ(ಠ益ಠლ)",
                "ʕ•ᴥ•ʔ", "°Д°", "﴾͡๏̯͡๏﴿ O'RLY?", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧",
                "(ᵔᴥᵔ)", "(•ω•)", "☜(⌒▽⌒)☞", "〆(・∀・＠)",
                "◔ ⌣ ◔", "ლ(´ڡ`ლ)", "ლ,ᔑ•ﺪ͟͠•ᔐ.ლ", "ᕙ(⇀‸↼‶)ᕗ",
                "[̲̅$̲̅(̲̅5̲̅)̲̅$̲̅]", "(づ｡◕‿‿◕｡)づ", "ᄽὁȍ ̪ őὀᄿ", "▄︻̷̿┻̿═━一",
                "(╯°□°）╯︵ ┻━┻", "┻━┻ ︵ヽ(`Д´)ﾉ︵ ┻━┻", "┬──┬ ノ( ゜-゜ノ)", "(ノಠ益ಠ)ノ彡┻━┻",
                "（╯°□°）╯︵(\\ .o.)\\", "┬─┬\uFEFF ︵ /(.□. \\）", "╭∩╮(-_-)╭∩╮", "凸(-_-)凸",
                " ̿ ̿'̿'\\̵͇̿̿\\з=(•_•)=ε/̵͇̿̿/'̿'̿ ̿", "⌐╦╦═─", "(´・ω・)っ由", "（´・ω・ `）",
                "٩(⁎❛ᴗ❛⁎)۶", "(͡° ͜ʖ ͡°)", "¯\\_(ツ)_/¯", "(° ͜ʖ °)",
                "¯\\(°_o)/¯", "( ﾟヮﾟ)", "ヽ༼ຈل͜ຈ༽ﾉ", "(︺︹︺)",
                "┌─┐\n┴─┴\nಠ_ರೃ", "~~~ ╔͎═͓═͙╗\n~~~ ╚̨̈́═̈́﴾ ̥̂˖̫˖̥  ̂ )", "•_•)\n( •_•)>⌐■-■\n(⌐■_■)", "／人 ◕ ‿‿ ◕ 人＼",
        };
    }
}
