package ng.canon.savetwitter.Fragmented


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.photo_bucket.view.*
import kotlinx.android.synthetic.main.save_row.view.*
import ng.canon.savetwitter.GlideApp
import ng.canon.savetwitter.R
import java.io.File
import java.util.*


class PhotoBucket : Fragment(),SwipeRefreshLayout.OnRefreshListener {


    private var observable: Observable<String>? = null
    private var adapter: Photo_Adapter? = null
    private var arrayList: ArrayList<Photo_Model>? = null
    private var urls: ArrayList<String>? = null
    var swipes:SwipeRefreshLayout? = null
    var recyclerview:RecyclerView? = null


    private var path: String? = null
    internal var cur: Cursor? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
       val v = inflater.inflate(R.layout.photo_bucket, container, false)

        swipes = v.swipes
        recyclerview = v.recyclerview

        swipes!!.setColorSchemeColors(Color.GRAY, Color.GREEN, Color.BLUE,
                Color.RED, Color.CYAN)
        swipes!!.setDistanceToTriggerSync(20)// in dips
        swipes!!.setSize(SwipeRefreshLayout.DEFAULT)// LARGE also can be used
        swipes!!.setOnRefreshListener(this)

        recyclerview!!.setHasFixedSize(true)
        recyclerview!!.isNestedScrollingEnabled = true
        recyclerview!!.layoutManager = GridLayoutManager(activity!!.applicationContext,2)



        v.fabe.setOnClickListener {
            kmate()
        }

        kmate()

        return v
    }


    override fun onRefresh() {

        kmate()
    }


    // Method to load files from download folder if write_to_external storage is granted
    fun kmate(){

        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            FileQuery()

        }else{

            //Load Write permissions here
            lasma()
        }

    }

    fun FileQuery() {
        swipes!!.isRefreshing = true
        arrayList = ArrayList<Photo_Model>()
        arrayList!!.clear()
        path = Environment.getExternalStorageDirectory().absolutePath + "/savetwitter/"
        val dex = File(Environment.getExternalStorageDirectory().absolutePath, "savetwitter")
        if (!dex.exists())
            dex.mkdirs()


        observable = Observable.create(object: ObservableOnSubscribe<String> {
            override fun subscribe(subscriber: ObservableEmitter<String>) {


                val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Files.FileColumns.DATE_ADDED, MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.SIZE, MediaStore.Video.Thumbnails.DATA)


                val queryUri = MediaStore.Files.getContentUri("external")


                cur = activity!!.contentResolver.query(queryUri, projection, MediaStore.Files.FileColumns.DATA + " LIKE ? AND " + MediaStore.Files.FileColumns.DATA + " NOT LIKE ?", arrayOf(path + "%", path + "%/%"), MediaStore.Files.FileColumns.DATE_ADDED + " desc")

                var data: String
                var name: String
                var mime: String?
                var id: String
                var type: String
                var time: String
                var url: String
                var size: String

                if (cur != null) {

                    if (cur!!.moveToFirst()) {

                        val dataColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                        val nameColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        val mimeColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                        val idColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns._ID)
                        val timeColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
                        val typeColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                        val sizeColumn = cur!!.getColumnIndex(MediaStore.Files.FileColumns.SIZE)


                        do {

                            data = cur!!.getString(dataColumn)
                            name = cur!!.getString(nameColumn)
                            mime = cur!!.getString(mimeColumn)
                            id = cur!!.getString(idColumn)
                            time = cur!!.getString(timeColumn)
                            type = cur!!.getString(typeColumn)
                            size = cur!!.getString(sizeColumn)
                            val date = Date(cur!!.getLong(cur!!.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000)

                            val big = java.lang.Long.parseLong(size)
                            size = Formatter.formatFileSize(activity!!.applicationContext, big)

                            if (mime != null && mime.contains("video")) {

                                val uri = Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString() + "/" + id)
                                url = uri.toString()


                            }

                            if (mime != null && mime.contains("audio")) {

                                val uri = Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + id)
                                url = uri.toString()


                            }


                            else {

                                url = data
                            }

                            val milliSeconds = date.time
                            arrayList!!.add(Photo_Model(name, mime, url, milliSeconds, data, size))

                        } while (cur!!.moveToNext())


                    } else {

                    }
                }

                cur!!.close()
                subscriber.onNext("")
                subscriber.onComplete()

            }
        })


        observable!!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Observer<String> {
                    override fun onComplete() {

                        adapter = Photo_Adapter(activity!!.applicationContext, arrayList!!, this@PhotoBucket)

                        if (adapter == null) {


                        } else {
                            swipes!!.isRefreshing = false
                            recyclerview!!.adapter = adapter// set adapter on recyclerview
                            adapter!!.notifyDataSetChanged()


                        }
                    }

                    override fun onSubscribe(d: Disposable) {

                    }


                    override fun onError(e: Throwable) {

                        Toast.makeText(activity!!.applicationContext, "" + e, Toast.LENGTH_LONG).show()


                    }

                    override fun onNext(response: String) {


                    }


                })


    }






    fun lasma(){
        val request = permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE).build()
        request.send()
        request.listeners {

            onAccepted { permissions ->

                kmate()
            }

            onDenied { permissions ->
                lasma()
            }

            onPermanentlyDenied { permissions ->
                // Notified when the permissions are permanently denied.
            }

            onShouldShowRationale { permissions, nonce ->
                // Notified when the permissions should show a rationale.
                // The nonce can be used to request the permissions again.
            }
        }
        // load permission methods here
    }
}






data class Photo_Model(var title:String,var desc:String,var image:String,var time:Long,var link:String,var size:String)

class Photo_Adapter(var context: Context, var arraylists: ArrayList<Photo_Model>, var downloads: PhotoBucket) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as Item).bindData(arraylists[position],downloads)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var v = LayoutInflater.from(context).inflate(R.layout.save_row, parent, false)
        return Item(v)

    }



    override fun getItemCount(): Int {
        return arraylists.size
    }



    class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(_data: Photo_Model, _cube: PhotoBucket) {

            val extension = _data.title.substring(_data.title.lastIndexOf(".") + 1)
            val ext = _data.desc;
            var urx = ""
            itemView.playImg.visibility = View.VISIBLE

            if (ext.contains("video")){
                urx = _data.image
                itemView.playImg.visibility = View.VISIBLE

            }

            if(ext.contains("image")){

                urx = "file://"+_data.link;
                itemView.playImg.visibility = View.GONE

            }





            if (ext.contains("audio")){


            }else{

                if (extension.contains("gif")){
                   GlideApp.with(itemView.context).asGif().diskCacheStrategy(DiskCacheStrategy.ALL).load(urx).into(itemView.cover);

                }else{

                    GlideApp.with(itemView.context)
                            .asBitmap()
                            .load(urx)
                            .into(object : SimpleTarget<Bitmap>(){
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                                    itemView.cover.setImageBitmap(resource)

                                }


                            })

                }



            }







            itemView.setOnClickListener {

                if ( ext.contains("video"))
                {

                    MediaScannerConnection.scanFile(itemView.context, arrayOf(_data.link), null) { path, uri ->

                        val untent = Intent(Intent.ACTION_SEND)
                        untent.putExtra(Intent.EXTRA_STREAM,uri)
                        untent.type = "video/*"
                        untent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        itemView.context.startActivity(untent)
                    }


                }

                if ( ext.contains("image")) {
                    MediaScannerConnection.scanFile(itemView.context, arrayOf(_data.link), null) { path, uri ->
                        val untent = Intent(Intent.ACTION_VIEW)
                        untent.setDataAndType(uri, "image/*")
                        itemView.context.startActivity(untent)
                    }



                }



            }
        }
    }





}


