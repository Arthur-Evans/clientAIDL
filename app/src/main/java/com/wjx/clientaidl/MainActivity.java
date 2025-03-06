package com.wjx.clientaidl;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wjx.serveraidl.IMathService;


public class MainActivity extends AppCompatActivity {


    private IMathService mathService;
    private boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mathService = IMathService.Stub.asInterface(service);
            isBound = true;
            Toast.makeText(MainActivity.this, "服务已连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mathService = null;
            isBound = false;
            Toast.makeText(MainActivity.this, "服务断开", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        // 初始化视图
        EditText etNum1 = findViewById(R.id.etNum1);
        EditText etNum2 = findViewById(R.id.etNum2);
        TextView tvResult = findViewById(R.id.tvResult);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnSubtract = findViewById(R.id.btnSubtract);
        Button btnMultiply = findViewById(R.id.btnMultiply);
        Button btnDivide = findViewById(R.id.btnDivide);

        // 绑定服务
        Intent intent = new Intent();
        intent.setAction("com.wjx.aidlservice.MATH_SERVICE");
        intent.setPackage("com.wjx.serveraidl");
        bindService(intent, connection, BIND_AUTO_CREATE);

        // 设置按钮点击事件
        btnAdd.setOnClickListener(v -> calculate(IMathOperation.ADD));
        btnSubtract.setOnClickListener(v -> calculate(IMathOperation.SUBTRACT));
        btnMultiply.setOnClickListener(v -> calculate(IMathOperation.MULTIPLY));
        btnDivide.setOnClickListener(v -> calculate(IMathOperation.DIVIDE));
    }


    private interface IMathOperation {
        int ADD = 1;
        int SUBTRACT = 2;
        int MULTIPLY = 3;
        int DIVIDE = 4;
    }

    private void calculate(int operation) {
        if (!isBound) {
            Toast.makeText(this, "服务未连接", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText etNum1 = findViewById(R.id.etNum1);
        EditText etNum2 = findViewById(R.id.etNum2);
        TextView tvResult = findViewById(R.id.tvResult);

        try {
            int num1 = Integer.parseInt(etNum1.getText().toString());
            int num2 = Integer.parseInt(etNum2.getText().toString());
            int result = 0;

            switch (operation) {
                case IMathOperation.ADD:
                    result = mathService.add(num1, num2);
                    break;
                case IMathOperation.SUBTRACT:
                    result = mathService.subtract(num1, num2);
                    break;
                case IMathOperation.MULTIPLY:
                    result = mathService.multiply(num1, num2);
                    break;
                case IMathOperation.DIVIDE:
                    result = mathService.divide(num1, num2);
                    break;
            }
            tvResult.setText("结果: " + result);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Toast.makeText(this, "远程调用失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }


}