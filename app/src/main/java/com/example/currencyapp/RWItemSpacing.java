package com.example.currencyapp;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RWItemSpacing extends RecyclerView.ItemDecoration {

    private final Integer vertivalSpace;
    private final Integer rightSpace;

    public RWItemSpacing(Integer verticalSpace, Integer rightSpace) {
        this.vertivalSpace = verticalSpace;
        this.rightSpace = rightSpace;
    }



    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(vertivalSpace  != null) outRect.bottom = vertivalSpace;
        if(rightSpace != null) outRect.right = rightSpace;
    }
}