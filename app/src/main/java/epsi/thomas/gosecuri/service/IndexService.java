package epsi.thomas.gosecuri.service;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import epsi.thomas.gosecuri.controller.IndexController;
import epsi.thomas.gosecuri.entity.Personne;
import epsi.thomas.gosecuri.firebase.FirebaseDetection;
import epsi.thomas.gosecuri.firebase.FirebaseInteraction;

public class IndexService {
    private FirebaseDetection firebaseDetection = new FirebaseDetection(this);
    private FirebaseInteraction firebaseInteraction = FirebaseInteraction.getInstance();
    private IndexController indexController;

    public IndexService(IndexController indexController){
        this.indexController = indexController;
    }

    public void detectText(Bitmap image){
        firebaseDetection.detectTextFromImage(image);
    }

    public void detectionResult(Personne personne){
        if(personne == null){
            Log.i("detectionResult", "fail");
            Toast.makeText(indexController.getApplicationContext(),"Impossible de récupérer vos informations",Toast.LENGTH_LONG).show();
        }else{
            Log.i("detectionResult", personne.getId());
            firebaseInteraction.addUser(personne);
            Toast.makeText(indexController.getApplicationContext(),"Succès",Toast.LENGTH_LONG).show();
        }
    }
}
