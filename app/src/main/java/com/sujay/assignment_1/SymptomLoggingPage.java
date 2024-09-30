package com.sujay.assignment_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SymptomLoggingPage extends AppCompatActivity {
    public ContentValues val1 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_logging_page);

        Button uploadSymptomsSubmit=(Button) findViewById(R.id.uploadSymptomsSubmit);
        RatingBar ratingBar=(RatingBar) findViewById(R.id.ratingBar);


        Bundle b=getIntent().getExtras();
        if(b!=null)
            val1=b.getParcelable("Initial");

        AssignmentDbHelper dbHelper = new AssignmentDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        uploadSymptomsSubmit.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                try {
                    finalchk();
                    long newRowId = db.insert("tblPat", null, val1);
                }catch (SQLException e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                Toast.makeText(getApplicationContext(),"Data Inserted into Database. Redirecting in 5 Second",Toast.LENGTH_LONG).show();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent1;
                intent1 = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent1);
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setSelection(0);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Symptoms, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position>0){

                    ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                            String inp= String.valueOf(spinner.getSelectedItem());
                            String var="";
                            if(inp.compareTo("Nausea")==0){
                                var="symptomsNausea";
                            }else if(inp.compareTo("Headache")==0){
                                var="symptomsHeadache";
                            }else if(inp.compareTo("Diarrhea")==0){
                                var="symptomsDiarrhea";
                            }else if(inp.compareTo("Soar Throat")==0){
                                var="symptomsSoarThroat";
                            }else if(inp.compareTo("Fever")==0){
                                var="symptomsFever";
                            }else if(inp.compareTo("Muscle Ache")==0){
                                var="symptomsMuscleAche";
                            }else if(inp.compareTo("Loss of Smell or Taste")==0){
                                var="symptomsLOS";
                            }else if(inp.compareTo("Cough")==0){
                                var="symptomsCough";
                            }else if(inp.compareTo("Shortness of Breath")==0){
                                var="symptomsSOB";
                            }else if(inp.compareTo("Feeling Tired")==0){
                                var="FeelingFT";
                            }



                                val1.put(var,rating);
                        }
                    });
                    ratingBar.setRating(0);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void finalchk() {
        boolean False = false;
        if(val1.containsKey("timestamp")==False){
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            val1.put("timestamp",timeStamp);
        }
        if(val1.containsKey("symptomsNausea")==False){
                val1.put("symptomsNausea",0);
        }
        if(val1.containsKey("symptomsHeadache")==False){
            val1.put("symptomsHeadache",0);
        }
        if(val1.containsKey("symptomsDiarrhea")==False){
            val1.put("symptomsDiarrhea",0);
        }
        if(val1.containsKey("symptomsSoarThroat")==False){
            val1.put("symptomsSoarThroat",0);
        }
        if(val1.containsKey("symptomsFever")==False){
            val1.put("symptomsFever",0);
        }
        if(val1.containsKey("symptomsMuscleAche")==False){
            val1.put("symptomsMuscleAche",0);
        }
        if(val1.containsKey("symptomsLOS")==False) {
            val1.put("symptomsLOS", 0);
        }
        if(val1.containsKey("symptomsCough")==False) {
            val1.put("symptomsCough", 0);
        }
        if(val1.containsKey("symptomsSOB")==False) {
            val1.put("symptomsSOB", 0);
        }
        if(val1.containsKey("FeelingFT")==False) {
            val1.put("FeelingFT", 0);
        }

    }
}