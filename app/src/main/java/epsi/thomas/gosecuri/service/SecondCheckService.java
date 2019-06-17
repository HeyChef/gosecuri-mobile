package epsi.thomas.gosecuri.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import epsi.thomas.gosecuri.api.FaceDetection;
import epsi.thomas.gosecuri.controller.IndexController;
import epsi.thomas.gosecuri.controller.SecondCheckController;
import epsi.thomas.gosecuri.entity.Personne;
import epsi.thomas.gosecuri.firebase.FirebaseInteraction;
import epsi.thomas.gosecuri.firebase.FirebaseStorageCloud;
import epsi.thomas.gosecuri.utils.PicturesScaling;

public class SecondCheckService {
    private FirebaseInteraction firebaseInteraction = FirebaseInteraction.getInstance();
    private FirebaseStorageCloud firebaseStorageCloud = FirebaseStorageCloud.getInstance();
    private SecondCheckController secondCheckController;
    private PicturesScaling picturesScaling = new PicturesScaling();

    public SecondCheckService(SecondCheckController secondCheckController) {
        this.secondCheckController = secondCheckController;
    }

    public void checkFace(Bitmap bitmap, Bitmap idBitmap) {
        new FaceDetection(secondCheckController.personne, this).execute(bitmap, idBitmap);
    }

    public void faceResult(String face, Personne personne) {
        try {
            Boolean isValid = false;
            JSONObject obj = new JSONObject(face);
            if (obj.has("confidence")) {
                Float coef = BigDecimal.valueOf(obj.getDouble("confidence")).floatValue();
                System.out.println(coef);
                if (coef >= 50.0) {
                    isValid = true;
                }
            }
            String message = "Visiteur non reconnu";
            if (isValid) {
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = dateFormat.format(date);
                personne.setDate_visite(strDate);
                firebaseInteraction.addUser(personne);
                message = "Visiteur reconnu";
            } else {
                firebaseStorageCloud.deleteFiles(personne);
            }

            secondCheckController.progressLayout.setVisibility(View.GONE);
            Intent intent = new Intent(secondCheckController, IndexController.class);
            intent.putExtra("message", message);

            secondCheckController.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Bitmap rotateImage(Context context, Uri imageUri) {
        return picturesScaling.rotateImage(context, imageUri);
    }
}
