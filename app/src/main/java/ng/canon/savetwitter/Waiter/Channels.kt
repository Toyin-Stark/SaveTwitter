package ng.canon.savetwitter.Waiter

import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import ng.canon.savetwitter.Earth.Core

class Channels : Service() {
    val clipBox = ArrayList<String>()

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val currentText = clipboard.primaryClip.getItemAt(0).text.toString()
            if (currentText.contains("https://twitter.com")) {

                val id = currentText
                if (!clipBox.contains(id)) {


                    val dialogIntent = Intent(this, Core::class.java)
                    dialogIntent.putExtra("postID",id)
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent)
                }


            }


        }
        return super.onStartCommand(intent, flags, startId)

    }
}
