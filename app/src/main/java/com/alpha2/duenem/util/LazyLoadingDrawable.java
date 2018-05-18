package com.alpha2.duenem.util;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LazyLoadingDrawable extends Drawable {
    private Drawable drawable;

    public LazyLoadingDrawable() {
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // when drawable is null, we draw nothing in view
        if (drawable != null) {
            drawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (drawable != null)
            drawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        if (drawable != null)
            drawable.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        if (drawable != null)
            return drawable.getOpacity();
        else
            return PixelFormat.OPAQUE;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
