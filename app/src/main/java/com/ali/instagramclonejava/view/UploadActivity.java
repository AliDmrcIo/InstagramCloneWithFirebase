package com.ali.instagramclonejava.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.ali.instagramclonejava.databinding.ActivityUploadBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private FirebaseStorage firebaseStorage;//Büyük verileri(foto gibi) saklayacağımız storage alanı. depolama için kullanılan kod, heryerden erişeyim diye class'ın hemen altına ekledim ama onCreate altında da initialize etmem gerek
    private FirebaseAuth auth; //authentication yapmak için zaten MainActivity de de kullandığımız şey
    private FirebaseFirestore firebaseFirestore; //küçük verileri saklamak, storage'a yüklediğimiz fotonun linkini vereceğimiz firestore kısmı.
    private StorageReference storageReference;


    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Uri imageData;
    private ActivityUploadBinding binding;

    //Şimdi şöyle ki. Firebase'de firestore database altında cloud firestore ve onun da altında data bölümü var. Burası No SQL mantığıyla çalışan, sütunlar ve satırlardan oluşmayan bir databse'dir. Burada biz sadece değişken gibi(String, int, boolean, date) verileri saklayabiliyoruz. Burada koleksiyonlar var ve onların da altında dökümanlar var. Bu dökümanlarda biz değişken adını, veritipini ve değerini verdikten sonra bunları database de kaydedebiliyoruz. Buradaki verilerde resim, video vs gibi büyük alana ihtiyaç duyan veriler tutamıyoruz. Max 1 mb olan verileri koyabiliyoruz. 1 mb tan büyük resim gibi, video gibi verileri Firebase'de storage diye bir kısım var, orada tutacağız. Bu firestore database'de resme dair max yapabileceğimiz şey; storage'da saklanan resmin url'sini gireriz, o şekilde bir veritipi saklarız.
    //Yani biz bu storage denen kısma foto vs yüklicez, sonra oradaki fotonun linkini gidip firestore database'e vericez ve her cihazdan buna erişebilir hale gelicez.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUploadBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        firebaseStorage=FirebaseStorage.getInstance();//onCreate altında bunları initialize etmem gerek
        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference=firebaseStorage.getReference();//firebase'den verileri aldığımızı söylemiştik zaten

        registerLauncher();//çağırmazsak çöker knk
    }

    public void uploadButtonClicked(View view){

        if(imageData!=null){//storage'a fotoyu URI kullanarak upload edicem. Kullanıcı fotoyu seçtiyse storage'a yükleme işlemini yap
            //bunu aldıktan sonra referans sitemi denen bir şey var. referans sistemiyle bunu çalıştırabiliyoruz.
            //bu referans aslında storage'da neyi nereye koyacağız, hangi sırayla koyacağız vs bunları düzenleyen birşey. Örneğin ben sotage'da bir dosya oluşturacağım, onun içerisine de bir dosya oluşturacağım ve oraya da fotoğrafımı koyacağım diye düzenlemeler yapmamıza yardımcı olan bir obje

            UUID uuid=UUID.randomUUID();//(NOT: 2 SATIR AŞAĞIDAKİ KODU YAZDIKTAN SONRA BUNA İHTİYAÇ OLDU. ÖNCE ORAYA BAK SONRA BURAYA GEL)şimdi bu 2 satır aşağıdaki storageReference.child("images/image.jpg") koddan sonra şöyle bir sorun oluştu: Burada kullanıcı seçtiği fotoyu yüklüyor ve sonrasında foto firebase storage altında images adlı klasör içerisindeki images.jpg adlı bir dosyaya giriyor ancak sonrasında başka bir foto yükledi mi kullanıcı tekar o fotoya gidiyor yapışıyor bir önceki girdiği foto silinmiş oluyor. O yüzden biz kullanıcı her yeni foto girdiğinde yeni adla bir dosya açmamız gerekecek image1.jpg, image2.jpg gibi. Bu sorunu çözmek için de UUID(Universal Unique Id) denen bir şey kullanacağız. Bu da bize uydurma random isimler oluşturacak
            String imageName="imags/"+uuid+".jpg";


            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {//storage sayfasında bir klasör oluştur demektir .child(). Biz burada images diye bir klasör oluştur, onun altına da image.jpg diye bir dosya oluştur dedik. .child().child() diye de istediğimiz kadar klasör oluşturabiliriz alt alta. İşimiz bittikten sonra da .putFile() diyip oraya ne koyacağımızı içerisine veri olarak veririz. sonrasında oluşturduğumuz .addOnSuccessListener ve .addOnFailureListener lar ise yine asenkron çalışmamızı(kullanıcının arayüzüne bulaşmama ve uygulamayı çökertmemek adına) sağlaması için yazdık.
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {//burada firebase'de storage içerisindeki resmi firestore database'e vericez

                    //Download URL 'i alacağız. Tabi bunu yaparken aynı storage'a dosya açarkenki vs gibi referansla yapcaz bunu

                    StorageReference newReferance=firebaseStorage.getReference(imageName);//storage içreisinde bu isimli fotoyu al ve firestore database'e koy
                    newReferance.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() { //download url'i al
                        @Override
                        public void onSuccess(Uri uri) {

                            String downloadUrl=uri.toString();//bize verilen bu URI'ı stirnge çevirdik
                            String comment=binding.commentText.getText().toString();

                            FirebaseUser user=auth.getCurrentUser();//hangi kullanıcı bu yorumu yapmış onu al

                            String email=user.getEmail();//kullanıcının emailini al

                            //firestore database'e bu bilgileri kaydederken orada bilgiler anahtar kelime şeklinde depolanıyor. Bu da demek oluyor ki pythondan bildiğim dictionary, java da hashmap olan veritipiyle bu işi çözmemiz gerekecek

                            HashMap<String, Object> postData=new HashMap<>();//firestore database'e verileri hashmap olarak koyabildiğimizden dolayı burayı açtım, Anahtarlar String, Keyler object veritipinde olsun dedim. Object: String te olabilir, integer da olabilir, tarihte olabilir demektir. Firestore'da hocanın açtığı sayfayı hatırla. önce verinin ne olacağını tanımlıyorduk(email olsun, şifre olsun vsvs) sonra da değişken tipini söylüyorduk(integer olsun string olsun date olsun. Yani sağ taraafta ne olacağı belli olmaz o yüzden Object olsun, ne yollarsam onu al dedik)
                            postData.put("useremail",email);//hashmap'e kullanıcının girdiği değerleri verdik."" ile yazdığım key, virgülden sonrası ise value
                            postData.put("downloadurl",downloadUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp());//firebase'den güncel tarihi aldık


                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() { //oluşturduğumuz hashMap'i firestore'a koyduk
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    Intent intent=new Intent(UploadActivity.this,FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(UploadActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }



    }

    public void imageClicked(View view){

        if(ContextCompat.checkSelfPermission(UploadActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(UploadActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                Snackbar.make(view,"Izin almam lazim canim",Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);//izin iste


                    }
                }).show();



            }else{

                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            }


        }else{

            Intent intentToGallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//izni aldıysak galeriye eriş
            activityResultLauncher.launch(intentToGallery);

        }

    }

    private void registerLauncher(){//bu en baştaki result launcher'ların onClick altında register edilebilmesi için yazdığımız registration methodu burası. Eğer onları onClick altında initialize etmezsek uygulamayı çökertiyordu

        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if(result.getResultCode()==RESULT_OK){//galeriye düzgün şekilde gidebildiysek, her şey okeyse, sorun yoksa
                    Intent intentFromResult=result.getData();

                    if(intentFromResult!=null){//eğer seçilecek foto oradaysa, seçilmeye hazır beklenen bir foto varsa

                        imageData=intentFromResult.getData();//bu bize URI tipinde veri döndürüyor, burada aslında galerideki seçilen fotonun adresini aldık gibi bişe oldu. Firebase bizden uri tipinde istiyor aslında bu yeterli de, kullanıcıya da göstermemiz lazım
                        binding.imageView.setImageURI(imageData);


                    }

                }
                else{

                }

            }
        });

        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result){

                    Intent intentToGallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                }else{

                    Toast.makeText(UploadActivity.this, "Izin ver", Toast.LENGTH_SHORT).show();


                }

            }
        });

    }



}