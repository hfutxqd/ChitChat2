package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.hwangjr.rxbus.RxBus;
import com.orhanobut.logger.Logger;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.helpers.LocationHelper;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.helpers.RxHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.UserService;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.MainActivity;
import com.room517.chitchat.ui.adapters.UserAdapter;

import java.util.List;

import retrofit2.Retrofit;

/**
 * Created by ywwynm on 2016/5/24.
 * 显示附近的人的Fragment
 */
public class NearbyPeopleFragment extends BaseFragment {

    public static NearbyPeopleFragment newInstance(Bundle args) {
        NearbyPeopleFragment fragment = new NearbyPeopleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private MainActivity mActivity;

    private LinearLayout mLlEmpty;
    private ScrollView   mScrollView;

    private RecyclerView mRecyclerView;
    private UserAdapter  mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        super.init();
        return mContentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RxBus.get().post(Def.Event.BACK_FROM_FRAGMENT, new Object());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_nearby_people;
    }

    @Override
    protected void initMember() {
        mActivity = (MainActivity) getActivity();
    }

    @Override
    protected void findViews() {
        mLlEmpty    = f(R.id.ll_empty_state_nearby_people);
        mScrollView = f(R.id.sv_nearby_people);

        mRecyclerView = f(R.id.rv_nearby_people);
    }

    @Override
    protected void initUI() {
        updateActionbar();
        findNearbyUsers();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void updateActionbar() {
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.find_new_friend);
        }
    }

    private void findNearbyUsers() {
        Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
        UserService service = retrofit.create(UserService.class);
        double[] location = getLocationArr();
        RxHelper.ioMain(
                service.getNearbyUsers(App.getMe().getId(), location[0], location[1]),
                new SimpleObserver<List<User>>() {

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                    }

                    @Override
                    public void onNext(List<User> users) {
                        if (users.isEmpty()) {
                            // TODO: 2016/5/25 empty state for nearby people
                            return;
                        }

                        if (mAdapter == null) {
                            mAdapter = new UserAdapter(mActivity, users);
                            mRecyclerView.setAdapter(mAdapter);
                        } else {
                            mAdapter.setUsers(users);
                            mAdapter.notifyDataSetChanged();
                        }

                        for (User user : users) {
                            Logger.json(user.toString());
                        }
                    }
                });
    }

    private double[] getLocationArr() {
        double[] locationArr = LocationHelper.getLocationArray();
        if (locationArr == null) {
            locationArr = new double[2];
            User user = App.getMe();
            locationArr[0] = user.getLongitude();
            locationArr[1] = user.getLatitude();
        }
        return locationArr;
    }

    @Override
    protected void setupEvents() {
        mActivity.getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.getSupportFragmentManager().popBackStack();
            }
        });
    }
}
