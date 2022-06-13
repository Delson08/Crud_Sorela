package com.example.crud_sorela;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button cmdAdd, cmdRetrive, cmdDelete, cmdUpdate, viewAll;
    EditText etID, etName, etCourse, Year;
    ArrayList<Student> student = new ArrayList<>();
    private StorageReference ProductImagesRef;
    private Uri ImageUri;
    private static final int GalleryPick = 1;
    private String productRandomKey, downloadImageUrl;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef= database.getReference("Students");
    private ImageView InputProductImage;
    ImageView imgview;
    private String CategoryName;
    private DatabaseReference ProductsRef;
    private static final int PICK_IMAGE_REQUEST = 123;
    private Uri filePath;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProductsRef = FirebaseDatabase.getInstance().getReference().child("Students");
        ProductImagesRef = FirebaseStorage.getInstance().getReference().child("Student Images");

        InputProductImage = (ImageView) findViewById(R.id.image_view);
        InputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                OpenGallery();
            }


        });


        refs();

        cmdAdd.setOnClickListener(add);
        cmdRetrive.setOnClickListener(retrive);
        cmdDelete.setOnClickListener(delete);
        cmdUpdate.setOnClickListener(update);
        imgview.setOnClickListener(selectImage);

        addValueListener();
    }

    private void OpenGallery(){
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    private void StoreProductInformation()
    {

        final StorageReference filePath = ProductImagesRef.child(ImageUri.getLastPathSegment() + productRandomKey + ".jpg");
        final UploadTask uploadTask = filePath.putFile(ImageUri);

        SaveProductInfoToDatabase();


    }
    private void SaveProductInfoToDatabase()
    {
        HashMap<String, Object> productMap = new HashMap<>();

        productMap.put("image", downloadImageUrl);


        ProductsRef.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);

                            loadingBar.dismiss();
                            Toast.makeText(MainActivity.this, "Student is added successfully..", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                        StoreProductInformation();
                    }
                });
    }
    View.OnClickListener retrive= new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
//            (etID.getText().toString())
            if (TextUtils.isEmpty(etID.getText().toString()))
            {
                Toast.makeText(MainActivity.this, "Please input Student ID", Toast.LENGTH_SHORT).show();
            }
            else
            {
                StorageReference storageReference;
                storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference islandRef = storageReference.child("my images/"+etID.getText());
                final long ONE_MEGABYTE = 1024*1024;
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        DisplayMetrics dm = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(dm);
                        ImageView viewview = (ImageView) findViewById(R.id.image_view);
                        viewview.setImageBitmap(bm);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                    }
                });

                int flag = 0;
                for (Student s : student)
                {
                    if (s.getId().compareTo(etID.getText().toString()) == 0) {

                        etName.setText(s.getName());
                        etCourse.setText(s.getCourse());
                        Year.setText(s.getYear());

                        flag = 1;
                        break;
                    }
                }
                if (flag == 0)
                {
                    Toast.makeText(MainActivity.this, "No record's found! ", Toast.LENGTH_SHORT).show();
                    imgview.setImageResource(R.drawable.person_avatar);


                    etName.setText("");
                    etCourse.setText("");
                    Year.setText("");

                }
            }
        }
    };

    View.OnClickListener add=  new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {

            if (TextUtils.isEmpty(etID.getText().toString()) || TextUtils.isEmpty(etName.getText().toString()) || TextUtils.isEmpty(etCourse.getText().toString()) || TextUtils.isEmpty(Year.getText().toString()) || imgview.getDrawable() == null)
            {
                Toast.makeText(MainActivity.this, "There are empty fields", Toast.LENGTH_SHORT).show();
            }
            else
            {
                int flag = 0;
                for (Student s : student) {
                    if (s.getId().compareTo(etID.getText().toString()) == 0) {
                        Toast.makeText(MainActivity.this, "Already exist", Toast.LENGTH_SHORT).show();
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    String id, name, course, year1,image1;

                    id = etID.getText().toString();
                    name = etName.getText().toString();
                    course = etCourse.getText().toString();
                    year1 = Year.getText().toString();


                    Student student = new Student(id, name, course, year1);

                    myRef.child(id).setValue(student);
                    uploadPhoto();
                    addValueListener();
                    Toast.makeText(MainActivity.this, "Data Added", Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    etCourse.setText("");
                    Year.setText("");
                    imgview.setImageResource(R.drawable.person_avatar);
                }
            }
        }
    };

    View.OnClickListener update=  new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(etID.getText().toString()) || TextUtils.isEmpty(etName.getText().toString()) || TextUtils.isEmpty(etCourse.getText().toString()) || TextUtils.isEmpty(Year.getText().toString()) || imgview.getDrawable() == null)
            {
                Toast.makeText(MainActivity.this, "There are empty fields", Toast.LENGTH_SHORT).show();
            }
            else
            {
                int flag = 0;
                for (Student s : student)
                {
                    if (s.getId().compareTo(etID.getText().toString()) == 0) {
                        String id, name, course, year1,images;


                        id = etID.getText().toString();
                        name = etName.getText().toString();
                        course = etCourse.getText().toString();
                        year1 = Year.getText().toString();



                        Student student = new Student(id, name, course, year1);
                        myRef.child(id).setValue(student);

                        addValueListener();
                        uploadPhoto();
                        Toast.makeText(MainActivity.this, "Data Updated", Toast.LENGTH_SHORT).show();
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0)
                {

                    Toast.makeText(MainActivity.this, "No record's found!", Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    etCourse.setText("");
                    Year.setText("");
                    imgview.setImageResource(R.drawable.person_avatar);
                }
            }
        }
    };

    View.OnClickListener delete=  new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(etID.getText().toString()))
            {
                Toast.makeText(MainActivity.this, "Please input Student ID", Toast.LENGTH_SHORT).show();
            }
            else {
                int flag = 0;
                for (Student s : student)
                {
                    if (s.getId().compareTo(etID.getText().toString()) == 0) {
                        String id;
                        id = etID.getText().toString();

                        StorageReference storageReference;
                        storageReference = FirebaseStorage.getInstance().getReference();
                        StorageReference islandRef = storageReference.child("my images/"+etID.getText());

                        islandRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("Picture","#deleted");
                            }
                        });

                        myRef.child(id).removeValue();
                        addValueListener();
                        Toast.makeText(MainActivity.this, "Data Deleted", Toast.LENGTH_SHORT).show();
                        etName.setText("");
                        etCourse.setText("");
                        Year.setText("");
                        imgview.setImageResource(R.drawable.person_avatar);
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0)
                {
                    Toast.makeText(MainActivity.this, "No record's found!", Toast.LENGTH_SHORT).show();
                    etName.setText("");
                    etCourse.setText("");
                    Year.setText("");
                    imgview.setImageResource(R.drawable.person_avatar);

                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!= null && data.getData()!=null){
            filePath=data.getData();

            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgview.setImageBitmap(bitmap);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void uploadPhoto()
    {
        if(filePath!=null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Adding new record.");
            progressDialog.show();

            String id = etID.getText().toString();
            String name = etName.getText().toString();
            StorageReference uploadRef = FirebaseStorage.getInstance().getReference().child("my images/"+ id);
            ProductsRef = FirebaseDatabase.getInstance().getReference().child("Products");



            uploadRef.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Record Added.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setMessage(((int) progress) + "%");
                    });

        }
        else{
            Toast.makeText(getApplicationContext(), "Image Required" , Toast.LENGTH_SHORT).show();
        }
    }

    View.OnClickListener selectImage = view -> {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    };

    public void addValueListener(){
        myRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else {
                for(DataSnapshot ds:task.getResult().getChildren()){
                    student.add(ds.getValue(Student.class));

                }
            }
        });
    }

    public void refs(){
        cmdAdd=findViewById(R.id.cmdCreate);
        cmdRetrive=findViewById(R.id.cmdRetrive);
        cmdUpdate=findViewById(R.id.cmdUpdate);
        cmdDelete=findViewById(R.id.cmdDelete);
        etID=findViewById(R.id.stud_ID);
        etName=findViewById(R.id.stud_Name);
        etCourse=findViewById(R.id.stud_Course);
        Year = findViewById(R.id.stud_Year);
        imgview = findViewById(R.id.image_view);
    }


}