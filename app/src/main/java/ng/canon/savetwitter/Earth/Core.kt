package ng.canon.savetwitter.Earth

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import com.downloader.*
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.shashank.sony.fancygifdialoglib.FancyGifDialog
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.core.*
import kotlinx.android.synthetic.main.jars.*
import ng.canon.savetwitter.R
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class Core : AppCompatActivity() {
    var observable: Observable<String>? = null
    var postID = ""
    var finalUrl =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.core)
        postID = intent.getStringExtra("postID")

        val config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build()
        PRDownloader.initialize(applicationContext, config)

        looku()
    }







    fun looku(){


        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            cardy.visibility = View.VISIBLE
            progressx.isIndeterminate = true
            linkCore(postID)


        }else{
            runOnUiThread {

                lasma()

            }
        }

    }



    fun linkCore(videoID: String) {


        observable = Observable.create(object : ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                try {

                    val mu = pullLinks(videoID)
                   val realUrl = mu[0]
                    val instaUrl = "https://www.saveitoffline.com/process/?url=$realUrl"
                    val respond = SaveDit(instaUrl)
                    val json = JSONObject(respond)
                    val arrays = json.getJSONArray("urls")
                    var links = arrays.getJSONObject(0).getString("id")
                    finalUrl = links


                    subscriber.onNext(links)

                } catch (e: Exception) {

                    subscriber.onError(e)
                }


                subscriber.onComplete()
            }
        })

        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onSubscribe(d: Disposable) {


                    }

                    override fun onComplete() {


                        val toast = getString(R.string.starts)
                        mrSave(finalUrl)
                        Toast.makeText(this@Core,""+toast,Toast.LENGTH_LONG).show()



                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(applicationContext, ""+getString(R.string.sorry), Toast.LENGTH_LONG).show()
                        finish()

                    }

                    override fun onNext(response: String) {

                    }
                })

    }



    fun SaveDit(link: String): String {

        var pink = ""
        val saveclient = OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build()
        val saverequest = Request.Builder()
                .url(link)
                .build()
        val response = saveclient.newCall(saverequest).execute()


        val json = JSONObject(response.body()!!.string())


        return json.toString()
    }





    fun mrSave(urld:String) {

        progressx.isIndeterminate = false
        progressx.progress = 0
        var extension = "mp4"
        var desc = getString(R.string.bannerTitle)
        val timeStamp = System.currentTimeMillis()
        val name = "insta_$timeStamp.$extension"
        val dex = File(Environment.getExternalStorageDirectory().absolutePath, "savetwitter")
        if (!dex.exists())
            dex.mkdirs()

        val filed = File(dex, name)


        val downloadId = PRDownloader.download(urld, dex.absolutePath, name)
                .build()
                .setOnProgressListener(object : OnProgressListener {
                    override fun onProgress(progress: Progress?) {
                        val progressPercent = progress!!.currentBytes * 100 / progress.totalBytes
                        val percents = progressPercent.toInt()
                        val papi = percents.toString()
                        status.text = "${papi}%"
                        progressx.progress = percents


                    }


                }).start(object : OnDownloadListener {
            override fun onError(error: Error?) {


            }

            override fun onDownloadComplete() {

                MediaScannerConnection.scanFile(applicationContext, arrayOf(filed.absolutePath), null) { path, uri ->

                    finish()

                }


            }


        })


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
            FancyGifDialog.Builder(this@Core)
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

                            Toast.makeText(this@Core,""+getString(R.string.permissionMessage),Toast.LENGTH_LONG).show()
                            finish()

                        }


                    })
                    .build()
        }


    }






    // EXTRACT LINKS FROM STRINGS
    fun pullLinks(text: String): ArrayList<String> {
        val links = ArrayList<String>()
        //String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        val regex = "\\(?\\b(https?://|www[.]|ftp://)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]"

        val p = Pattern.compile(regex)
        val m = p.matcher(text)

        while (m.find()) {
            var urlStr = m.group()

            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length - 1)
            }

            links.add(urlStr)
        }

        return links
    }


}
