package org.tensorflow.lite.examples.classification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {
    EditText etname, etaddress, etcontact, etemail, etaddhar, etpass, etconformpass, etotp;
    Button btn_reg;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    String Emailpattern="^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    String Pattern = "[0-9]{10}";
    String AdharPattern = "[0-9]{12}";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etname =findViewById(R.id.etname);
        etaddress =findViewById(R.id.etAddress);
        etcontact =findViewById(R.id.etContact);
        etemail =findViewById(R.id.etMail);
        etaddhar =findViewById(R.id.etAadhar);
        etpass =findViewById(R.id.etPassword);
        etconformpass =findViewById(R.id.etConformpassword);

        btn_reg=findViewById(R.id.btn_reg);

        databaseReference= firebaseDatabase.getInstance().getReference("/User");

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etname.getText().toString().isEmpty()) {
                    etname.setError("Enter Name");
                    return;
                }
                if (etaddress.getText().toString().isEmpty()) {
                    etaddress.setError("Enter Address");
                    return;
                }
                if (etcontact.getText().toString().isEmpty()) {
                    etcontact.setError("Enter Contact");
                    return;
                }
                if(!etcontact.getText().toString().trim().matches(Pattern))
                {
                    etcontact.setError("Please enter valid 10 digit phone number");
                    return;
                }
                if(etemail.getText().toString().isEmpty())
                {
                    etemail.setError("Enter email");
                }
                if (!etemail.getText().toString().trim().matches(Emailpattern)){
                    etemail.setError("Email not valid");
                    return;
                }
                if(etaddhar.getText().toString().isEmpty())
                {
                    etaddhar.setError("Enter Addhar NO");
                    return;
                }
                if(!etaddhar.getText().toString().trim().matches(AdharPattern))
                {
                    etaddhar.setError("Please enter valid 12 digit Addhar number");
                    return;
                }
                if(etpass.getText().toString().isEmpty())
                {
                    etpass.setError("Enter Password");
                    return;

                }
                if(etconformpass.getText().toString().isEmpty())
                {
                    etconformpass.setError("Enter Password");
                    return;

                }
                if (!etconformpass.getText().toString().equals(etpass.getText().toString())){
                    etconformpass.setError("pass not valid");
                    return;

                }

                if(etconformpass.getText().toString().isEmpty())
                {
                    etconformpass.setError("Enter Confirm password");
                    if (!etconformpass.getText().toString().equals(etpass.getText().toString()))
                    {
                        etconformpass.setError("Enter correct password");

                    }
                }

                User_Info user=new User_Info();
                user.setContact(etcontact.getText().toString());
                user.setName(etname.getText().toString());
                user.setAddhar(etaddhar.getText().toString());
                user.setAddress(etaddress.getText().toString());
                user.setEmail(etemail.getText().toString());
                user.setPass(etpass.getText().toString());
                user.setConform_pass(etconformpass.getText().toString());
                String key=databaseReference.push().getKey();
                user.setUserkey(key);


                databaseReference.child(user.getContact()).setValue(user, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        Toast.makeText(Registration.this,"Data Added successfully",Toast.LENGTH_SHORT).show();

                        Intent intent=new Intent(Registration.this, Login.class);
                        startActivity(intent);
                    }
                });
            }

        });

    }
}