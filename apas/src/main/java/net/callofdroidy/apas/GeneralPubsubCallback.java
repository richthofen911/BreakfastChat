package net.callofdroidy.apas;

/**
 * Created by admin on 23/03/16.
 */
public interface GeneralPubsubCallback {

    void onSuccessPublish(Object obj);
    void onFailPublish(Object obj);

    void onSuccessReceive(Message msg);
    void onFailReceive(Object obj);
}
