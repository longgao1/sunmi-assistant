package sunmi.common.base.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
	private final SparseArray<View> mViews;
	private int mPosition;
	private View mConvertView;

	private ViewHolder(Context context, ViewGroup parent, int layoutId, int position) {
		this.mPosition = position;
		this.mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
		mConvertView.setTag(this);
	}

	public static ViewHolder get(Context context, View convertView, ViewGroup parent, int layoutId, int position) {
		if (convertView == null) {
			return new ViewHolder(context, parent, layoutId, position);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.setPosition(position);
		return holder;
	}

	private void setPosition(int position) {
		this.mPosition = position;
	}

	public int getPosition() {
		return mPosition;
	}

	public View getConvertView() {
		return mConvertView;
	}

	@SuppressWarnings("unchecked")
	public <T extends View> T getView(int viewId) {
		View view = mViews.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	public ViewHolder setText(int viewId, String text) {
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}

	public ViewHolder setImageResource(int viewId, int drawableId) {
		ImageView view = getView(viewId);
		view.setImageResource(drawableId);

		return this;
	}

	public ViewHolder setImageBitmap(int viewId, Bitmap bm) {
		ImageView view = getView(viewId);
		view.setImageBitmap(bm);
		return this;
	}

	/* public ViewHolder setImageByUrl(int viewId, String url) {
	 * ImageLoader.getInstance(3, Type.LIFO).loadImage(url, (ImageView) getView(viewId));
	 * return this;
	 * } */
}
