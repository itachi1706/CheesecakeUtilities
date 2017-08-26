package com.itachi1706.cheesecakeutilities.Modules.UnicodeKeyboard;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.Modules.UnicodeKeyboard.RecyclerAdapters.UnicodeMenuAdapter;
import com.itachi1706.cheesecakeutilities.R;

public class UnicodeActivity extends BaseActivity {

    @Override
    public String getHelpDescription() {
        String message = "List of Unicode emojis that you can click on to copy to your clipboard for use everywhere else :D";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            message += "\n\nYou can even drag and drop emojis into textboxes when you are in Android's Multi Window mode by long clicking the emoji!";
        return message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unicode);

        RecyclerView recyclerView = findViewById(R.id.main_menu_recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

            recyclerView.setLayoutManager(gridLayoutManager);
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
                "☻", "☹",
                "♡", "♥", "❤", "⚘",
                "❀", "❃", "❁", "✼",
                "♫", "♪",
                "☃", "❄", "❅", "❆",
                "☕", "☂", "★",
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
