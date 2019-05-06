package epsi.thomas.gosecuri;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import epsi.thomas.gosecuri.api.ocr.OcrCni;

public class IndexActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button btnCamera;
    private Button btnValidate;
    private File tempFile;
    private final String TEMP_FILE_NAME = "temp_img_file.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        btnCamera = findViewById(R.id.btnCamera);
        btnValidate = findViewById(R.id.btnValidate);
        imageView = findViewById(R.id.imageView);

        File cDir = getBaseContext().getCacheDir();
        tempFile = new File(cDir.getPath() + "/" + TEMP_FILE_NAME) ;


        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });

        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OcrCni(IndexActivity.this, getBaseContext().getCacheDir().getPath() + "/" + TEMP_FILE_NAME).execute();
            }
        });
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap = (Bitmap) data.getExtras().get("data");

        imageView.setImageBitmap(bitmap);
        btnValidate.setVisibility(View.VISIBLE);
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, outStream);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
