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
    private EditText editTextUsername,editTextPassword;

    private Button buttonLogin,buttonRegister;
    private TextView textViewForgotPassword;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbHelper = new DBHelper(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                if (register(username, password)) {
                    // Thêm người dùng mới vào cơ sở dữ liệu
                    dbHelper.addUser(username, password);
                    // Đăng ký thành công, chuyển đến trang đăng nhập
                     Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                     startActivity(intent);
                } else {
                    // Hiển thị thông báo lỗi đăng ký
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private boolean register(String username, String password) {
        // Kiểm tra xem người dùng đã tồn tại hay chưa
        if (dbHelper.checkUser(username,password)) {
            // Người dùng đã tồn tại, đăng ký không thành công
            return false;
        }
        return true;
    }
}
