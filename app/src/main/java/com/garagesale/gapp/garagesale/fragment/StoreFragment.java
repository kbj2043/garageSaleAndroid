package com.garagesale.gapp.garagesale.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.garagesale.gapp.garagesale.BaseFragment;
import com.garagesale.gapp.garagesale.R;
import com.garagesale.gapp.garagesale.databinding.FragmentStoreBinding;
import com.garagesale.gapp.garagesale.entity.User;
import com.garagesale.gapp.garagesale.response.UserResponse;
import com.garagesale.gapp.garagesale.service.UserService;
import com.garagesale.gapp.garagesale.util.Camera.*;
import com.garagesale.gapp.garagesale.util.DataContainer;
import com.garagesale.gapp.garagesale.util.TabPagerAdapter;
import com.garagesale.gapp.garagesale.util.setPermission;
import com.gun0912.tedpermission.PermissionListener;

import java.util.ArrayList;

import javax.inject.Inject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.app.Activity.RESULT_OK;


/**
 * Created by juyeol on 2017-06-28.
 * 현재 skeleton 레이아웃
 */
public class StoreFragment extends BaseFragment implements
        View.OnClickListener {

    // 싱글톤 패턴
    @SuppressLint("StaticFieldLeak")
    private static StoreFragment mInstance;

    public static StoreFragment getInstance() {
        if (mInstance == null) mInstance = new StoreFragment();
        return mInstance;
    }

    private FragmentStoreBinding binding;
    View view;
    @Inject
    public Retrofit retrofit;
    // 여기부턴 카메라관련 인자
    private UserService userService;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_GALLERY = 2;
    private Uri outputFileUri;
    private LoadPicture loadPicture;
    // 여기부턴 리스트 관련 인자들

    // 탭관련 인자들
    TabPagerAdapter mTabPagerAdapter;

    User user = DataContainer.getInstance().getmUser(); // user 정보

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_store, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        new setPermission(getContext(), GoogleMapPermission, Manifest.permission.ACCESS_FINE_LOCATION); // 권한요청 및 권한에따른 구글맵 셋팅});
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getNetworkComponent().inject(this); // retrofit 객체 주입
        userService = retrofit.create(UserService.class);
        binding = FragmentStoreBinding.bind(getView()); // Store 프레그먼트 View
        loadPicture = new LoadPicture(this, getContext());

        // Glide.with(getInstance()).load("http://192.168.42.180:3000/imeage/Screenshot_2017-07-04-17-56-47.png").into(binding.imageView5);

        mTabPagerAdapter = new TabPagerAdapter(getActivity().getSupportFragmentManager());
        binding.viewpager.setAdapter(mTabPagerAdapter);
        binding.tabs.setupWithViewPager(binding.viewpager);

        // 유저 정보 출력
        if (user != null) {
            binding.editText11.setText(user.getName() + "의 행성");    // 유저 이름 출력
            binding.editText12.setText(user.getPlanet().getName()); // 행성 이름 출력
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                outputFileUri = data.getData();

                if (outputFileUri != null) {
                    showImage(loadPicture.drawFile(outputFileUri));
                    sendServerImage();
                }
            } else if (requestCode == REQUEST_TAKE_PHOTO) {
                if (outputFileUri != null) {
                    showImage(loadPicture.drawFile(outputFileUri));
                    sendServerImage();
                }
            }
        }
    }

  /*  @Override
    public void sendMessageToParent(LatLng latLng, String address) {
        binding.editText13.setText("위도 : " + String.valueOf(latLng.longitude) + "\n경도 : " + String.valueOf(latLng.latitude));
        binding.editText14.setText(address);
    }*/

    /*****************카메라 처리 메소드 **********************/

    private void showImage(Bitmap bitmap) {
        Drawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        binding.imageView5.setImageDrawable(bitmapDrawable);
    }

    public void sendServerImage() {
        MultipartBody.Part body;
        body = loadPicture.createBody(outputFileUri);
        Call<UserResponse> repos = userService.uploadProfile(body);

        repos.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                Log.e("model", "Success");
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                Log.e("model", "Failed" + throwable.getMessage());
            }
        });
    }

    /*****************카메라 처리 메소드 끝**********************/
    /******************리스너 정의**********************/
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addItem:
                loadPicture.onGallery();
                break;
            case R.id.addItem2:
                outputFileUri = loadPicture.onCamera();
                break;
        }
    }

    /******************리스너 정의 끝**********************/

    /******************구글맵 메소드(+권한)**********************/
    PermissionListener GoogleMapPermission = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
        }
    };

    /******************구글맵 메소드(+권한) 끝**********************/


    @Override
    public String getTitle() {
        return "Store";
    }

}
