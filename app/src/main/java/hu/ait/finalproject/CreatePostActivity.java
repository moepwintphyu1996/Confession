package hu.ait.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.finalproject.Data.Post;

public class CreatePostActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_TAKE_PHOTO = 101;
    @BindView(R.id.etTitle)
    EditText etTitle;
    @BindView(R.id.etBody)
    EditText etBody;
    @BindView(R.id.imgAttach)
    ImageView imgAttach;

    public Post newPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnAttach)
    public void attachClick() {
        Intent intentTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentTakePhoto, REQUEST_CODE_TAKE_PHOTO);
    }

    @OnClick(R.id.btnSend)
    public void sendClick() {
        if (imgAttach.getVisibility() == View.GONE) {
            if(!etTitle.getText().toString().equals("") && !etBody.getText().toString().equals("")){
                uploadPost();
            }

            else{
                if(etTitle.getText().toString().equals("") && etBody.getText().toString().equals("")){
                    etBody.setError(getText(R.string.emptyString));
                    etTitle.setError(getText(R.string.emptyString));
                }
                if(etBody.getText().toString().equals("")){
                    etBody.setError(getText(R.string.emptyString));
                }
                else{
                    etTitle.setError(getText(R.string.emptyString));
                }
            }

        } else {
            try {
                uploadPostWithImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            Bitmap img = (Bitmap) data.getExtras().get("data");
            imgAttach.setImageBitmap(img);
            imgAttach.setVisibility(View.VISIBLE);
        }
    }

    private void uploadPost(String... imageUrl) {
        String key = FirebaseDatabase.getInstance().
                getReference().child("posts").push().getKey();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        newPost = new Post(
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                etTitle.getText().toString(),
                etBody.getText().toString(),formattedDate
        );



        if (imageUrl != null && imageUrl.length>0) {
            newPost.setImageUrl(imageUrl[0]);
        }

        FirebaseDatabase.getInstance().getReference().
                child("posts").child(key).setValue(newPost);

        deleteAfterOneDay();

        finish();
    }

    public void deleteAfterOneDay(){
        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(86400, TimeUnit.DAYS);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query timeStamp = mDatabase.child("posts").orderByChild("timeStamp").endAt(cutoff);
    }

    public void uploadPostWithImage() throws Exception {
        imgAttach.setDrawingCacheEnabled(true);
        imgAttach.buildDrawingCache();
        Bitmap bitmap = imgAttach.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInBytes = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8")+".jpg";
        StorageReference newImageRef = storageRef.child(newImage);
        StorageReference newImageImagesRef = storageRef.child("images/"+newImage);
        newImageRef.getName().equals(newImageImagesRef.getName());    // true
        newImageRef.getPath().equals(newImageImagesRef.getPath());    // false

        UploadTask uploadTask = newImageImagesRef.putBytes(imageInBytes);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(CreatePostActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                uploadPost(taskSnapshot.getDownloadUrl().toString());
            }
        });
    }


}
