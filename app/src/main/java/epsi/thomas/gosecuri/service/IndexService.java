package epsi.thomas.gosecuri.service;

import android.graphics.Bitmap;

import epsi.thomas.gosecuri.firebase.FirebaseDetection;

public class IndexService {
    private FirebaseDetection firebaseDetection = new FirebaseDetection();

    public void detectText(Bitmap image){
        firebaseDetection.detectTextFromImage(image);
    }
}
