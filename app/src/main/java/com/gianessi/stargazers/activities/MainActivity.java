package com.gianessi.stargazers.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.gianessi.stargazers.R;
import com.gianessi.stargazers.models.User;

public class MainActivity extends AppCompatActivity {

    private static final int USERS_LIST_REQ = 34;

    private EditText userEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userEdit = findViewById(R.id.main_user_edit);
        userEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseUser();
            }
        });
    }

    private void chooseUser(){
        Intent intent = new Intent(this, UsersListActivity.class);
        startActivityForResult(intent, USERS_LIST_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USERS_LIST_REQ) {
            if (resultCode == RESULT_OK && data.hasExtra(User.USERNAME)) {
                this.userEdit.setText(data.getStringExtra(User.USERNAME));
            }
        }
    }
}
