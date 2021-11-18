package com.example.yddc_2.navigation.word;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.yddc_2.R;
import com.example.yddc_2.bean.DaySentence;
import com.example.yddc_2.databinding.FirstFragmentBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class FirstFragment extends Fragment {
    private FirstFragmentBinding binding;
    private FirstViewModel mViewModel;

    private LineChartView lineChart;
    String[] date = {"10-22","11-22","12-22","1-22","6-22","5-23","5-22","6-22","5-23","5-22"};//X轴的标注
    int[] score= {50,42,90,33,10,74,22,18,79,20};//图表的数据点
    private List<PointValue> mPointValues = new ArrayList<>();
    private List<AxisValue> mAxisXValues = new ArrayList<>();


    public static FirstFragment newInstance() {
        return new FirstFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FirstFragmentBinding.inflate(inflater);
        //替换return inflater.inflate(R.layout.first_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FirstViewModel.class);
        // TODO: Use the ViewModel
        lineChart = binding.chart;
        getAxisXLables();//获取x轴的标注
        getAxisPoints();//获取坐标点
        initLineChart();
        /**
         * ********************************************
         */
        initDaySentence();

    }

    private void initLineChart(){

            Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
            List<Line> lines = new ArrayList<Line>();
            line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
            line.setCubic(false);//曲线是否平滑，即是曲线还是折线
            line.setFilled(false);//是否填充曲线的面积
            line.setHasLabels(true);//曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
            line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
            line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
            lines.add(line);
            LineChartData data = new LineChartData();
            data.setLines(lines);
            //坐标轴
            Axis axisX = new Axis(); //X轴
            axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
            axisX.setTextColor(Color.BLUE);  //设置字体颜色
            //axisX.setName("date");  //表格名称
            axisX.setTextSize(10);//设置字体大小
            axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
            axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
            data.setAxisXBottom(axisX); //x 轴在底部
            //data.setAxisXTop(axisX);  //x 轴在顶部
            axisX.setHasLines(true); //x 轴分割线
            // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
            Axis axisY = new Axis();  //Y轴
            axisY.setName("test");//y轴标注
            axisY.setTextSize(10);//设置字体大小
            data.setAxisYLeft(axisY);  //Y轴设置在左边
            //data.setAxisYRight(axisY);  //y轴设置在右边
            //设置行为属性，支持缩放、滑动以及平移
            lineChart.setInteractive(true);
            lineChart.setZoomType(ZoomType.VERTICAL);
            lineChart.setMaxZoom((float) 2);//最大方法比例
            lineChart.setContainerScrollEnabled(false, ContainerScrollType.HORIZONTAL);
            lineChart.setLineChartData(data);
            lineChart.setVisibility(View.VISIBLE);
            /**注：下面的7，10只是代表一个数字去类比而已
             * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
             */
            Viewport v = new Viewport(lineChart.getMaximumViewport());
            v.left = 0;
            v.right= 7;
            lineChart.setCurrentViewport(v);
        }
    /**
     * 设置X 轴的显示
     */
    private void getAxisXLables(){
        for (int i = 0; i < date.length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(date[i]));
        }
    }
    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints(){
        for (int i = 0; i < score.length; i++) {
            mPointValues.add(new PointValue(i, score[i]));
        }

    }


    /**
     * **************************************************************************
     */
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
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            textView.setText(daySentence.getNewslist().get(0).getNote());
//                            Glide.with(requireContext())
//                                    //加载网址
//                                    .load()
//                                    //设置占位图
//                                    .placeholder(R.mipmap.ic_launcher)
//                                    //加载错误图
//                                    .error(R.mipmap.ic_launcher)
//                                    //磁盘缓存的处理
//                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                    .into(imageView);
                            RoundedCorners roundedCorners = new RoundedCorners(20);//数字为圆角度数
                            RequestOptions coverRequestOptions = new RequestOptions()
                                    .transforms(new CenterCrop(), roundedCorners)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)//不做磁盘缓存
                                    .skipMemoryCache(true);//不做内存缓存
                            //Glide 加载图片简单用法
                            Glide.with(requireContext()).load(daySentence.getNewslist().get(0).getImgurl())
                                    .apply(coverRequestOptions).into(imageView);
                        }
                    });
                }
            }
        });
    }
}