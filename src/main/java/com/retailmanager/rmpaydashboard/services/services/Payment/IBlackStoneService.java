package com.retailmanager.rmpaydashboard.services.services.Payment;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import com.retailmanager.rmpaydashboard.services.services.Payment.data.ResponsePayment;

public interface IBlackStoneService {
    /**
     * Performs a payment transaction with a credit card.
     *
     * @param  Amount               the amount to be paid
     * @param  ZipCode              the zip code associated with the credit card
     * @param  CardNumber           the credit card number
     * @param  ExpDate              the expiration date of the credit card
     * @param  NameOnCard           the name on the credit card
     * @param  CVN                  the card verification number
     * @param  Track2               the track 2 data of the credit card
     * @param  UserTransactionNumber the user's transaction number
     * @return                     the payment response
     */
    public ResponsePayment paymentWithCreditCard(String Amount, String ZipCode, String CardNumber, String ExpDate, String NameOnCard, 
            String CVN, String Track2, String UserTransactionNumber)throws ConsumeAPIException;
}
