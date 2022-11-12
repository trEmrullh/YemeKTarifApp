package com.example.yemektarifapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Aşağıdaki fonksiyon xml deki menu klasörünü kodlarla görünüme bağlar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.yemek_ekleme,menu)

        return super.onCreateOptionsMenu(menu)
    }


    // OptionsMenu den bir şey seçilirse ne yapayım sorusunu sorar ve actionu çalıştırır.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // navigation_graph te yaptığımız navigasyon actionunu burada sağlaması yapılır
        // if kod bloğu ile çalıştırılır

        // Aşağıda item adı R.id.yemek_ekleme_id mi diye kontrol ettik. Eğer bu id doğru değilse bu işlem yapılmayacaktır

        if (item.itemId == R.id.yemek_ekleme_item) {
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("menudengeldim",0)
            Navigation.findNavController(this,R.id.fragment).navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }
}