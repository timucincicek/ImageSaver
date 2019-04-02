package com.timucincicek.imagesaver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main2Activity extends AppCompatActivity {

    //declarations
    ImageView imageView;
    EditText editText;
    static SQLiteDatabase database;
    Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        Button button = (Button) findViewById(R.id.button);


        Intent intent = getIntent();
        //variable comes from previous activity represents selected data
        String info = intent.getStringExtra("info");
        //if image doesn't exists on database
        if (info.equalsIgnoreCase("new")) {

            Bitmap background = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.background);
            imageView.setImageBitmap(background);
            //make visible the button save to save new image
            button.setVisibility(View.VISIBLE);
            editText.setText("");

        } else {

            String name = intent.getStringExtra("name");
            editText.setText(name);
            int position = intent.getIntExtra("position", 0);
            imageView.setImageBitmap(MainActivity.artImage.get(position));
            button.setVisibility(View.INVISIBLE);
        }


    }


    public void select (View view) {
        //permissions

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        }
        else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //if there's no permission,ask user to get permission again
        if (requestCode == 2) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
            }

        }



        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            Uri image = data.getData();

            try {
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save (View view) {
        //get Name of image from editText
        String artName = editText.getText().toString();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //compress selected image
        selectedImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);

        byte[] byteArray = outputStream.toByteArray();


        try {
            //sql insert query to put database
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR, image BLOB)");

            String sqlString = "INSERT INTO arts (name, image) VALUES (?, ?)";
            SQLiteStatement statement = database.compileStatement(sqlString);
            statement.bindString(1,artName);
            statement.bindBlob(2,byteArray);
            statement.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //return back to first activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);


    }


}
