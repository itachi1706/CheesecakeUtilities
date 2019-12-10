package com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.Fragments;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.BarcodeHelper;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.util.LogHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class BarcodeGeneratorFragment extends Fragment {

    LinearLayout barcodeActions;
    Button generate, share;
    ImageView result;
    TextView restrictions;
    TextInputEditText textToConvert;
    TextInputLayout convertTextError;
    Spinner barcodeType;

    String[] restrictionString;

    Bitmap bitmap = null;

    private static final String TAG = "BarcodeGenerator";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_barcode_generator, container, false);

        barcodeActions = v.findViewById(R.id.ll_barcode_actions);
        generate = v.findViewById(R.id.barcode_generate_btn);
        share = v.findViewById(R.id.barcode_share);
        textToConvert = v.findViewById(R.id.etBarcode);
        result = v.findViewById(R.id.barcode_generated);
        barcodeType = v.findViewById(R.id.barcode_types);
        restrictions = v.findViewById(R.id.barcode_restrictions);
        convertTextError = v.findViewById(R.id.til_etBarcode);

        //noinspection ConstantConditions
        restrictionString = getActivity().getResources().getStringArray(R.array.barcode_types_restrictions);
        generate.setOnClickListener(v1 -> generate());
        share.setOnClickListener(v2 -> share());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result.setOnLongClickListener(v12 -> {
                if (bitmap == null) return false;
                Uri contentUri = saveImageTmpAndGetUri();
                ClipData clip = ClipData.newUri(getActivity().getContentResolver(), "barcode", contentUri);
                View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v12);
                v12.startDragAndDrop(clip, dragShadowBuilder, true, View.DRAG_FLAG_GLOBAL | View.DRAG_FLAG_GLOBAL_URI_READ |
                        View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION);
                return true;
            });
        }
        barcodeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                restrictions.setText(restrictionString[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not used. Code stub
            }
        });

        return v;
    }

    private Uri saveImageTmpAndGetUri() {
        //noinspection ConstantConditions
        File cache = new File(getActivity().getExternalCacheDir(), "images_cache");
        //noinspection ResultOfMethodCallIgnored
        cache.mkdirs();
        try {
            FileOutputStream s = new FileOutputStream(cache + "/barcode_share.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, s);
            s.close();
        } catch (IOException e) {
            LogHelper.e(TAG, "Failed to create temp barcode file");
            e.printStackTrace();
            return null;
        }

        File shareFile = new File(cache, "barcode_share.png");
        Uri contentUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", shareFile);

        if (contentUri == null) {
            LogHelper.e(TAG, "Failed to share file, invalid contentUri");
            return null;
        }

        return contentUri;
    }

    private void share() {
        if (bitmap == null) {
            LogHelper.e(TAG, "Cannot share an empty image");
            Toast.makeText(getActivity(), "Invalid Action", Toast.LENGTH_SHORT).show();
            return;
        }

        // Temporary save image
        Uri contentUri = saveImageTmpAndGetUri();
        if (contentUri == null) {
            Toast.makeText(getActivity(), "Failed to share barcode", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
        //noinspection ConstantConditions
        shareIntent.setType(getActivity().getContentResolver().getType(contentUri));
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(shareIntent, "Choose an app to share the image to"));
    }

    private void generate() {
        // Check that theres something to generate
        convertTextError.setError(null);
        if (textToConvert.getText() == null || textToConvert.getText().toString().isEmpty()) {
            // Nope
            convertTextError.setError("Input cannot be empty");
            return;
        }
        String text = textToConvert.getText().toString();

        // Do validation
        int generateCode = barcodeType.getSelectedItemPosition();
        String error = BarcodeHelper.checkValidation(generateCode, text);
        if (error != null && !error.isEmpty()) {
            convertTextError.setError(error);
            return;
        }

        //noinspection ConstantConditions
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            //noinspection ConstantConditions
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }

        // Check type
        BarcodeFormat format = BarcodeHelper.getGenerateType(generateCode);
        try {
            bitmap = encodeAsBitmap(text, format);
            result.setImageBitmap(bitmap);
            barcodeActions.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            Toast.makeText(getActivity(), "Unable to generate barcode", Toast.LENGTH_LONG).show();
            barcodeActions.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }


    // Generator
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private static final int IMAGE_DIMEN = 1200;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format) throws WriterException {
        if (contents == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = writer.encode(contents, format, IMAGE_DIMEN, IMAGE_DIMEN, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap resultBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return resultBitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }
}
