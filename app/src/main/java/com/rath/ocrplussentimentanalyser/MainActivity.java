package com.rath.ocrplussentimentanalyser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button snapBtn;
    public float[][] input;
    TextToSpeech t1;
    private Uri mImageUri;
    private Canvas canvas;
    private Button detectBtn;
    private int count=0;
    private Button sentiment;
    private ImageView imageView;
    private TextView txtView;
    private String resultText;
    private ScrollView textdetect;
    private Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        snapBtn = findViewById(R.id.snapBtn);
        sentiment = findViewById(R.id.sentimentbutton);
        textdetect = findViewById(R.id.textdetext);


        detectBtn = findViewById(R.id.detectBtn);
        imageView = findViewById(R.id.imageView);
        txtView = findViewById(R.id.txtView);
        input = new float[1][1000];

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        sentiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((resultText==null)||(resultText=="")){
                    Toast.makeText(MainActivity.this,"No Text, No Sentiments", Toast.LENGTH_LONG).show();
                }else{
                    resultText = resultText.trim().toLowerCase();
                    String[] words = resultText.split("\\s+");
                    try {
                        JSONObject obj = new JSONObject(loadJSONFromAsset());
                        obj = obj.getJSONObject("word_index");

                        for(int i=0; i<words.length;i++){
                            if(obj.has(words[i])){
                                count = count+1;
                                input[0][obj.getInt(words[i])] = 1;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i("error",e.toString());
                    }
                    if(count!=0){
                        FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel.Builder()
                                .setAssetFilePath("sentiment.tflite")
                                .build();

                        FirebaseModelInterpreter interpreter;
                        try {
                            FirebaseModelInterpreterOptions options =
                                    new FirebaseModelInterpreterOptions.Builder(localModel).build();
                            interpreter = FirebaseModelInterpreter.getInstance(options);
                            try {
                                final FirebaseModelInputOutputOptions inputOutputOptions =
                                        new FirebaseModelInputOutputOptions.Builder()
                                                .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1,1000})
                                                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1,1})
                                                .build();

                                FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                                        .add(input)  // add() as many input arrays as your model requires
                                        .build();
                                interpreter.run(inputs, inputOutputOptions)
                                        .addOnSuccessListener(
                                                new OnSuccessListener<FirebaseModelOutputs>() {
                                                    @Override
                                                    public void onSuccess(FirebaseModelOutputs result) {
                                                        float[][] output = result.getOutput(0);
                                                        float[] abc = output[0];
                                                        Toast.makeText(MainActivity.this, "Prediction Score = "+Arrays.toString(abc),Toast.LENGTH_LONG).show();
                                                        Log.i("Hurray", Arrays.toString(abc));
                                                        if(abc[0]>0.54){
                                                            String toSpeak = "This is a positive statement";
                                                            txtView.setTextColor(Color.parseColor("#32a852"));
                                                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                                                textdetect.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.bordergreen));
                                                            } else {
                                                                textdetect.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.bordergreen));
                                                            }

                                                            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                        }else{
                                                            String toSpeak = "This is a negative statement";
                                                            txtView.setTextColor(Color.parseColor("#f02011"));
                                                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                                                textdetect.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.borderred));
                                                            } else {
                                                                textdetect.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.borderred));
                                                            }

                                                            t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                                        }
                                                        input = new float[1][1000];
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        Log.i("Failure",e.toString());
                                                        // Task failed with an exception
                                                        // ...
                                                    }
                                                });
                            } catch (FirebaseMLException e) {
                                e.printStackTrace();
                            }
                        } catch (FirebaseMLException e) {
                            Log.i("Error",e.toString());
                        }
                    }else{
                        String toSpeak = "This is a neutral statement";
                        Toast.makeText(MainActivity.this, toSpeak,Toast.LENGTH_LONG).show();
                        txtView.setTextColor(Color.parseColor("#000000"));
                        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                        count =0;
                        input = new float[1][1000];
                    }



                    Log.i("input", Arrays.toString(input[0]));
                }
            }
        });

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectTxt();
            }
        });
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {

        CropImage.activity().setAspectRatio(900, 900).setCropMenuCropButtonIcon(R.drawable.ic_launcher_yes)
                .setMinCropResultSize(300, 300).start(MainActivity.this);
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                mImageUri = result.getUri();
                Picasso.get().load(mImageUri).into(imageView);
            }
        }

/*        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Log.i("Path",extras.toString());
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }*/
    }

    private void detectTxt(){
        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(MainActivity.this,mImageUri);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();;
            detector.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            processTxt(firebaseVisionText);
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                }
                            });
        } catch (Exception e) {
            Toast.makeText(MainActivity.this,"Capture Image",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void processTxt(FirebaseVisionText text) {
        resultText = text.getText();
        if(resultText.matches("")){
            Toast.makeText(MainActivity.this,"No Text",Toast.LENGTH_SHORT).show();
        }else{
            txtView.setText(resultText);
        }
/*        for (FirebaseVisionText.TextBlock block: text.getTextBlocks()) {
            String blockText = block.getText();
            Float blockConfidence = block.getConfidence();
            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();


            for (FirebaseVisionText.Line line: block.getLines()) {
                String lineText = line.getText();
                Float lineConfidence = line.getConfidence();
                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (FirebaseVisionText.Element element: line.getElements()) {
                    String elementText = element.getText();
                    Float elementConfidence = element.getConfidence();
                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
            }
        }*/
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("tokenizer.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
