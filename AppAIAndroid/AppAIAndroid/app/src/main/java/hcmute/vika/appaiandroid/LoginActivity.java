package hcmute.vika.appaiandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername,editTextPassword;

    private Button buttonLogin,buttonRegister;
    private TextView textViewForgotPassword;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                if (login(username, password)) {
                    // Đăng nhập thành công, chuyển đến màn hình chính
                     Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                     startActivity(intent);
                } else {
                    // Hiển thị thông báo lỗi đăng nhập
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });


        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String username = editTextUsername.getText().toString();
//                String password = editTextPassword.getText().toString();
//                if (register(username, password)) {
//                    // Đăng ký thành công, chuyển đến trang đăng nhập
//                    // Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
//                    // startActivity(intent);
//                } else {
//                    // Hiển thị thông báo lỗi đăng ký
//                    Toast.makeText(LoginActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
//                }
                 Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                 startActivity(intent);
            }
        });
    }

    private boolean login(String username, String password) {
        // Kiểm tra thông tin đăng nhập
        return dbHelper.checkUser(username, password);
    }

    private boolean register(String username, String password) {
        // Kiểm tra xem người dùng đã tồn tại hay chưa
        if (dbHelper.checkUser(username,password)) {
            // Người dùng đã tồn tại, đăng ký không thành công
            return false;
        }

        // Thêm người dùng mới vào cơ sở dữ liệu
        dbHelper.addUser(username, password);
        return true;
    }
}
