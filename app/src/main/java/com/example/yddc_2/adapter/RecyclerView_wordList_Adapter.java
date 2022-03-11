package com.example.yddc_2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yddc_2.R;
import com.example.yddc_2.bean.WordList;
import com.example.yddc_2.utils.GetNetService;
import com.example.yddc_2.utils.SecuritySP;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RecyclerView_wordList_Adapter extends RecyclerView.Adapter<RecyclerView_wordList_Adapter.RvViewHolder>{
    private final int layoutId;
    private final Context context;
    private final WordList wordList;
    private final int flag;
    public RecyclerView_wordList_Adapter(int layoutId,Context context,WordList wordList,int flag) {
        this.layoutId = layoutId;
        this.context = context;
        this.wordList = wordList;
        this.flag = flag;
    }
    @NonNull
    @Override
    public RecyclerView_wordList_Adapter.RvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layoutId,null);
        return new RecyclerView_wordList_Adapter.RvViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView_wordList_Adapter.RvViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.tv.setText(wordList.getData().get(position).getWord().getSpell());
            holder.tvDetail.setText(wordList.getData().get(position).getWord().getClearfix().get(0).getClearfix());
            if (flag == 1)//收藏模式，改为黄色
            {
                holder.tv_tip.setBackgroundColor(context.getColor(R.color.item));
            }
    }

    @Override
    public int getItemCount() {
        return wordList.getData().size();
    }

    static class RvViewHolder extends RecyclerView.ViewHolder{
        public TextView tv;
        public TextView tvDetail;
        public TextView tv_tip;
        public RvViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv = (TextView)itemView.findViewById(R.id.word1);
            tvDetail = (TextView)itemView.findViewById(R.id.word2);
            tv_tip = (TextView) itemView.findViewById(R.id.tip);
        }
    }


}
