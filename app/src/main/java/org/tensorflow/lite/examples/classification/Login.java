package org.tensorflow.lite.examples.classification;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class Login extends AppCompatActivity {
    TextView registration;
    EditText username, userpass;
    Button btn_login;
    public static int langflg=0;
    DatabaseReference databaseReference;
    User_Info userInfo;
    int flg=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.edit_username);
        userpass = findViewById(R.id.edit_pass);
        btn_login = findViewById(R.id.btn_login);
        registration=findViewById(R.id.registration);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username.getText().toString().isEmpty())
                {
                    username.setError("Enter Username.");
                }
                if(userpass.getText().toString().isEmpty())
                {
                    userpass.setError("Enter password");
                }



                databaseReference= FirebaseDatabase.getInstance().getReference("/User");

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        flg=0;
                        for(DataSnapshot data : snapshot.getChildren()){
                            //flg=0;
                            userInfo=data.getValue(User_Info.class);
                            String contact =userInfo.getContact();
                            String pass = userInfo.getPass();

                            if (contact.equals(username.getText().toString()) && pass.equals(userpass.getText().toString()) ){
                                Toast.makeText(Login.this, "Login Successfull", Toast.LENGTH_SHORT).show();


                                SharedPreferences.Editor sh = Login.this.getSharedPreferences(App_constant.SH_STOARGE_KEY, MODE_PRIVATE).edit();
                                Gson gson = new Gson();
                                String json = gson.toJson(userInfo);
                                sh.putString(App_constant.SH_USER_INFO, json);
                                sh.commit();

                                flg=1;

                                Intent intent1 = new Intent(Login.this, Dashboard.class);
                                startActivity(intent1);

                            }

                        }
                        if (flg==0)
                        {
                            Toast.makeText(Login.this, " Login UnSuccessfully", Toast.LENGTH_SHORT).show();

                        }





                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }


        });
        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Login.this, Registration.class);
                startActivity(intent);
            }
        });
    }
}

