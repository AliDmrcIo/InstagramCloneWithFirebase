package com.ali.instagramclonejava.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ali.instagramclonejava.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);


        auth = FirebaseAuth.getInstance();

        FirebaseUser user=auth.getCurrentUser();

        if(user!=null){
            Intent intent=new Intent(MainActivity.this,FeedActivity.class);
            startActivity(intent);
            finish();

        }

    }





    public void signInClicked(View view){

        String email=binding.emailText.getText().toString();
        String password=binding.passwordText.getText().toString();

        if(email.equals("") || password.equals("")){

            Toast.makeText(this, "Lutfen email ve sifre giriniz!", Toast.LENGTH_SHORT).show();

        }
        else{

            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>()
            {
                @Override
                public void onSuccess(AuthResult authResult) {

                    Intent intent=new Intent(MainActivity.this,FeedActivity.class);
                    startActivity(intent);
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
}

    public void signUpClicked(View view){

        String email=binding.emailText.getText().toString();
        String password=binding.passwordText.getText().toString();

        if((email.equals("") || password.equals(""))){
            Toast.makeText(MainActivity.this, "Lutfen Email ve Sifreyi bos birakmayiniz!", Toast.LENGTH_LONG).show();
        }

        else{

            auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {//email ve şifre ile bir kullanıcı yarat dedik. İçerisinde de bizden bir email ve şifre istedi. bizde binding. ile kullanıcının gireceği email ve şifreyi buraya verdik. auth.createUserWithEmailAndPassword(email,password) burası tamamlandıktan sonra tekrar . dedik ve bize sunucunun bize döndüreceği cevaplara göre aksiyon alabileceğimiz listenerlar çıktı. biz de sunucuya gönderilen cevap başarılı olursa ne yapacağız(.addOnSuccessListener(new OnSuccessListener) ve başarısız olursa ne yapacağız durumlarındaki listener'ları ekledik. Aşağısını kendi doldurdu yani ben bir şey yapmadım

                @Override
                public void onSuccess(AuthResult authResult) {

                    Intent intent=new Intent(MainActivity.this,FeedActivity.class);
                    startActivity(intent);
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(MainActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }


    }


}
