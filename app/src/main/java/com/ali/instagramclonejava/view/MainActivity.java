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
    private FirebaseAuth auth;//firebase Authentication sınıfını kullanılmak üzere hazırda beklettim

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);


        auth = FirebaseAuth.getInstance();//firebase'i initialize ettik. objeyi oluşturduk. Artık bunu kullanarak giriş çıkış işlemlerinin hepsini yapabilirim.

        FirebaseUser user=auth.getCurrentUser();//daha önceden giriş yapmış kullanıcı varsa onu bana verir ama kesin değil . Nullable diyoruz buna, olabilir olmayadabilir o yüzden şimdi if açıp eğer giriş yapan varsa tekrar ona email şifre kısmını gösterme dicez

        if(user!=null){//eğer halihazırda uygulamada giriş yapmış kullanıcı varsa
            Intent intent=new Intent(MainActivity.this,FeedActivity.class);
            startActivity(intent);
            finish();

        }

        //Normalde büyük bir firmada ekipler olur. Bu ekiplerde backendcisi olur, dizayncısı, database cisi vs olur. Firebase bize bunları tek kişiyle yapabilme olanağı sunuyor. Firebase aslında verileri internette tutup kullanıcının giriş bilgilerine girdikten sonra verilerine erişebileceği bir hizmet sunar. Firebase çok büyük bir backend servisidir gibi düşünebiliriz. İçerisinde veritabanı hizmeti, depolama, kullanıcı oluşturma, parolaları saklama, parolaları oluşturma, hesaba giriş yapma. Bunların hepsini bize sağlayan bir Google servisidir. İlk başta ücretsiz bir hizmettir ancak uygulama belli bir popülerliğe ulaştığı zaman kullandığın kadar öde mantığıyla çalışmaya başlıyor. Firebasein hizmetlerinde analizler gibi kısımlar da var, kullanıcılar hangi ülkelerden, cinsiyet dağılımları ne vs gibi. Firebase hiç kullanmasa bile bir firma, analizler için kullanabiliyormuş. Firebase; Android, Ios, web, unity, vb platformlarda çalışabiliyor
        //https://console.firebase.google.com/project/instaclonejava-5df97/overview buraya gidip paket ismini yapıştırıp bize sunduğu dosyayı indirmemiz ve burada androidden çıkıp project'e  tıklayıp app içerisine o dosyayı atmamız gerekiyor.


    }





    public void signInClicked(View view){//giriş yap

        String email=binding.emailText.getText().toString();
        String password=binding.passwordText.getText().toString();

        if(email.equals("") || password.equals("")){

            Toast.makeText(this, "Lutfen email ve sifre giriniz!", Toast.LENGTH_SHORT).show();

        }
        else{

            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() //email ve parolayla giriş yap. Aşağıdaki auth.createUser.... kısmıyla aynı zaten listener falan olarakta. Bilgi almak için aşağı bak
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

    public void signUpClicked(View view){//kayıt ol

        String email=binding.emailText.getText().toString();
        String password=binding.passwordText.getText().toString();

        if((email.equals("") || password.equals(""))){//kullanıcı eğer email veya şifreden herhangi birini boş bıraktıysa ona toast mesajı gönderiyoruz.
            Toast.makeText(MainActivity.this, "Lutfen Email ve Sifreyi bos birakmayiniz!", Toast.LENGTH_LONG).show();
        }

        else{//email ve şifre kısmını kullanıcının boş bırakmadığından emin olmak istiyoruz.

            auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {//email ve şifre ile bir kullanıcı yarat dedik. İçerisinde de bizden bir email ve şifre istedi. bizde binding. ile kullanıcının gireceği email ve şifreyi buraya verdik. auth.createUserWithEmailAndPassword(email,password) burası tamamlandıktan sonra tekrar . dedik ve bize sunucunun bize döndüreceği cevaplara göre aksiyon alabileceğimiz listenerlar çıktı. biz de sunucuya gönderilen cevap başarılı olursa ne yapacağız(.addOnSuccessListener(new OnSuccessListener) ve başarısız olursa ne yapacağız durumlarındaki listener'ları ekledik. Aşağısını kendi doldurdu yani ben bir şey yapmadım

                @Override
                public void onSuccess(AuthResult authResult) {//sunucudan başarılı bir cevap gelirse ne yapacağımız kısmı. Bu method biz server'dan cevabı aldığımızda çalışıyor ve aynı zamanda addOnSuccessListener'da asenkron çalıştığından kullanıcının arayüzüne bir şey yapmış olmuyor.

                    Intent intent=new Intent(MainActivity.this,FeedActivity.class);
                    startActivity(intent);
                    finish();//bu aktiviteyle işim bitince burayı kapatacağım ve bir daha buraya geri dönmeye gerek yok. Kullanıcı giriş yaptıktan sonra bu aktiviteyi kapatayım ki boşu boşuna hafızada yer kaplamasın mesela.

                }
            }).addOnFailureListener(new OnFailureListener() {//eğer firebase sunucusuna kayıt için yollanılan şeye dönüş başarısız yönünde olursa ne yapıcaz kısmı
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(MainActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();//e.getLocalizedMessage() demek kullanıcının anlayacağı dilden bir mesaj yaz demektir.
                }
            });


        }


    }


}