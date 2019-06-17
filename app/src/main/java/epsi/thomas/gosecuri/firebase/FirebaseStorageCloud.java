package epsi.thomas.gosecuri.firebase;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import epsi.thomas.gosecuri.entity.Personne;

public class FirebaseStorageCloud {

    private static FirebaseStorageCloud INSTANCE = new FirebaseStorageCloud();
    private FirebaseStorage storage;

    public static FirebaseStorageCloud getInstance() {
        return INSTANCE;
    }

    private FirebaseStorageCloud() {
        storage = FirebaseStorage.getInstance();
    }

    public Personne uploadFiles(Bitmap faceBitmap, Bitmap idBitmap, final Personne personne) {

        String name = personne.getNom() + "." + personne.getPrenom();
        final StorageReference storageReference = storage.getReference().child(name).child("Id.jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        idBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] idData = baos.toByteArray();

        baos = new ByteArrayOutputStream();
        final StorageReference storageReference2 = storage.getReference().child(name).child("Face.jpg");

        faceBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] faceFata = baos.toByteArray();


        UploadTask uploadTask = storageReference.putBytes(idData);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    personne.setImageURL(downloadUri.toString());
                }
            }
        });

        UploadTask uploadTask2 = storageReference2.putBytes(faceFata);
        Task<Uri> urlTask2 = uploadTask2.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return storageReference2.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    personne.setFaceURL(downloadUri.toString());
                }
            }
        });

        try {
            Tasks.await(urlTask);
            Tasks.await(urlTask2);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return personne;
    }

    public void deleteFiles(Personne personne) {
        StorageReference storageRef = storage.getReference();
        StorageReference faceRef = storageRef.child(personne.getNom() + "." + personne.getPrenom()+ "/Face.jpg");
        StorageReference idRef = storageRef.child(personne.getNom() + "." + personne.getPrenom()+ "/Id.jpg");

        idRef.delete();
        faceRef.delete();
    }
}
