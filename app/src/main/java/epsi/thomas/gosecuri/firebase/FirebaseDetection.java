package epsi.thomas.gosecuri.firebase;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirebaseDetection {

    public void detectTextFromImage(Bitmap bitmapImage) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmapImage);

        FirebaseVisionCloudDocumentRecognizerOptions options =
                new FirebaseVisionCloudDocumentRecognizerOptions.Builder()
                        .setLanguageHints(Arrays.asList("fr"))
                        .build();
        FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer(options);

        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                    @Override
                    public void onSuccess(FirebaseVisionDocumentText result) {
                        getText(result);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Labeltest", e.toString());
                    }
                });
    }

    private void getText(FirebaseVisionDocumentText result) {
        Pattern nomLabel = Pattern.compile("Nom");
        Pattern prenomLabel = Pattern.compile("Prénom");
        Pattern idLabel = Pattern.compile("IDFRA.*$");

        String nom = "";
        String prenom = "";
        String id = "";

        for (FirebaseVisionDocumentText.Block block : result.getBlocks()) {
            for (FirebaseVisionDocumentText.Paragraph paragraph : block.getParagraphs()) {
                String paragraphText = paragraph.getText();

                paragraphText = paragraphText.replace("\n","");

                Matcher nomMatch = nomLabel.matcher(paragraphText);
                Matcher prenomMatch = prenomLabel.matcher(paragraphText);
                Matcher idMatch = idLabel.matcher(paragraphText);

                if(nomMatch.find()){
                    String[] string = paragraphText.split(":");
                    nom = string[1];
                }

                if(prenomMatch.find()){
                    String[] string = paragraphText.split(":");
                    prenom = string[1];
                }

                if(idMatch.find()){
                    id = idMatch.group();
                }
                Log.i("GetLabel",nom);
                Log.i("GetLabel",prenom);
                Log.i("GetLabel",id);
            }
        }
    }
}
