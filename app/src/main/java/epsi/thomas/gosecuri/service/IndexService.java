package epsi.thomas.gosecuri.service;

import android.graphics.Bitmap;

import epsi.thomas.gosecuri.entity.Personne;
import epsi.thomas.gosecuri.firebase.FirebaseDetection;

public class IndexService {
    private FirebaseDetection firebaseDetection = new FirebaseDetection();

    public Personne detectText(Bitmap image){
        Personne p = firebaseDetection.detectTextFromImage(image);
        return p;
    }
}
