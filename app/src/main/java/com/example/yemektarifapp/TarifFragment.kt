package com.example.yemektarifapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_tarif.*
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
class TarifFragment : Fragment() {

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener {
            kaydet(it)
        }

        imageView.setOnClickListener {
            gorselSec(it)
        }

        arguments?.let {
            var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi

            if (gelenBilgi.equals("menudengeldim")) {
                // Yeni bir yemek eklemeye geldi
                yemekAdıText.setText("")
                yemekMalzemeleriText.setText("")
                button.visibility = View.VISIBLE

                val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.resim)
                imageView.setImageBitmap(gorselSecmeArkaPlani)

            } else {
                // Daha önce oluşturulan yemeği görmeye geldi

                // Aşağıdaki kod ise butonu gizler
                button.visibility = View.INVISIBLE

                val secileId = TarifFragmentArgs.fromBundle(it).id


                context?.let {

                    try {

                        val db = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secileId.toString()))

                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex = cursor.getColumnIndex("yemekmalzemesi")
                        val yemekGorsel = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){
                            yemekAdıText.setText(cursor.getString(yemekIsmiIndex))
                            yemekMalzemeleriText.setText(cursor.getString(yemekMalzemeIndex))

                            val byteDizisi = cursor.getBlob(yemekGorsel)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            imageView.setImageBitmap(bitmap)
                        }

                        cursor.close()

                    } catch (e : Exception) {
                        e.printStackTrace()
                    }
                }

            }
        }

    }



    fun kaydet(view : View) {
        // SQLite Kaydetme

        val yemekIsmi = yemekAdıText.text.toString()
        val yemekMalzemeleri = yemekMalzemeleriText.text.toString()

        if (yemekIsmi == ""){
            context?.let {
                Toast.makeText(it.applicationContext,"Yemek İsmi Girmediniz!",Toast.LENGTH_LONG).show()
            }

        }

        if (secilenBitmap != null) {
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,400)

            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {

                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB)")

                    // Aşağıdaki VALUES parantezi içinde bulunan yapıların indexi 1 den başlar!!
                    val sqlString = "INSERT INTO yemekler (yemekismi, yemekmalzemesi,gorsel) VALUES (?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,yemekIsmi)
                    statement.bindString(2,yemekMalzemeleri)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()

                }

            } catch (e : Exception) {
                e.printStackTrace()
            }

            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    fun gorselSec (view : View) {

        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmedi, izin istememiz gerekiyor
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            } else {
                // zaten izin verildi, tekrar sormadan direkt galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 1)

            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzni aldık
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            secilenGorsel = data.data

            try {

                context?.let {
                    if (Build.VERSION.SDK_INT >= 28) {

                        val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                        secilenBitmap = ImageDecoder.decodeBitmap(source)
                        imageView.setImageBitmap(secilenBitmap)
                    } else {
                        secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                        imageView.setImageBitmap(secilenBitmap)
                    }
                }


            } catch (e : Exception) {
                e.printStackTrace()
            }

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapOlustur(kullanicininSectigiBitmap : Bitmap, maximumBoyut : Int) : Bitmap {
        var width = kullanicininSectigiBitmap.width
        var heigth = kullanicininSectigiBitmap.height

        // Genişliği yüksekliğe bölüp 1 den büyük bir sayı bulursak görsel yataydır.
        // birden küçük ise görsel dikeydir.

        val bitmapOrani : Double = width.toDouble() / heigth.toDouble()

        if (bitmapOrani > 1) {
            // Görselimiz yataydır
            width = maximumBoyut
            val kisaltilmisHeigth = width / bitmapOrani
            heigth = kisaltilmisHeigth.toInt()
        } else {
            // görsel dikey
            heigth = maximumBoyut
            val kisaltilmisWidth = heigth * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }


        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,heigth,true)



    }

}