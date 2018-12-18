package com.example.app.adapter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.app.R;
import com.example.app.activity.MkActivity;
import com.example.app.bean.GridModel;
import com.god.adapter.recyclerview.BaseRecyclerAdapter;
import com.god.adapter.recyclerview.BaseRecyclerViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by abook23 on 2016/6/2.
 */
public class RecyclerAdapter extends BaseRecyclerAdapter<GridModel, RecyclerAdapter.ViewHolder> {

    public RecyclerAdapter(List<GridModel> data) {
        super(R.layout.item_image_01, data);
    }

    @Override
    protected void convert(RecyclerAdapter.ViewHolder holder, final int position, GridModel gridModel) {
        holder.text.setText(gridModel.getTitle());
        if (gridModel.getMdName() != null) {
            holder.codeBut.setVisibility(View.VISIBLE);
            holder.codeBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MkActivity.show(mContext, getItem(position).getMdName());
                }
            });
        } else {
            holder.codeBut.setVisibility(View.GONE);
        }
    }

    static class ViewHolder extends BaseRecyclerViewHolder {
        @BindView(R.id.text)
        TextView text;
        @BindView(R.id.codeBut)
        Button codeBut;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
