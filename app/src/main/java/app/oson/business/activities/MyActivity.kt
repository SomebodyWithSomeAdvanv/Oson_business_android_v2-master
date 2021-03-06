package app.oson.business.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.view.View
import android.widget.ProgressBar
import app.oson.business.R
import app.oson.business.api.Api
import app.oson.business.api.callbacks.BaseCallback
import app.oson.business.database.Preferences
import kotlinx.android.synthetic.main.action_bar.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

abstract class MyActivity : AppCompatActivity(), View.OnClickListener {

    internal lateinit var backImageView: AppCompatImageView;
    internal lateinit var filterImageView: AppCompatImageView;
    internal lateinit var infoImageView: AppCompatImageView;
    internal lateinit var titleTextView: AppCompatTextView
    internal lateinit var clearImageView: AppCompatImageView

    lateinit var retrofit: Retrofit
    lateinit var api: Api

    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = Preferences(applicationContext)
    }

    override fun setContentView(layoutResID: Int){
        super.setContentView(layoutResID)


        initActionBar()

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val builder = chain.request().newBuilder()

                    if (preferences.getUserData() != null)
                        builder.addHeader("token", preferences.getUserData()!!.token)

                    var request = builder.build()

                    var response = chain.proceed(request)


//                    if (response.code() == 401 && preferences.getLoginData() != null) {
//                        val r = api!!.refreshToken("refresh_token", Api.CLIENT_ID, Api.CLIENT_SECRET, preferences.getUserData()!!.token).execute()
//
//                        if (r.code() == 200 && r.body() != null) {
//                            preferences.saveLoginData(r.body()!!)
//
//
//                            builder.removeHeader("authorization")
//                            builder.addHeader("authorization", "Bearer " + preferences.getLoginData()!!.accessToken)
//
//                            request = builder.build()
//
//                            response = chain.proceed(request)
//                        }
//                    }


                    return response
                }
            })
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()


        retrofit = Retrofit.Builder()
            .baseUrl(Api.API_HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        api = retrofit.create(Api::class.java!!)
    }


    fun initActionBar() {

        backImageView = findViewById(R.id.image_view_back)
        filterImageView = findViewById(R.id.image_view_filter)
        infoImageView = findViewById(R.id.image_view_info)
        titleTextView = findViewById(R.id.text_view_title)
        clearImageView = findViewById(R.id.image_view_clear)

        backImageView.setOnClickListener() { view ->
            finish()
        }
        filterImageView.setOnClickListener(this)
        infoImageView.setOnClickListener(this)
        clearImageView.setOnClickListener(this)

        setupActionBar()
    }

    abstract fun setupActionBar();

    @SuppressLint("MissingPermission")
    fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting

    }

    var progressDialog: Dialog? = null;
    fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = Dialog(this);
            progressDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog?.setCancelable(false)
            progressDialog?.setContentView(ProgressBar(this))
        }

        progressDialog?.show();
    }

    fun cancelProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing)
            progressDialog?.cancel()
    }

    fun showAlertDialog(title: String, message: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("ОК",
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()
    }

}