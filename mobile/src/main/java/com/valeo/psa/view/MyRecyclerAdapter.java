package com.valeo.psa.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.valeo.psa.R;
import com.valeo.psa.model.ViewModel;

import java.lang.ref.WeakReference;
import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {
    private List<ViewModel> items;
    private int itemLayout;
    private Typeface typeface;
    private final OnStartDragListener mDragStartListener;
    private final View.OnTouchListener mTouchListener;
    //private final OnIconLongPressedListener onIconLongPressedListener;
    //private final GestureDetectorCompat mDetector;

    public MyRecyclerAdapter(Context context, final WeakReference<RecyclerView> recyclerViewWeakReference, List<ViewModel> items, int itemLayout,
                             Typeface typeface, OnStartDragListener mDragStartListener, //OnIconLongPressedListener onIconLongPressedListener,
                             View.OnTouchListener mTouchListener) {
        this.items = items;
        this.itemLayout = itemLayout;
        this.typeface = typeface;
        this.mDragStartListener = mDragStartListener;
        this.mTouchListener = mTouchListener;
        //this.onIconLongPressedListener = onIconLongPressedListener;
        /*this.mDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                View childView = recyclerViewWeakReference.get().findChildViewUnder(e.getX(), e.getY());
                MyRecyclerAdapter.this.onIconLongPressedListener.onIconLongPressed(recyclerViewWeakReference.get().getChildAdapterPosition(childView));
                super.onLongPress(e);
            }
        });*/
    }

    public List<ViewModel> getItems() {
        return items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        //return new ViewHolder(v, mDragStartListener, mDetector);
        return new ViewHolder(v, mDragStartListener, mTouchListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ViewModel item = items.get(position);
        holder.text.setText(item.getActionTitle());
        holder.text.setTypeface(typeface);
        holder.icon.setImageDrawable(item.getIcon());
        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView text;
        public ImageView drag;

        public ViewHolder(View itemView, final OnStartDragListener mDragStartListener, final View.OnTouchListener mTouchListener){//, final GestureDetectorCompat mDetector) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            /*icon.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d("icon onTouchEvent", "ACTION_DOWN");
                            //mDetector.onTouchEvent(event);
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.d("icon onTouchEvent", "ACTION_UP");
                            break;
                    }
                    return true;
                }
            });*/
            icon.setOnTouchListener(mTouchListener);
            text = (TextView) itemView.findViewById(R.id.control_action);
            drag = (ImageView) itemView.findViewById(R.id.drag_icon);
            drag.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(ViewHolder.this);
                    }
                    return true;
                }
            });
        }
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnIconLongPressedListener {
        void onIconLongPressed(int position);
    }
}
