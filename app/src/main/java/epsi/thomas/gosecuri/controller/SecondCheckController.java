package epsi.thomas.gosecuri.controller;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;

import epsi.thomas.gosecuri.R;
import epsi.thomas.gosecuri.entity.Personne;
import epsi.thomas.gosecuri.service.SecondCheckService;

public class SecondCheckController extends AppCompatActivity {

    private ImageView imageView;
    private Button btnCamera;
    private Uri imageUri;
    private Bitmap bitmapFace;
    private Button btnValidate;
    public FrameLayout progressLayout;
    public TextView textView;

    public Uri idUri;
    public Personne personne;

    private SecondCheckService secondCheckService;

    private static final int CAMERA_REQUEST = 1888;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondcheck);

        secondCheckService = new SecondCheckService(this);

        personne = (Personne) getIntent().getSerializableExtra("personne");
        String uriString = getIntent().getStringExtra("imageUri");
        idUri = Uri.parse(uriString);

        btnCamera = findViewById(R.id.btnCamera);
        imageView = findViewById(R.id.imageView);
        btnValidate = findViewById(R.id.btnValidate);
        progressLayout = findViewById(R.id.progressLayout);
        textView = findViewById(R.id.textView);
    }

    public void cameraClick(View v) {
        imageView.setBackgroundColor(Color.rgb(0, 0, 0));
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Face");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                imageView.setImageBitmap(secondCheckService.rotateImage(this, imageUri));
                ((LinearLayout.LayoutParams) btnCamera.getLayoutParams()).weight = 1;
                btnValidate.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void validateClick(View v) {
        progressLayout.setVisibility(View.VISIBLE);
        Bitmap bitmapId = null;
        try {
            bitmapId = MediaStore.Images.Media.getBitmap(getContentResolver(), idUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        bitmapFace = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.8), (int)(bitmap.getHeight()*0.8), true);
        secondCheckService.checkFace(bitmapFace, bitmapId);
    }
}
