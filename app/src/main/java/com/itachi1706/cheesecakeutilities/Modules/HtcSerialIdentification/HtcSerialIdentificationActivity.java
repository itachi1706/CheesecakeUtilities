package com.itachi1706.cheesecakeutilities.Modules.HtcSerialIdentification;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itachi1706.appupdater.Util.DeprecationHelper;
import com.itachi1706.cheesecakeutilities.BaseActivity;
import com.itachi1706.cheesecakeutilities.Modules.HtcSerialIdentification.Util.HtcSerialNumberDates;
import com.itachi1706.cheesecakeutilities.Modules.HtcSerialIdentification.Util.HtcSerialNumberManufacturingLocations;
import com.itachi1706.cheesecakeutilities.R;

public class HtcSerialIdentificationActivity extends BaseActivity {

    Button search, serial;
    TextView resultList;
    EditText serialNumber;

    /**
     * Serial Number Format
     * HTYMDAABBBBB
     * HT = Vendor HTC--> Hsinchu, Taiwan or SH = Shanghai, China
     * Y = Year (3 = 2013)
     * M = Month (hex 1...C = 1 - 12 months)
     * D = Day (hex 1...9A..Z = 1 - 31 days)
     * YY = Part Code
     * ZZZZZ : Identification Number (decimal)
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_htc_serial_identification);

        search = (Button) findViewById(R.id.btn_htc_sn_search);
        serial = (Button) findViewById(R.id.btn_htc_sn_serial);
        resultList = (TextView) findViewById(R.id.tv_htc_sn_result);
        serialNumber = (EditText) findViewById(R.id.htc_sn_serialField);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkSerialNumberValid(serialNumber.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Invalid Serial Number", Toast.LENGTH_SHORT).show();
                    return;
                }

                String htmlFormattedSN = parseSerialNumber(serialNumber.getText().toString());
                resultList.setText(DeprecationHelper.Html.fromHtml(htmlFormattedSN));
            }
        });

        serial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String uid = tManager.getDeviceId();
                */
                if (Build.SERIAL == null || Build.SERIAL.equals("")) {
                    Toast.makeText(getApplicationContext(), "No Serial Number found", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isAHTCPhone()) {
                    new AlertDialog.Builder(HtcSerialIdentificationActivity.this).setTitle("Not a HTC Phone")
                            .setMessage("This application currently only supports HTC Phones. It may support other phones soon")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    HtcSerialIdentificationActivity.this.finish();
                                }
                            }).show();
                    return;
                }
                //Check if this is a HTC phone
                String uid = Build.SERIAL;
                serialNumber.setText(uid);
                search.performClick();
            }
        });
    }

    private boolean isAHTCPhone() {
        return Build.MANUFACTURER.equalsIgnoreCase("HTC") || Build.BRAND.equalsIgnoreCase("htc")
                || Build.DEVICE.toLowerCase().contains("htc") || Build.MODEL.toLowerCase().contains("htc")
                || Build.FINGERPRINT.toLowerCase().contains("htc") || Build.PRODUCT.toLowerCase().contains("htc");
    }

    private String parseSerialNumber(String serial){
        serial = serial.toUpperCase();
        String manufacturerLocation = serial.substring(0,2);
        String date = serial.substring(2,5);
        StringBuilder html = new StringBuilder();

        html.append("Result for S/N <b>").append(serial).append("</b><br />");

        HtcSerialNumberManufacturingLocations manufacturer = HtcSerialNumberManufacturingLocations.fromCode(manufacturerLocation);
        if (manufacturer == HtcSerialNumberManufacturingLocations.UNKNOWN)
            html.append("Manufactured At: ").append(manufacturerLocation).append("<br />");
        else
            html.append("Manufactured At: ").append(manufacturer.getLocation()).append("<br />");

        HtcSerialNumberDates year = HtcSerialNumberDates.getData(date.charAt(0) + "");
        HtcSerialNumberDates month = HtcSerialNumberDates.getData(date.charAt(1) + "");
        HtcSerialNumberDates day = HtcSerialNumberDates.getData(date.charAt(2) + "");
        html.append("HtcSerialNumberDates of Manufacture: ").append(day.getDate()).append(" ").append(month.getMonth()).append(" ").append(year.getYear());
        return html.toString();
    }

    private boolean checkSerialNumberValid(String serial){
        return serial.length() == 12;
    }

    @Override
    public String getHelpDescription() {
        return "Identifies the manufacturing date and location of HTC devices based on their serial numbers";
    }
}
