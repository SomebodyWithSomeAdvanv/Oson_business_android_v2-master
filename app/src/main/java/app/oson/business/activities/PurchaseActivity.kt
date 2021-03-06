package app.oson.business.activities

import android.os.Bundle
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatEditText
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import app.oson.business.App.Companion.context
import app.oson.business.R
import app.oson.business.api.callbacks.BaseCallback
import app.oson.business.api.services.MerchantService
import app.oson.business.api.services.PurchaseService
import app.oson.business.models.Merchant
import app.oson.business.models.PurchaseTransaction
import app.oson.business.views.FieldView
import app.oson.business.views.FieldsLinearLayout

class PurchaseActivity : MyActivity() {

    lateinit var spinner: Spinner
    lateinit var cardNumberEditText: AppCompatEditText
    lateinit var cardExpireEditText: AppCompatEditText
    lateinit var amountEditText: AppCompatEditText
    lateinit var sendButton: AppCompatButton
    lateinit var linearLayout: FieldsLinearLayout


    var merchantList: ArrayList<Merchant>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
        titleTextView.setText(R.string.menu_item_bottomnavigationview_purchase_title)

        merchantList = intent.getSerializableExtra("merchant") as? ArrayList<Merchant>

        initViews()

        val arrayList = ArrayList<String>()
        for (i in merchantList!!.indices) {
            arrayList.add(merchantList!![i].name)
        }

        val spinnerAdapter = ArrayAdapter(this@PurchaseActivity, android.R.layout.simple_spinner_item, arrayList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        spinner!!.adapter = spinnerAdapter

        sendButton.setOnClickListener(this)

        getMerchantWithFields()
    }

    override fun setupActionBar() {
        backImageView.visibility = View.VISIBLE
        titleTextView.visibility = View.VISIBLE

    }

    override fun onClick(v: View?) {
        if (v == backImageView) {
            finish()
        } else if (v == sendButton) {
            putPurchase()
        }

    }

    fun initViews() {
        spinner = findViewById(R.id.spinner)
        cardNumberEditText = findViewById(R.id.edit_text_card_number)
        cardExpireEditText = findViewById(R.id.edit_text_card_expire)
        amountEditText = findViewById(R.id.edit_text_amount)
        sendButton = findViewById(R.id.button_send_purchase)
        linearLayout = findViewById(R.id.linear_layout)
    }

    fun checkPurchaseData(): Boolean {
        if (merchantList == null) {
            showAlertDialog("Error" , "NULLLLL")
            return false
        }

        if (cardNumberEditText.text.toString().length != 16) {
            showAlertDialog("Error" , "Kartani to'liq kiriting")
            return false
        }

        if (cardExpireEditText.text.toString().length != 4) {
            showAlertDialog("Error" , "Karta amal qilish muddatini kiriting")
            return false
        }

        if (amountEditText.text.toString().isEmpty()) {
            showAlertDialog("Error" , "Summani kiriting!")
            return false
        }

        return true
    }

    fun getFields() {
        if (merchantWithFields != null) {
            linearLayout.buildWithFields(merchantWithFields!![0].fields())
        }
    }

    var merchantId: Long? = null
    lateinit var fields: String
    lateinit var cardNumber: String
    lateinit var cardExpireData: String
    var amount: Long? = null

    fun putPurchase() {
        if (checkPurchaseData()) {
            if (linearLayout.getPurchaseData()!! != null) {
                merchantId = merchantList!!.get(spinner.selectedItemPosition).id;
                fields = linearLayout.getPurchaseData()!!
                cardNumber = cardNumberEditText.text.toString()
                cardExpireData = cardExpireEditText.text.toString()
                amount = amountEditText.text.toString().toLong()

                PurchaseService().putPublicPurchase(
                    merchantId = merchantId,
                    fields = fields,
                    cardNumber = cardNumber,
                    cardExpireDate = cardExpireData,
                    amount = amount!! * 100,
                    callback = object : BaseCallback<PurchaseTransaction> {
                        override fun onLoading() {

                        }

                        override fun onError(throwable: Throwable) {

                        }

                        override fun onSuccess(response: PurchaseTransaction) {

                        }

                    })
            } else {
                showAlertDialog("Error" , "Malumotlarni kiritishda xatolik bo'ldi")
            }
        }
    }

    var merchantWithFields: ArrayList<Merchant>? = null

    fun getMerchantWithFields() {

        MerchantService().getMerchant(
            merchantId = merchantList!!.get(spinner.selectedItemPosition).id,
            callback = object : BaseCallback<Merchant.MerchantList> {
                override fun onLoading() {

                }

                override fun onError(throwable: Throwable) {
                    throwable.printStackTrace()
                }

                override fun onSuccess(response: Merchant.MerchantList) {
                    merchantWithFields = response.arrayList

                    getFields()


                }

            }
        )
    }


}