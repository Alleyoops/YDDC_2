package com.example.yddc_2.navigation.find;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.yddc_2.R;
import com.example.yddc_2.ResultActivity;
import com.example.yddc_2.bean.DaySentence;

import com.example.yddc_2.databinding.SecondFragmentBinding;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;


import java.util.ArrayList;

public class SecondFragment extends Fragment implements OnBannerListener {
    private SecondFragmentBinding binding;
    private SecondViewModel mViewModel;
    public static SecondFragment newInstance() {
        return new SecondFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SecondFragmentBinding.inflate(inflater);
        initBanner();
        initSearch();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SecondViewModel.class);
        // TODO: Use the ViewModel
        initDaySentence();
    }
    private void initDaySentence()
    {
        mViewModel.getMds(getContext()).observe(getViewLifecycleOwner(), new Observer<DaySentence>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(DaySentence daySentence) {
                TextView textView = (TextView) requireActivity().findViewById(R.id.textView4);
                ImageView imageView = (ImageView)requireActivity().findViewById(R.id.dayView);
                if (daySentence.getCode()!=200)
                {
                    textView.setText("????????????????????????????????????");
                    imageView.setImageResource(R.drawable.img3);
                }
                else
                {
                    textView.setText(daySentence.getNewslist().get(0).getContent());
                    //Glide ????????????????????????
                    RoundedCorners roundedCorners = new RoundedCorners(80);//?????????????????????
                    RequestOptions coverRequestOptions = new RequestOptions()
                            .transforms(new FitCenter(), roundedCorners)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)//??????????????????
                            .skipMemoryCache(true);//??????????????????
                    Glide.with(requireContext()).load(daySentence.getNewslist().get(0).getImgurl())
                            .apply(coverRequestOptions).into(imageView);

                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {imageView.setVisibility(View.VISIBLE);
                            textView.setText(daySentence.getNewslist().get(0).getNote());
                            Handler handler=new Handler();
                            Runnable runnable=new Runnable() {
                                @Override
                                public void run() {
                                    textView.setText(daySentence.getNewslist().get(0).getContent());
                                }
                            };
                            handler.postDelayed(runnable, 5000);
                        }
                    });
                }
            }
        });
    }

    private Banner mBanner;
    private MyImageLoader mMyImageLoader;
    private ArrayList<Integer> imagePath;
    private ArrayList<String> imageTitle;
    private void initBanner() {
        imagePath = new ArrayList<>();
        imageTitle = new ArrayList<>();
        imagePath.add(R.drawable.img_banner_4);
        imagePath.add(R.drawable.img_banner_3);
        imagePath.add(R.drawable.img_banner_1);
        imageTitle.add("null");
        imageTitle.add("null");
        imageTitle.add("null");
        mMyImageLoader = new MyImageLoader();
        mBanner = binding.banner;
        //??????????????????????????????????????????????????????????????????
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        //?????????????????????
        mBanner.setImageLoader(mMyImageLoader);
        //???????????????????????????,????????????????????????,????????????????????????
        mBanner.setBannerAnimation(Transformer.Default);
        //?????????????????????
        mBanner.setBannerTitles(imageTitle);
        //????????????????????????
        mBanner.setDelayTime(5000);
        //???????????????????????????????????????true
        mBanner.isAutoPlay(true);
        //???????????????????????????????????????????????????
        mBanner.setIndicatorGravity(BannerConfig.CENTER);
        //????????????????????????
        mBanner.setImages(imagePath)
                //??????????????????
                .setOnBannerListener(this)
                //??????????????????????????????????????????
                .start();
    }
    /**
     * ??????????????????
     *
     * @param position
     */
    @Override
    public void OnBannerClick(int position) {
        Toast.makeText(getContext(), "????????????" + (position + 1) + "????????????", Toast.LENGTH_SHORT).show();
    }
    /**
     * ???????????????
     */
    private class MyImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context.getApplicationContext())
                    .load(path)
                    .into(imageView);
        }
    }

    private void initSearch()
    {
        SearchView searchView = binding.toolbar.findViewById(R.id.searchView);
        // ????????????????????????
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //????????????????????????
                Intent intent = new Intent();
                intent.setClass(getContext(), ResultActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("query",query);
                intent.putExtra("result",bundle);
                startActivity(intent);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

    }

}