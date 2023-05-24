package hcmute.vika.appaiandroid;


import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DetectionModel {
    // dùng để load model và nhận diện
    private Interpreter interpreter;
    // lưu mảng  danh sách các label
    private List<String> listLabel;
    private  int INPUT_SIZE;
    private int PIXEL_SIZE=3; //RGB
    private int IMAGE_MEAN=0;
    private float IMAGE_SID=255.0f;
    // dùng để khởi tạo gpu trong app
    private static final int CAMERA_FRONT = 98;
    private static final int CAMERA_BACK =99;
    private int currentCamera ;
    private GpuDelegate gpuDelegate;
    private int height=0;
    private int width=0;
    public void setCamera(int camera){
        currentCamera=camera;
    }

    DetectionModel(AssetManager assetManager, String modelPath, String labelPath, int inputSize) throws IOException {
        INPUT_SIZE=inputSize;
        // dùng để xác định cpu hay gpu hay không
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
        options.addDelegate(gpuDelegate);
        options.setNumThreads(2);//
        interpreter=new Interpreter(loadModelFile(assetManager,modelPath),options);
        //load label
        listLabel=loadLabelList(assetManager,labelPath);
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        //dùng để chứa label
        List<String> labelList=new ArrayList<>();
        // dùng để tạo reader
        BufferedReader reader=new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        //lặp từng dòng và chứa vào labellist
        while ((line=reader.readLine())!=null){
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        //Dùng để lấy mô tả của file
        AssetFileDescriptor fileDescriptor=assetManager.openFd(modelPath);
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }
    public Mat recognizeImage(Mat mat_image){
        //Xoay ảnh gốc 90 độ ,lấy ra portrait image
        Mat rotated_mat_image=new Mat();
        if(currentCamera==CAMERA_BACK) {
            Core.flip(mat_image.t(), rotated_mat_image, 1);
        }
        else {
            Core.flip(mat_image.t(), rotated_mat_image, 0);
        }
        //Cover ảnh sang bitmap
        Bitmap bitmap =null;
        bitmap=Bitmap.createBitmap(rotated_mat_image.cols(),rotated_mat_image.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotated_mat_image,bitmap);
        // xác định chiều cao và chiều dài
        height=bitmap.getHeight();
        width=bitmap.getWidth();
        //Chia tỉ lệ bit map theo kích thước đầu vào của model
        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);
        //Convert bitmap thành bytebuffer làm mô hình đầu vào
        ByteBuffer byteBuffer=convertBitmaptoByteBuffer(scaledBitmap);
        //Xác định đầu ra
        // 10 : top 10 đối tượng phát hiện
        // 4: tọa độ trong hình ảnh

        Object[] input =new Object[1];
        input[0]=byteBuffer;
        // tạo ra treemap của 3 mảng (boxes,score,class)
        Map<Integer,Object> output_map=new TreeMap<>();
        float[][][]boxes=new float[1][10][4];
        //Chứa điểm của 10 đối tượng
        float[][] scores=new float[1][10];
        //Lưu class của đổi tượng
        float[][] classes=new float[1][10];
// dành cho model có sẵn
        //Thêm vào object_map
        output_map.put(0,boxes);
        output_map.put(1,classes);
        output_map.put(2,scores);
//        output_map.put(1,boxes);
//        output_map.put(3,classes);
//        output_map.put(0,scores);

        //dự đoán
        interpreter.runForMultipleInputsOutputs(input,output_map);

        Object value= output_map.get(0);
        Object object_class=output_map.get(1);
        Object object_score=output_map.get(2);
        // dành cho model tự train
//        Object value=output_map.get(1);
//        Object object_class=output_map.get(3);
//        Object object_score=output_map.get(0);

        //Lặp qua từng đối tượng
        for (int i=0; i<10;i++){

            float class_value=(float) Array.get(Array.get(object_class,0),i);
            float score_value= (float) Array.get(Array.get(object_score,0),i);
            //Xác định ngưỡng của điểm
            if (score_value>0.5){
                Object box1=Array.get(Array.get(value,0),i);
                //Nhân với chiều dài và chiều rộng của frame
                float top=(float) Array.get(box1,0)*height;
                float left=(float) Array.get(box1,1)*width;
                float bottom=(float) Array.get(box1,2)*height;
                float right=(float) Array.get(box1,3)*width;
                if(currentCamera==CAMERA_BACK){

                    // vẽ khung                          //Bắt đầu của khung   //Kết thúc của khung     //Màu khung             //Độ dày khung
                    Imgproc.rectangle(rotated_mat_image,new Point(left,top), new Point(right,bottom),new Scalar(255,155,255),2);
                    //Viết text trên frame            //tên của đối tượng                 //Điểm bắt đầu viết                                           //kích thước chữ
                    Imgproc.putText(rotated_mat_image,listLabel.get((int) class_value),new Point(left,top),3,1,new Scalar(102, 255, 102),2);
                    Core.flip(rotated_mat_image.t(),mat_image,0);
                }else {

                    Imgproc.rectangle(rotated_mat_image, new Point(left,top),new Point(right,bottom),new Scalar(255,155,255),2);
                    //Viết text trên frame            //tên của đối tượng                 //Điểm bắt đầu viết                                           //kích thước chữ
                    Imgproc.putText(rotated_mat_image,listLabel.get((int) class_value),new Point(left,top),3,1,new Scalar(102, 255, 102),2);

                    Core.flip(rotated_mat_image.t(),mat_image,1);
                }

            }
        }
        //Trước khi trả về xoay ảnh -90 độ

        return mat_image;
    }
    //    private ByteBuffer convertBitmaptoByteBuffer(Bitmap scaledBitmap) {
//        ByteBuffer byteBuffer;
//        int size_img=INPUT_SIZE; // 224
//
//        byteBuffer=ByteBuffer.allocateDirect(4*size_img*3);
//
//        byteBuffer.order(ByteOrder.nativeOrder());
//        byteBuffer.rewind();
//
//        int[] intValues= new int[size_img];
//        scaledBitmap.getPixels(intValues,0,scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight());
//        int pixel=0;
//        for(int i=0;i<size_img;++i){
//            for (int j=0;j<size_img;++j) {
//                final int val = intValues[pixel++];
//                byteBuffer.putFloat((((val >> 16) & 0xFF)) / 255.0f);
//                byteBuffer.putFloat((((val >> 8) & 0xFF)) / 255.0f);
//                byteBuffer.putFloat((((val) & 0xFF)) / 255.0f);
//            }
//        }
//        byteBuffer.flip();
//        return  byteBuffer;
//    }
    private ByteBuffer convertBitmaptoByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer;

        int quantity=0;
        int size_img=INPUT_SIZE;
        if(quantity==0){
            byteBuffer=ByteBuffer.allocateDirect(1*size_img*size_img*3);

        }
        else {
            byteBuffer=ByteBuffer.allocateDirect(4*1*size_img*size_img*3);

        }
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.rewind();
        int[] intValues= new int[size_img*size_img];
        scaledBitmap.getPixels(intValues,0,scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight());
        int pixel=0;
        for(int i=0;i<size_img;++i){
            for (int j=0;j<size_img;++j){
                final  int val=intValues[pixel++];
                if(quantity==0){
                    byteBuffer.put((byte)((val>>16)&0xFF));
                    byteBuffer.put((byte)((val>>8)&0xFF));
                    byteBuffer.put((byte)(val&0xFF));
                }else {
                    byteBuffer.putFloat((((val >> 16) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val >>8) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val) & 0xFF))/255.0f);
                }
            }
        }
        byteBuffer.flip();
        return  byteBuffer;
    }

}
