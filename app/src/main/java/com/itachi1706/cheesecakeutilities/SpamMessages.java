package com.itachi1706.cheesecakeutilities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SpamMessages extends AppCompatActivity implements View.OnClickListener {

    private EditText messageText, teleText, numberText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spam_messages);
        this.messageText = (EditText) findViewById(R.id.edit_messagespam_text);
        this.numberText = (EditText) findViewById(R.id.edit_messagespam_times);
        this.teleText = (EditText) findViewById(R.id.edit_messagespam_contact);
        Button sendBtn = (Button) findViewById(R.id.btn_messagespam_send);
        if (sendBtn != null) {
            sendBtn.setOnClickListener(this);
        }
        Button contactBtn = (Button) findViewById(R.id.btn_messagespam_contacts);
        if (contactBtn != null) {
            contactBtn.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_messagespam_contacts:
                viewContactList();
                break;
            case R.id.btn_messagespam_send:
                sendSMS();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.binhex_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                new AlertDialog.Builder(this)
                        .setMessage("Sends a SMS message multiple times to the same contact.\n\n" +
                                "Note: Spamming may be illegal in your country and may cost real money as it send SMS messages" +
                                ". Use with caution.\n\nThis app or the creator may not be held responsible for any " +
                                "costs that this utility may incur.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, null).show();
                return true;
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case 2:
                if (data == null) return;
                Cursor c = getContentResolver().query(Phone.CONTENT_URI, null, Phone._ID + "=?", new String[]{data.getData().getLastPathSegment()}, null);
                if (c == null) return;
                if (c.getCount() == 1) {
                    if (c.moveToFirst()) {
                        String phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
                        Log.i("ContactPicker", "Phone Number: " + phoneNumber);
                        this.teleText.setText(phoneNumber);
                    } else
                        Log.w("ContactPicker", "No Numbers Found");
                }
                c.close();
        }
    }

    private void sendSMS() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (rc == PackageManager.PERMISSION_GRANTED){
            canSendSMS();
        } else {
            requestMessagingPermission();
        }
    }

    private void canSendSMS() {
        String message = this.messageText.getText().toString();
        String number = this.numberText.getText().toString();
        String teleNum = this.teleText.getText().toString();
        try {
            if (teleNum.equals("")) {
                Toast.makeText(getApplicationContext(), "Enter phone number", Toast.LENGTH_LONG).show();
                Log.e("Spammer", "Enter Phone Number");
                return;
            }
            try {
                int num = Integer.parseInt(number);
                if (num > 0) {
                    SmsManager sms = SmsManager.getDefault();
                    for (int i = 0; i < num; i++) {
                        sms.sendTextMessage(teleNum, null, message, null, null);
                    }
                    finish();
                    Toast.makeText(getApplicationContext(), "Message sent " + num + " times.", Toast.LENGTH_LONG).show();
                    Log.i("Spammer", "Message sent " + num + " times");
                } else {
                    Toast.makeText(getApplicationContext(), "Enter a number greater than zero.", Toast.LENGTH_LONG).show();
                    Log.e("Spammer", "Enter Number greated than zero. Number entered: " + num);
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Enter a number greater than zero.", Toast.LENGTH_LONG).show();
                Log.e("Spammer", "Exception occurred: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        } catch (Exception e2) {
            Toast.makeText(getApplicationContext(), "Enter phone number no symbols.", Toast.LENGTH_LONG).show();
            Log.e("Spammer", "Exception occurred: " + e2.getLocalizedMessage());
            e2.printStackTrace();
        }
    }

    public void viewContactList(){
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (rc == PackageManager.PERMISSION_GRANTED){
            startActivityForResult(new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI), 2);
        } else {
            requestContactsPermission();
        }
    }

    // Request Contacts/SMS Permissions
    private static final int RC_HANDLE_REQUEST_CONTACTS = 1;
    private static final int RC_HANDLE_REQUEST_MESSAGING = 2;

    private void requestContactsPermission() {
        Log.w("PermMan", "Contacts permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.READ_CONTACTS};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)){
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_REQUEST_CONTACTS);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle("Requesting Contacts Permission")
                .setMessage("This app requires ability to read your contacts to automatically get the phone number from your contacts list")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_CONTACTS);
                    }
                }).show();
    }

    private void requestMessagingPermission() {
        Log.w("PermMan", "Messaging permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.SEND_SMS};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_REQUEST_MESSAGING);
            return;
        }

        final Activity thisActivity = this;

        new AlertDialog.Builder(this).setTitle("Requesting SMS Permission")
                .setMessage("This app requires ability to send SMS Messages")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_REQUEST_MESSAGING);
                    }
                }).show();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        final Activity thisActivity = this;
        switch (requestCode) {
            case RC_HANDLE_REQUEST_MESSAGING:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PermMan", "Messaging Permission Granted. Sending SMS");
                    canSendSMS();
                    return;
                }
                Log.e("PermMan", "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                new AlertDialog.Builder(this).setTitle("Permission Denied")
                        .setMessage("You have denied the app ability to send SMS. This app will not be able to spam anybody")
                        .setPositiveButton(android.R.string.ok, null)
                        .setNeutralButton("SETTINGS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                                permIntent.setData(packageURI);
                                startActivity(permIntent);
                            }
                        }).show();
                break;
            case RC_HANDLE_REQUEST_CONTACTS:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PermMan", "Contacts Permission Granted. Launching Contacts Picker");
                    // we have permission, so create the camerasource
                    startActivityForResult(new Intent(Intent.ACTION_PICK, Phone.CONTENT_URI), 2);
                    return;
                }
                Log.e("PermMan", "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                new AlertDialog.Builder(this).setTitle("Permission Denied")
                        .setMessage("You have denied the app ability to access contacts. We are unable to let you select a contact. " +
                                "Please enter the phone number manually or grant the app permission to read your contacts")
                        .setPositiveButton(android.R.string.ok, null)
                        .setNeutralButton("SETTINGS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri packageURI = Uri.parse("package:" + thisActivity.getPackageName());
                                permIntent.setData(packageURI);
                                startActivity(permIntent);
                            }
                        }).show();
                break;
            default:
                Log.d("PermMan", "Got unexpected permission result: " + requestCode);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
