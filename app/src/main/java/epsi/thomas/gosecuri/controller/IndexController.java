package epsi.thomas.gosecuri.controller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import epsi.thomas.gosecuri.R;
import epsi.thomas.gosecuri.service.IndexService;
import epsi.thomas.gosecuri.utils.ConnectionUtils;

import static epsi.thomas.gosecuri.service.IndexService.hasPermissions;

public class IndexController extends AppCompatActivity {

    private Bitmap bitmapId;
    private Button btnValidate;
    private Button btnCamera;

    public ImageView imageView;
    public Uri imageUri;
    public FrameLayout progressLayout;
    public TextView textView;

    private IndexService indexService;

    private static final int CAMERA_REQUEST = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        FirebaseApp.initializeApp(this);

        indexService = new IndexService(this);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        btnCamera = findViewById(R.id.btnCamera);
        imageView = findViewById(R.id.imageView);
        btnValidate = findViewById(R.id.btnValidate);
        progressLayout = findViewById(R.id.progressLayout);
        textView = findViewById(R.id.textView);

        String message = getIntent().getStringExtra("message");

        if (message != null) {
            textView.setText(message);
        }
        ConnectionUtils.connectionTest(this);
    }

    public void cameraClick(View v) {
        textView.setText(null);
        imageView.setBackgroundColor(Color.rgb(0, 0, 0));
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "IDCard");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                imageView.setImageBitmap(indexService.rotateImage(this, imageUri));
                ((LinearLayout.LayoutParams) btnCamera.getLayoutParams()).weight = 1;
                btnValidate.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void validateClick(View v) {
        progressLayout.setVisibility(View.VISIBLE);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        bitmapId = drawable.getBitmap();
        indexService.detectText(bitmapId);
    }
}
