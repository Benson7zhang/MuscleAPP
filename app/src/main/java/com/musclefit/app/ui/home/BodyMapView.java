package com.musclefit.app.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.musclefit.app.R;

import java.util.Arrays;
import java.util.List;

public class BodyMapView extends View {
    public interface OnMuscleClickListener {
        void onMuscleClick(@NonNull String muscleKey);
    }

    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;
    public static final int SIDE_FRONT = 0;
    public static final int SIDE_BACK = 1;

    private static final String KEY_SHOULDER = "shoulder";
    private static final String KEY_CHEST = "chest";
    private static final String KEY_ABS = "abs";
    private static final String KEY_THIGH = "thigh";
    private static final String KEY_CALF = "calf";
    private static final String KEY_BACK = "back";

    private static final float SOURCE_IMAGE_WIDTH = 330f;
    private static final float SOURCE_IMAGE_HEIGHT = 729f;
    private static final float SOURCE_IMAGE_ASPECT = SOURCE_IMAGE_WIDTH / SOURCE_IMAGE_HEIGHT;
    private static final int ALPHA_THRESHOLD = 24;

    private static final List<HotspotMaskDef> FRONT_MASKS = Arrays.asList(
            new HotspotMaskDef(KEY_SHOULDER, R.drawable.body_hotspot_front_shoulder, 6f),
            new HotspotMaskDef(KEY_CHEST, R.drawable.body_hotspot_front_chest, 6f),
            new HotspotMaskDef(KEY_ABS, R.drawable.body_hotspot_front_abs, 5f),
            new HotspotMaskDef(KEY_THIGH, R.drawable.body_hotspot_front_thigh, 6f),
            new HotspotMaskDef(KEY_CALF, R.drawable.body_hotspot_front_calf, 4f)
    );

    private static final List<HotspotMaskDef> BACK_MASKS = Arrays.asList(
            new HotspotMaskDef(KEY_SHOULDER, R.drawable.body_hotspot_back_shoulder, 6f),
            new HotspotMaskDef(KEY_BACK, R.drawable.body_hotspot_back_back, 6f),
            new HotspotMaskDef(KEY_THIGH, R.drawable.body_hotspot_back_thigh, 6f),
            new HotspotMaskDef(KEY_CALF, R.drawable.body_hotspot_back_calf, 4f)
    );

    private final Paint highlightStrokePaint = new Paint();
    private final Paint highlightFillPaint = new Paint();
    private final RectF imageContentRect = new RectF();
    private final RectF resolvedImageRect = new RectF();
    private final RectF expandedMaskRect = new RectF();
    private final SparseArray<Bitmap> bitmapCache = new SparseArray<>();

    private int gender = GENDER_MALE;
    private int side = SIDE_FRONT;
    private boolean hasExplicitImageContentRect;
    private String selectedKey;
    private String pressedKey;
    private String pendingClickKey;

    private int hotspotPressedColor;
    private int hotspotSelectedColor;
    private int hotspotStrokeColor;

    private OnMuscleClickListener listener;

    public BodyMapView(Context context) {
        super(context);
        init();
    }

    public BodyMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BodyMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initPaint(highlightStrokePaint);
        initPaint(highlightFillPaint);

        hotspotPressedColor = ContextCompat.getColor(getContext(), R.color.body_map_hotspot_pressed);
        hotspotSelectedColor = ContextCompat.getColor(getContext(), R.color.body_map_hotspot_selected);
        hotspotStrokeColor = ContextCompat.getColor(getContext(), R.color.body_map_hotspot_stroke);

        setClickable(true);
    }

    private static void initPaint(Paint paint) {
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
    }

    public void setOnMuscleClickListener(@Nullable OnMuscleClickListener listener) {
        this.listener = listener;
    }

    public void setGender(int gender) {
        if (this.gender == gender) {
            return;
        }
        this.gender = gender;
        invalidate();
    }

    public void setSide(int side) {
        if (this.side == side) {
            return;
        }
        this.side = side;
        invalidate();
    }

    public int getSide() {
        return side;
    }

    public void setDrawSilhouette(boolean drawSilhouette) {
        // Compatibility no-op: silhouette is provided by the background ImageView.
    }

    public void setSelectedMuscleKey(@Nullable String selectedMuscleKey) {
        if (selectedMuscleKey == null && selectedKey == null) {
            return;
        }
        if (selectedMuscleKey != null && selectedMuscleKey.equals(selectedKey)) {
            return;
        }
        selectedKey = selectedMuscleKey;
        invalidate();
    }

    public void setImageContentRect(@Nullable RectF rect) {
        if (rect == null || rect.width() <= 0f || rect.height() <= 0f) {
            hasExplicitImageContentRect = false;
            imageContentRect.setEmpty();
            invalidate();
            return;
        }
        if (hasExplicitImageContentRect
                && almostEqual(imageContentRect.left, rect.left)
                && almostEqual(imageContentRect.top, rect.top)
                && almostEqual(imageContentRect.right, rect.right)
                && almostEqual(imageContentRect.bottom, rect.bottom)) {
            return;
        }
        hasExplicitImageContentRect = true;
        imageContentRect.set(rect);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bitmapCache.clear();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        resolveImageRect();
        if (resolvedImageRect.width() <= 0f || resolvedImageRect.height() <= 0f) {
            return;
        }

        List<HotspotMaskDef> defs = currentMaskDefs();
        for (HotspotMaskDef def : defs) {
            Bitmap mask = getMaskBitmap(def.maskResId);
            if (mask == null) {
                continue;
            }

            int highlightColor;
            if (def.key.equals(pressedKey)) {
                highlightColor = hotspotPressedColor;
            } else if (def.key.equals(selectedKey)) {
                highlightColor = hotspotSelectedColor;
            } else {
                continue;
            }

            float scale = resolvedImageRect.width() / SOURCE_IMAGE_WIDTH;
            // Fill uses the exact mask bounds to fully cover the selected muscle region.
            float strokeExpandPx = Math.max(1.2f, scale * 0.90f);
            float fillExpandPx = 0f;

            drawMaskColor(canvas, mask, hotspotStrokeColor, highlightStrokePaint, strokeExpandPx);
            drawMaskColor(canvas, mask, highlightColor, highlightFillPaint, fillExpandPx);
        }

        highlightStrokePaint.setColorFilter(null);
        highlightFillPaint.setColorFilter(null);
    }

    private void drawMaskColor(@NonNull Canvas canvas, @NonNull Bitmap mask, int color, @NonNull Paint paint, float expandPx) {
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        if (expandPx <= 0f) {
            canvas.drawBitmap(mask, null, resolvedImageRect, paint);
            return;
        }

        expandedMaskRect.set(resolvedImageRect);
        expandedMaskRect.inset(-expandPx, -expandPx);
        canvas.drawBitmap(mask, null, expandedMaskRect, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        resolveImageRect();
        float x = event.getX();
        float y = event.getY();
        String hitKey = findHitKey(x, y);
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            if (hitKey == null) {
                return false;
            }
            pressedKey = hitKey;
            invalidate();
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (pressedKey == null) {
                return false;
            }
            if (hitKey == null || !pressedKey.equals(hitKey)) {
                pressedKey = null;
                invalidate();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP) {
            if (pressedKey != null && pressedKey.equals(hitKey)) {
                selectedKey = pressedKey;
                pendingClickKey = selectedKey;
                pressedKey = null;
                invalidate();
                performClick();
                return true;
            }
            pressedKey = null;
            invalidate();
            return true;
        }

        if (action == MotionEvent.ACTION_CANCEL) {
            pressedKey = null;
            pendingClickKey = null;
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        if (pendingClickKey != null && listener != null) {
            listener.onMuscleClick(pendingClickKey);
        }
        pendingClickKey = null;
        return true;
    }

    @Nullable
    private String findHitKey(float viewX, float viewY) {
        if (!resolvedImageRect.contains(viewX, viewY)) {
            return null;
        }

        int[] sourcePoint = toSourcePixel(viewX, viewY);
        if (sourcePoint == null) {
            return null;
        }
        int px = sourcePoint[0];
        int py = sourcePoint[1];

        List<HotspotMaskDef> defs = currentMaskDefs();

        for (HotspotMaskDef def : defs) {
            Bitmap mask = getMaskBitmap(def.maskResId);
            if (mask == null) {
                continue;
            }
            if (isOpaque(mask, px, py)) {
                return def.key;
            }
        }

        float density = getResources().getDisplayMetrics().density;
        for (HotspotMaskDef def : defs) {
            Bitmap mask = getMaskBitmap(def.maskResId);
            if (mask == null) {
                continue;
            }
            int expandPx = Math.max(1, Math.round(def.touchExpandDp * density));
            if (isOpaqueNearby(mask, px, py, expandPx)) {
                return def.key;
            }
        }

        return null;
    }

    @Nullable
    private int[] toSourcePixel(float viewX, float viewY) {
        float nx = (viewX - resolvedImageRect.left) / resolvedImageRect.width();
        float ny = (viewY - resolvedImageRect.top) / resolvedImageRect.height();
        if (nx < 0f || nx > 1f || ny < 0f || ny > 1f) {
            return null;
        }

        int px = Math.round(nx * (SOURCE_IMAGE_WIDTH - 1f));
        int py = Math.round(ny * (SOURCE_IMAGE_HEIGHT - 1f));
        px = Math.max(0, Math.min((int) SOURCE_IMAGE_WIDTH - 1, px));
        py = Math.max(0, Math.min((int) SOURCE_IMAGE_HEIGHT - 1, py));
        return new int[]{px, py};
    }

    private boolean isOpaque(@NonNull Bitmap mask, int x, int y) {
        int pixel = mask.getPixel(x, y);
        int alpha = (pixel >>> 24) & 0xFF;
        return alpha >= ALPHA_THRESHOLD;
    }

    private boolean isOpaqueNearby(@NonNull Bitmap mask, int x, int y, int radiusPx) {
        int minX = Math.max(0, x - radiusPx);
        int maxX = Math.min(mask.getWidth() - 1, x + radiusPx);
        int minY = Math.max(0, y - radiusPx);
        int maxY = Math.min(mask.getHeight() - 1, y + radiusPx);
        int radiusSquared = radiusPx * radiusPx;

        for (int py = minY; py <= maxY; py++) {
            int dy = py - y;
            for (int px = minX; px <= maxX; px++) {
                int dx = px - x;
                if ((dx * dx) + (dy * dy) > radiusSquared) {
                    continue;
                }
                if (isOpaque(mask, px, py)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void resolveImageRect() {
        if (hasExplicitImageContentRect) {
            resolvedImageRect.set(imageContentRect);
            return;
        }

        float contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float contentHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (contentWidth <= 0f || contentHeight <= 0f) {
            resolvedImageRect.setEmpty();
            return;
        }

        float drawnWidth;
        float drawnHeight;
        float contentAspect = contentWidth / contentHeight;
        if (contentAspect > SOURCE_IMAGE_ASPECT) {
            drawnHeight = contentHeight;
            drawnWidth = drawnHeight * SOURCE_IMAGE_ASPECT;
        } else {
            drawnWidth = contentWidth;
            drawnHeight = drawnWidth / SOURCE_IMAGE_ASPECT;
        }

        float left = getPaddingLeft() + ((contentWidth - drawnWidth) / 2f);
        float top = getPaddingTop() + ((contentHeight - drawnHeight) / 2f);
        resolvedImageRect.set(left, top, left + drawnWidth, top + drawnHeight);
    }

    @Nullable
    private Bitmap getMaskBitmap(int resId) {
        Bitmap cached = bitmapCache.get(resId);
        if (cached != null) {
            return cached;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap decoded = BitmapFactory.decodeResource(getResources(), resId, options);
        if (decoded == null) {
            return null;
        }

        decoded = normalizeTransparentPixels(decoded);
        bitmapCache.put(resId, decoded);
        return decoded;
    }

    private static Bitmap normalizeTransparentPixels(@NonNull Bitmap bitmap) {
        if (!bitmap.isMutable()) {
            Bitmap mutableCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (mutableCopy != null) {
                bitmap = mutableCopy;
            }
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int alpha = (color >>> 24) & 0xFF;
            if (alpha == 0) {
                pixels[i] = 0;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private List<HotspotMaskDef> currentMaskDefs() {
        if (side == SIDE_BACK) {
            return BACK_MASKS;
        }
        return FRONT_MASKS;
    }

    private static boolean almostEqual(float a, float b) {
        return Math.abs(a - b) < 0.5f;
    }

    private static final class HotspotMaskDef {
        private final String key;
        private final int maskResId;
        private final float touchExpandDp;

        private HotspotMaskDef(String key, int maskResId, float touchExpandDp) {
            this.key = key;
            this.maskResId = maskResId;
            this.touchExpandDp = touchExpandDp;
        }
    }
}
