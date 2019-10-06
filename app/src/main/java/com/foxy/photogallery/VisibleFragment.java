package com.foxy.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

// Предоставляет обобщенный фрагмент, скрывающий оповещения переднего плана
public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";

    // регистрация приемника
    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(onShowNotification, filter, PollService.PERM_PRIVATE, null);
    }

    // отмена регистрации
    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(onShowNotification);
    }

    private BroadcastReceiver onShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getActivity(),
                    "Got a broadcast: " + intent.getAction(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    };
}
