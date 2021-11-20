package com.example.yddc_2.navigation.find;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.yddc_2.R;
import com.example.yddc_2.bean.DaySentence;
import com.example.yddc_2.databinding.FirstFragmentBinding;
import com.example.yddc_2.databinding.SecondFragmentBinding;

public class SecondFragment extends Fragment {

    private SecondViewModel mViewModel;
    private SecondFragmentBinding binding;
    public static SecondFragment newInstance() {
        return new SecondFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SecondFragmentBinding.inflate(inflater);
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
                if (daySentence.getNewslist()==null)
                {
                    textView.setText("Loading error ~");
                    imageView.setImageResource(R.drawable.img3);
                }
                else
                {
                    textView.setText(daySentence.getNewslist().get(0).getContent());
                    //Glide 加载图片简单用法
                    RoundedCorners roundedCorners = new RoundedCorners(40);//数字为圆角度数
                    RequestOptions coverRequestOptions = new RequestOptions()
                            .transforms(new FitCenter(), roundedCorners)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)//不做磁盘缓存
                            .skipMemoryCache(true);//不做内存缓存
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

}