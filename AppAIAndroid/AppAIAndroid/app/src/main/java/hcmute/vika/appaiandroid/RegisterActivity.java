package hcmute.vika.appaiandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextUsername,editTextPassword, editConfirmPassword;;

    private Button buttonRegister;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbHelper = new DBHelper(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        editConfirmPassword=findViewById(R.id.editconfirmPassword);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                String confirm = editConfirmPassword.getText().toString();
                if (!password.equals(confirm)) {
                    // Hiển thị thông báo lỗi khi mật khẩu và xác nhận mật khẩu không khớp
                    Toast.makeText(RegisterActivity.this, "Password does not match", Toast.LENGTH_SHORT).show();
                }else if(password.equals(confirm)) {
                    if(register(username)){
                        // Thêm người dùng mới vào cơ sở dữ liệu
                        dbHelper.addUser(username, password);
                        // Đăng ký thành công, chuyển đến trang đăng nhập
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("ToastRegister","Sign up success");
                        startActivity(intent);
                    }else {
                        // Hiển thị thông báo lỗi đăng ký
                        Toast.makeText(RegisterActivity.this, "Sign up failed! The account have existed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
    private boolean register(String username) {
        // Kiểm tra xem người dùng đã tồn tại hay chưa
        if (dbHelper.checkUser(username)) {
            // Người dùng đã tồn tại, đăng ký không thành công
            return false;
        }
        return true;
    }
}
