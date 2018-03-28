package ng.canon.savetwitter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.shashank.sony.fancygifdialoglib.FancyGifDialog
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener
import kotlinx.android.synthetic.main.activity_main.*
import ng.canon.savetwitter.Fragmented.Home
import ng.canon.savetwitter.Fragmented.PhotoBucket
import ng.canon.savetwitter.Waiter.Channels


import java.io.File


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startService(Intent(this@MainActivity, Channels::class.java))
        loadNavigation()
        looku()
    }




    fun loadNavigation(){


        val home = AHBottomNavigationItem(R.string.home_tab, R.drawable.ic_home,android.R.color.tertiary_text_light)
        val save = AHBottomNavigationItem(R.string.save_tab, R.drawable.ic_downloadx,android.R.color.tertiary_text_light)

        bottomNavigation.addItem(home)
        bottomNavigation.addItem(save)

        bottomNavigation.defaultBackgroundColor = ContextCompat.getColor(applicationContext, android.R.color.white)
        bottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        bottomNavigation.isForceTint = true
        bottomNavigation.accentColor = ContextCompat.getColor(applicationContext,R.color.colorPrimary)

        bottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        bottomNavigation.isForceTint = true


        val faces = Home()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frames, faces)
        transaction.addToBackStack(null)
        transaction.commit()


        bottomNavigation.setOnTabSelectedListener(object:AHBottomNavigation.OnTabSelectedListener{
            override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {


                if (position == 0){

                    val homes = Home()
                    val hometransaction = supportFragmentManager.beginTransaction()
                    hometransaction.replace(R.id.frames, homes)
                    hometransaction.addToBackStack(null)
                    hometransaction.commit()

                }

                if (position == 1){

                    val photopale = PhotoBucket()
                    val phototransaction = supportFragmentManager.beginTransaction()
                    phototransaction.replace(R.id.frames, photopale)
                    phototransaction.addToBackStack(null)
                    phototransaction.commit()

                }


                return true
            }


        })

    }




    fun looku(){


        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {




        }else{
            runOnUiThread {

                lasma()

            }
        }

    }



    fun checkVoid(){
        val file = File(Environment.getExternalStorageDirectory().absolutePath, "facesave")
        if (!file.exists())
            file.mkdirs()
        if (file.isDirectory) {
            val files = file.list()
            if (files.isEmpty()) {
                val homes = Home()
                val hometransaction = supportFragmentManager.beginTransaction()
                hometransaction.replace(R.id.frames, homes)
                hometransaction.addToBackStack(null)
                hometransaction.commit()

            }
        }
    }











    fun lasma(){
        val request = permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE).build()
        request.send()
        request.listeners {

            onAccepted { permissions ->

                looku()

            }

            onDenied { permissions ->

                permissionDialog()
            }

            onPermanentlyDenied { permissions ->
                permissionDialog()

            }

            onShouldShowRationale { permissions, nonce ->
                permissionDialog()

            }
        }
        // load permission methods here
    }












    fun permissionDialog(){


        runOnUiThread {
            FancyGifDialog.Builder(this@MainActivity)
                    .setTitle(getString(R.string.permissionTitle))
                    .setMessage(getString(R.string.permissionMessage))
                    .setNegativeBtnText(getString(R.string.permissionNegative))
                    .setPositiveBtnBackground("#FF4081")
                    .setPositiveBtnText(getString(R.string.permissionPositive))
                    .setNegativeBtnBackground("#FFA9A7A8")
                    .setGifResource(R.drawable.permit)   //Pass your Gif here
                    .isCancellable(false)
                    .OnPositiveClicked(object : FancyGifDialogListener {
                        override fun OnClick() {

                            lasma()


                        }


                    })

                    .OnNegativeClicked(object : FancyGifDialogListener {
                        override fun OnClick() {

                            Toast.makeText(this@MainActivity,""+getString(R.string.permissionMessage), Toast.LENGTH_LONG).show()
                            finish()

                        }


                    })
                    .build()
        }


    }
}
