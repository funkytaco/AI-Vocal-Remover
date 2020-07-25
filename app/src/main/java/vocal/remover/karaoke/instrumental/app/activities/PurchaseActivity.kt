package vocal.remover.karaoke.instrumental.app.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.billingclient.api.*
import vocal.remover.karaoke.instrumental.app.databinding.ActivityPurchaseBinding
import vocal.remover.karaoke.instrumental.app.utils_java.SessionManager.getSessionManagerInstance

class PurchaseActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private val skuList = listOf("50_songs")
    lateinit var binding: ActivityPurchaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        setupBillingClient()


    }



    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                acknowledgePurchase(purchase.purchaseToken, purchase)

            }
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.

        } else {
            // Handle any other error codes.
        }
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this)
                .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is setup successfully
                    Log.e(Companion.TAG, "onBillingSetupFinished: Setup Billing Done " )
                    loadAllSKUs()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

            }
        })

    }

    private fun loadAllSKUs() = if (billingClient.isReady) {
        Log.e(Companion.TAG, "billing client ready " )
        val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                for (skuDetails in skuDetailsList) {
                    binding.buttonBuyProduct.setOnClickListener {
                        Log.e(TAG, "loadAllSKUs: calling" )
                        val billingFlowParams = BillingFlowParams
                                .newBuilder()
                                .setSkuDetails(skuDetails)
                                .build()
                        billingClient.launchBillingFlow(this, billingFlowParams)
                    }
                    if (skuDetails.sku == "50_songs") {

                        binding.buttonBuyProduct.setOnClickListener {
                            Log.e(TAG, "loadAllSKUs: calling" )
                            val billingFlowParams = BillingFlowParams
                                    .newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                            billingClient.launchBillingFlow(this, billingFlowParams)
                        }
                    }

                }
            } else {
                Log.e(TAG, "else"+ billingResult.responseCode+" - "+ skuDetailsList.size )
            }
        }

    } else {
        println("Billing Client not ready")
        Log.e(Companion.TAG, "onBillingSetupFinished: Billing Client not ready " )
    }

    private fun acknowledgePurchase(purchaseToken: String, purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage

            if (purchase.sku.equals("50_songs")) {
                Toast.makeText(this, "Thank you for purchasing!", Toast.LENGTH_SHORT).show();
                getSessionManagerInstance().coins = 10
            }
        }
    }

    companion object {
        private const val TAG = "PurchaseActivity"
    }


}