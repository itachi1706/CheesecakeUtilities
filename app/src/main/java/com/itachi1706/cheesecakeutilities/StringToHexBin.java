package com.itachi1706.cheesecakeutilities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class StringToHexBin extends BaseActivity implements View.OnClickListener {

    TextView input, result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_string_to_hex_bin);
        input = findViewById(R.id.input_binhex_string);
        result = findViewById(R.id.tv_binhex_results);
        findViewById(R.id.btn_binhex_clear).setOnClickListener(this);
        findViewById(R.id.btn_binhex_copy).setOnClickListener(this);
        findViewById(R.id.btn_binhex_send).setOnClickListener(this);
        findViewById(R.id.btn_binhex_translate).setOnClickListener(this);
    }

    private String translate() {
        RadioButton bin = this.findViewById(R.id.rb_binhex_bin);
        String thetext = input.getText().toString();
        if (thetext.length() > 0 && thetext.charAt(0) == ' ')
            thetext = thetext.substring(1, thetext.length());
        if (thetext.length() > 0 && thetext.charAt(thetext.length() - 1) == ' ')
            thetext = thetext.substring(0, thetext.length() - 1);
        String theconv = "";
        CheckBox chkDel = this.findViewById(R.id.checkbox_binhex_delimit);
        RadioButton encdec = this.findViewById(R.id.rb_binhex_encode);
        for (int i = 0; i < thetext.length(); i++) {
            String thebin;
            int b = thetext.charAt(i);
            if (encdec.isChecked()) {
                if (bin.isChecked()) {
                    thebin = Integer.toBinaryString(b);
                    while (thebin.length() < 8) {
                        thebin = "0" + thebin;
                    }
                } else {
                    thebin = Integer.toHexString(b);
                    if (thebin.length() == 1) {
                        thebin = "0" + thebin;
                    }
                }
                if (chkDel.isChecked()) {
                    if (!bin.isChecked()) {
                        theconv = String.valueOf(theconv) + "\\x";
                    } else if (i != 0) {
                        theconv = String.valueOf(theconv) + " ";
                    }
                }
            } else {
                try {
                    if (chkDel.isChecked()) {
                        if (bin.isChecked()) {
                            thebin = Character.toString((char) Integer.parseInt(String.valueOf(Character.toString(thetext.charAt(i))) + Character.toString(thetext.charAt(i + 1)) + Character.toString(thetext.charAt(i + 2)) + Character.toString(thetext.charAt(i + 3)) + Character.toString(thetext.charAt(i + 4)) + Character.toString(thetext.charAt(i + 5)) + Character.toString(thetext.charAt(i + 6)) + Character.toString(thetext.charAt(i + 7)), 2));
                            i += 8;
                        } else {
                            thebin = Character.toString((char) Integer.parseInt(String.valueOf(Character.toString(thetext.charAt(i + 2))) + Character.toString(thetext.charAt(i + 3)), 16));
                            i += 3;
                        }
                    } else if (bin.isChecked()) {
                        thebin = Character.toString((char) Integer.parseInt(String.valueOf(Character.toString(thetext.charAt(i))) + Character.toString(thetext.charAt(i + 1)) + Character.toString(thetext.charAt(i + 2)) + Character.toString(thetext.charAt(i + 3)) + Character.toString(thetext.charAt(i + 4)) + Character.toString(thetext.charAt(i + 5)) + Character.toString(thetext.charAt(i + 6)) + Character.toString(thetext.charAt(i + 7)), 2));
                        i += 7;
                    } else {
                        thebin = Character.toString((char) Integer.parseInt(String.valueOf(Character.toString(thetext.charAt(i))) + Character.toString(thetext.charAt(i + 1)), 16));
                        i++;
                    }
                } catch (Exception e) {
                    i = thetext.length();
                    thebin = "";
                    Toast.makeText(this.getApplicationContext(), "An error occurred during conversion! Please check your input and options.", Toast.LENGTH_LONG).show();
                }
            }
            theconv = String.valueOf(theconv) + thebin;
        }
        return theconv;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_binhex_clear:
                result.setText("");
                input.setText("");
                break;
            case R.id.btn_binhex_copy:
                ClipboardManager manager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("binhex", (result.getText().toString()));
                manager.setPrimaryClip(data);
                Toast.makeText(this.getApplicationContext(), "Successfully copied!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_binhex_send:
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, result.getText().toString());
                startActivity(Intent.createChooser(sendIntent, "Send to"));
                break;
            case R.id.btn_binhex_translate:
                result.setText(translate());
        }
    }

    @Override
    public String getHelpDescription() {
        return "Translates normal language into machine code and back.";
    }
}
