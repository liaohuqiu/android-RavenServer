package in.srain.demos.ravenserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public final class RavenService extends Service {

    public static final int RAVEN_PORT = 23457;
    public static final String RAVEN_URL = "https://github.com/liaohuqiu/android-RavenServer";

    private static final String KEY_FOR_WEAK_LOCK = "weak-lock";
    private static final String KEY_FOR_CMD = "cmd";
    private InnerHTTPD mHTTPD;

    public static void start(Context context) {
        Intent serviceIntent = new Intent(context, RavenService.class);
        context.startService(serviceIntent);
    }

    public static void startForWeakLock(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, RavenService.class);
        context.startService(serviceIntent);

        intent.putExtra(RavenService.KEY_FOR_WEAK_LOCK, true);
        Intent myIntent = new Intent(context, RavenService.class);

        // using wake lock to start service
        WakefulBroadcastReceiver.startWakefulService(context, myIntent);
    }

    @Override
    public void onCreate() {
        mHTTPD = new InnerHTTPD(RAVEN_PORT);
        try {
            mHTTPD.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHTTPD != null) {
            mHTTPD.stop();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Utils.printIntent("onStartCommand", intent);

        if (intent != null) {
            // remove wake lock
            if (intent.getBooleanExtra(KEY_FOR_WEAK_LOCK, false)) {
                BootCompletedReceiver.completeWakefulIntent(intent);
            }
            String cmd = intent.getStringExtra(KEY_FOR_CMD);
            if (!TextUtils.isEmpty(cmd)) {
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class InnerHTTPD extends NanoHTTPD {

        public InnerHTTPD(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            final String html = String.format("<header> <meta http-equiv='refresh' content='0; url=%s' /> </header>", RAVEN_URL);
            Response response = NanoHTTPD.newFixedLengthResponse(Response.Status.OK, MIME_HTML, html);
            SecondActivity.start(getApplication());
            return response;
        }
    }
}