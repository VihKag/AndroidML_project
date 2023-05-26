package hcmute.vika.appaiandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import hcmute.vika.appaiandroid.ml.MobilenetV110224Quant;


public class MainActivity extends AppCompatActivity {
    Button selectedBtn,captureBtn,predictBtn,searchBtn;
    FloatingActionButton cameraBtn;
    TextView result;
    ImageView imageView;
    Bitmap bitmap;
    Boolean checkCapture=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getPermission();
        String[] labels=new String[1001];
        int cnt=0;
        try {
            BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String line=bufferedReader.readLine();
            while (line!=null && cnt < 1001){
                labels[cnt]=line;
                cnt++;
                line=bufferedReader.readLine();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        Init();
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(MainActivity.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        selectedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,10);
            }
        });
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCapture=true;
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,12);
            }
        });

        predictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(MainActivity.this);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

                    // Convert bitmap to ARGB_8888 format
                    Bitmap argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    inputFeature0.loadBuffer(TensorImage.fromBitmap(argbBitmap).getBuffer());

                    // Runs model inference and gets result.
                    MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    result.setText(labels[getMax(outputFeature0.getFloatArray())] + "");
                    // Releases model resources if no longer used.
                    Log.e("predict", labels.toString());
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }
                Log.e("predict", "answer");
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = result.getText().toString();
                // Hiển thị kết quả tìm kiếm trong giao diện người dùng
                Intent intent = new Intent(MainActivity.this, ResultSearchActivity.class);
                Bundle bundle = new Bundle();
//                bundle.putByteArray("imageByteArray", byteArray);
                bundle.putString("query",query);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void Init() {
        cameraBtn=findViewById(R.id.floatingActionButton);
        selectedBtn=findViewById(R.id.button);
        captureBtn=findViewById(R.id.button2);
        predictBtn=findViewById(R.id.button3);
        result=findViewById(R.id.textView2);
        imageView=findViewById(R.id.imageView);
        searchBtn=findViewById(R.id.searchButton);
    }


    int getMax(float[] arr){
        int max=0;
        for(int i=0;i<arr.length;i++){
            if(arr[i]>arr[max])max=i;
        }
        return max;
    }
    void getPermission(){
        if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},11);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==11){
            if(grantResults.length>0){
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    this.getPermission();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==10){
            if(data!=null){
                Uri uri=data.getData();
                try {
                    bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else if(requestCode==12){
            if(data!=null) {
                bitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bitmap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}