package epsi.thomas.gosecuri.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import epsi.thomas.gosecuri.controller.IndexController;
import epsi.thomas.gosecuri.controller.SecondCheckController;
import epsi.thomas.gosecuri.entity.Personne;
import epsi.thomas.gosecuri.firebase.FirebaseDetection;
import epsi.thomas.gosecuri.firebase.FirebaseInteraction;
import epsi.thomas.gosecuri.utils.PicturesScaling;

public class IndexService {
    private FirebaseDetection firebaseDetection = new FirebaseDetection(this);
    private FirebaseInteraction firebaseInteraction = FirebaseInteraction.getInstance();
    private IndexController indexController;
    private PicturesScaling picturesScaling = new PicturesScaling();

    private Bitmap idBitmap;

    public IndexService(IndexController indexController) {
        this.indexController = indexController;
    }

    public void detectText(Bitmap bitmap) {
        idBitmap = bitmap;
        firebaseDetection.detectTextFromImage(idBitmap);
    }

    public void detectionResult(Personne personne) {
        if (personne == null) {
            Toast.makeText(indexController.getApplicationContext(), "Impossible de récupérer vos informations", Toast.LENGTH_LONG).show();
            indexController.progressLayout.setVisibility(View.GONE);
        } else {
            firebaseInteraction.checkUser(personne, this);
        }
    }

    public void checkResult(Boolean isValid, Personne personne) {
        if (isValid == true) {
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = dateFormat.format(date);
            personne.setDate_visite(strDate);
            firebaseInteraction.updateUser(personne);
            indexController.textView.setText("Visiteur reconnu");
        } else {
            Intent intent = new Intent(indexController, SecondCheckController.class);
            intent.putExtra("personne", personne);
            intent.putExtra("imageUri", indexController.imageUri.toString());

            indexController.startActivity(intent);

            Toast.makeText(indexController.getApplicationContext(), "Impossible de récupérer vos informations", Toast.LENGTH_LONG).show();
        }
        indexController.progressLayout.setVisibility(View.GONE);
    }

    public Bitmap rotateImage(Context context, Uri imageUri) {
        return picturesScaling.rotateImage(context, imageUri);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
