package com.example.emailauthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    EditText fullName,email,password,phone;
    Button registerBtn,goToLogin;
    CheckBox isTeacherBox, isStudentBox;

    boolean valid = true;
    FirebaseAuth fAuth; // firebase authentication
    FirebaseFirestore fStore; // to store data on firestone

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        fullName = findViewById(R.id.registerName);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        phone = findViewById(R.id.registerPhone);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.gotoLogin);
        isTeacherBox = findViewById(R.id.isTeacher);
        isStudentBox = findViewById(R.id.isStudent);

        // checkboxes logic
        // user cannot check both checkboxes
        isStudentBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (compoundButton.isChecked()) {
                    isTeacherBox.setChecked(false);
                }
            }
        });

        isTeacherBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (compoundButton.isChecked()) {
                    isStudentBox.setChecked(false);
                }
            }
        });


        // if data is valid, we can register the user
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkField(fullName);
                checkField(email);
                checkField(password);
                checkField(phone);

                // checkbox validation
                if (!(isStudentBox.isChecked() || isTeacherBox.isChecked())) {
                    Toast.makeText(Register.this, "Select the account type!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(valid) {
                    // start the registration process
                    fAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            FirebaseUser user = fAuth.getCurrentUser();

                            // message is triggered when validation is a success
                            Toast.makeText(Register.this, "Account Created", Toast.LENGTH_SHORT).show();

                            // extract fullname, phone etc. data
                            DocumentReference df = fStore.collection("Users").document(user.getUid());

                            // store the data
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("FullName", fullName.getText().toString());
                            userInfo.put("UserEmail", email.getText().toString());
                            userInfo.put("PhoneNumber", phone.getText().toString());

                            // access level
                            // specify if user is teacher or student
                            if (isTeacherBox.isChecked()) {
                                userInfo.put("isTeacher", "1");
                            }

                            if (isStudentBox.isChecked()) {
                                userInfo.put("isStudent", "1");
                            }


                            df.set(userInfo);

                            // When button is clicked, it should take user to the right activity
                            if (isTeacherBox.isChecked()) {
                                startActivity(new Intent(getApplicationContext(), Admin.class));
                                finish();
                            }

                            if (isStudentBox.isChecked()) {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }

                        }
                        // message is triggered if failed
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Register.this, "Failed to create an account", Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

    }

    public boolean checkField(EditText textField){
        if(textField.getText().toString().isEmpty()){
            textField.setError("Error");
            valid = false;
        }else {
            valid = true;
        }

        return valid;
    }
}