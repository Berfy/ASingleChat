package cn.berfy.sdk.mvpbase.listener;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface CommonFragmentListener {

    void onCreate(@Nullable Bundle savedInstanceState);

    void onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    void onViewCreated(View view, @Nullable Bundle savedInstanceState);

    void onResume();

    void onPause();

    void onStop();

    void onDestroyView();

    void onDestroy();

    void onFragmentRemoved();//被removed
}
