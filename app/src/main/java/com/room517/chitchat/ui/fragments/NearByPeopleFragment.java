package com.room517.chitchat.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.amap.api.location.AMapLocation;
import com.google.gson.JsonElement;
import com.hwangjr.rxbus.RxBus;
import com.orhanobut.logger.Logger;
import com.room517.chitchat.App;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.db.UserDao;
import com.room517.chitchat.helpers.LocationHelper;
import com.room517.chitchat.helpers.RetrofitHelper;
import com.room517.chitchat.io.SimpleObserver;
import com.room517.chitchat.io.network.UserService;
import com.room517.chitchat.model.User;
import com.room517.chitchat.ui.activities.MainActivity;
import com.room517.chitchat.ui.adapters.UserAdapter;
import com.room517.chitchat.utils.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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
    private NestedScrollView mScrollView;

    private RecyclerView mRecyclerView;
    private UserAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mShouldBackFromFragment) {
            RxBus.get().post(Def.Event.BACK_FROM_FRAGMENT, new Object());
        }
        RxBus.get().unregister(this);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_nearby_people;
    }

    @Override
    protected void beforeInit() {
        RxBus.get().register(this);
    }

    @Override
    protected void initMember() {
        mActivity = (MainActivity) getActivity();
    }

    @Override
    protected void findViews() {
        mLlEmpty = f(R.id.ll_empty_state_nearby_people);
        mScrollView = f(R.id.sv_nearby_people);
        mRecyclerView = f(R.id.rv_nearby_people);
    }

    @Override
    protected void initUI() {
        mScrollView.setNestedScrollingEnabled(false);
        mRecyclerView.setNestedScrollingEnabled(false);
        RxBus.get().post(Def.Event.PREPARE_FOR_FRAGMENT, new Object());
        updateActionbar();
        updateLoadingState(true);
        findNearbyUsers();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setNestedScrollingEnabled(false);
    }

    private void updateActionbar() {
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.find_new_friend);
        }
    }

    private void updateLoadingState(boolean loading) {
        ProgressBar pb = f(R.id.pb_loading);
        if (loading) {
            pb.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.INVISIBLE);
        } else {
            pb.setVisibility(View.INVISIBLE);
            mScrollView.setVisibility(View.VISIBLE);
        }
    }

    private void findNearbyUsers() {
        // try to use RxAndroid on 2016/7/14
        Observable.create(new Observable.OnSubscribe<AMapLocation>() {
            @Override
            public void call(Subscriber<? super AMapLocation> subscriber) {
                subscriber.onNext(App.getLocationHelper().getLocationSync());
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .map(new Func1<AMapLocation, AMapLocation>() {
            @Override
            public AMapLocation call(AMapLocation location) {
                if (location == null) {
                    User me = App.getMe();
                    location = new AMapLocation("");
                    location.setLatitude(me.getLatitude());
                    location.setLongitude(me.getLongitude());
                }
                return location;
            }
        })
        .subscribeOn(Schedulers.io()) // don't know if so many subscribeOn/observeOn are necessary
        .observeOn(Schedulers.io())
        .flatMap(new Func1<AMapLocation, Observable<ResponseBody>>() {
            @Override
            public Observable<ResponseBody> call(AMapLocation location) {
                // use flatMap so that we can use Observable<ResponseBody> directly
                Retrofit retrofit = RetrofitHelper.getBaseUrlRetrofit();
                UserService service = retrofit.create(UserService.class);
                return service.getNearbyUsers(
                        App.getMe().getId(), location.getLongitude(), location.getLatitude());
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SimpleObserver<ResponseBody>() {
            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                updateLoadingState(false);
            }
            @Override
            public void onNext(ResponseBody body) {
                handleFindNearbyUsersResult(body);
            }
        });
    }

    private void handleFindNearbyUsersResult(ResponseBody body) {
        updateLoadingState(false);
        try {
            String json = body.string();
            Logger.json(json);

            List<JsonElement> jsonElements = JsonUtil.getJsonElements(json);
            List<User> users = new ArrayList<>();
            List<Integer> distances = new ArrayList<>();
            for (JsonElement jsonElement : jsonElements) {
                users.add(JsonUtil.getObject(jsonElement, User.class));
                distances.add(JsonUtil.getParam(
                        jsonElement, Def.Network.DISTANCE).getAsInt());
            }

            if (users.isEmpty()) {
                // TODO: 2016/5/25 empty state for nearby people
                return;
            }

            if (mAdapter == null) {
                mAdapter = new UserAdapter(mActivity, users, distances);
                mAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClicked(User user) {
                        UserDao.getInstance().insert(user);
                        Def.Event.StartChat startChat
                                = new Def.Event.StartChat();
                        startChat.user = user;
                        RxBus.get().post(Def.Event.START_CHAT, startChat);
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setUsers(users);
                mAdapter.notifyDataSetChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
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
}
