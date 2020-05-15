package com.liminal.eagamification.nav_menu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomDrawable extends View {
    private ShapeDrawable shapeDrawable;

    public CustomDrawable(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        shapeDrawable = new ShapeDrawable(new RectShape());
        shapeDrawable.getPaint().setColor(Color.WHITE);
        shapeDrawable.setBounds(0,0,100000,100000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        shapeDrawable.draw(canvas);
    }
}
