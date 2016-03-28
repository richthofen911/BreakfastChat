package net.callofdroidy.apas;

/**
 * Created by admin on 23/03/16.
 */
public interface PubsubProviderClient<S extends GeneralPubsubCallback> {

    void subscribeToChannel(String channelName, S generalPubsubCallback);

    void publishToChannel(String channelName, Message message, S generalPubsubCallback);
}
