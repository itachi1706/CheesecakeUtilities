package com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.BarcodeHelper;
import com.itachi1706.cheesecakeutilities.R;

import java.util.ArrayList;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

        restrictionString = getActivity().getResources().getStringArray(R.array.barcode_types_restrictions);
        generate.setOnClickListener(v1 -> generate());
        share.setOnClickListener(v2 -> share());
        barcodeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                restrictions.setText(restrictionString[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return v;
    }

    private void share() {
        Toast.makeText(getActivity(), "Unimplemented", Toast.LENGTH_LONG).show();
        /*if (bitmap == null) {
            Log.e("BarcodeGenerator", "Cannot share an empty image");
            Toast.makeText(getActivity(), "Invalid Action", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        String bitmapPath = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap,"title", null);
        Uri bitmapUri = Uri.parse(bitmapPath);
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent , "Share"));*/
    }

    private void generate() {
        // Check that theres something to generate
        if (textToConvert.getText() == null || textToConvert.getText().toString().isEmpty()) {
            // Nope
            Toast.makeText(getActivity(), "Text to generate cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = textToConvert.getText().toString();

        // Do validation
        convertTextError.setError(null);
        int generateCode = barcodeType.getSelectedItemPosition();
        String error = BarcodeHelper.checkValidation(generateCode, text);
        if (error != null && !error.isEmpty()) {
            convertTextError.setError(error);
            return;
        }

        // Check type
        BarcodeFormat format = BarcodeHelper.getGenerateType(generateCode);
        try {
            bitmap = encodeAsBitmap(text, format, 600, 600);
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

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
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
        BitMatrix result;
        try {
            result = writer.encode(contents, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
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
