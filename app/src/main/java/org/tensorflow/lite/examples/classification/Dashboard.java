package org.tensorflow.lite.examples.classification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Dashboard extends AppCompatActivity {
CardView cardobj,cardvoice,carddevice;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        cardobj=findViewById(R.id.cardobj);
        carddevice=findViewById(R.id.carddecive);
        cardvoice=findViewById(R.id.cardvoice);


        cardobj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Dashboard.this,ClassifierActivity2.class);
                startActivity(intent);
            }
        });

        cardvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Dashboard.this,Voice_reognization.class);
                startActivity(intent);
            }
        });
        carddevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Dashboard.this,Control_Device.class);
                startActivity(intent);
            }
        });

    }
}