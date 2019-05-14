package epsi.thomas.gosecuri.firebase;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import epsi.thomas.gosecuri.entity.Personne;
import epsi.thomas.gosecuri.service.IndexService;

public class FirebaseDetection {

    private Personne personne;
    private IndexService indexService;

    public FirebaseDetection(IndexService indexService){
        this.indexService = indexService;
    }

    public Personne detectTextFromImage(Bitmap bitmapImage) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmapImage);

        FirebaseVisionCloudDocumentRecognizerOptions options =
                new FirebaseVisionCloudDocumentRecognizerOptions.Builder()
                        .setLanguageHints(Arrays.asList("fr"))
                        .build();
        FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer(options);

        Task t = detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                    @Override
                    public void onSuccess(FirebaseVisionDocumentText result) {
                        personne = getPersonne(result);
                        indexService.detectionResult(personne);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Labeltest", e.toString());
                    }
                });
        return personne;
    }

    private Personne getPersonne(FirebaseVisionDocumentText result) {
        Pattern nomLabel = Pattern.compile("Nom");
        Pattern prenomLabel = Pattern.compile("Prénom");
        Pattern idLabel = Pattern.compile("IDFRA.*$");

        String nom = "";
        String prenom = "";
        String id = "";

        for (FirebaseVisionDocumentText.Block block : result.getBlocks()) {
            for (FirebaseVisionDocumentText.Paragraph paragraph : block.getParagraphs()) {
                String paragraphText = paragraph.getText();

                paragraphText = paragraphText.replace("\n", "");

                Matcher nomMatch = nomLabel.matcher(paragraphText);
                Matcher prenomMatch = prenomLabel.matcher(paragraphText);
                Matcher idMatch = idLabel.matcher(paragraphText);

                if (nomMatch.find()) {
                    String[] string = paragraphText.split(":");
                    if (1 <= string.length) {
                        nom = string[1];
                        nom.replace(" ", "");
                    }
                }

                if (prenomMatch.find()) {
                    String[] string = paragraphText.split(":");
                    if (!string[1].isEmpty()) {
                        string = string[1].split(",");
                        prenom = string[0].replace(" ", "");
                    }
                }

                if (idMatch.find()) {
                    id = idMatch.group().replace(" ", "");
                }
            }
        }
        Log.i("GetLabel nom ", nom);
        Log.i("GetLabel prenom ", prenom);
        Log.i("GetLabel id ", id);
        Personne personne = new Personne();
        if (nom != "" && prenom != "" && id != "") {
            personne = new Personne(nom, prenom, id);
        }
        return personne;
    }
}
