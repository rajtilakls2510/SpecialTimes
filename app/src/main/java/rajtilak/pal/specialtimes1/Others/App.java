package rajtilak.pal.specialtimes1.Others;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID="NotificationChannel1";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();

    }

    private void createNotificationChannels() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel1=new NotificationChannel(CHANNEL_ID,"Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("This is a Notification Channel1");

            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }
}
