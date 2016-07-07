package xyz.imxqd.photochooser.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

public class ImageBean implements Parcelable {

	private String path = null;
	private boolean isSelected = false;

	private Object selector = null;
	private View cover = null;

	public ImageBean(String path, boolean selected) {
		this.path = path;
		this.isSelected = selected;
	}

	public ImageBean() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(path);
		dest.writeInt(isSelected ? 1 : 0);
	}

	public static final Creator<ImageBean> CREATOR = new Creator<ImageBean>() {

		@Override
		public ImageBean createFromParcel(Parcel source) {
			return new ImageBean(source.readString(), source.readInt() == 1 ? true : false);
		}

		@Override
		public ImageBean[] newArray(int size) {
			return new ImageBean[size];
		}
	};

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the isSelected
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * @param isSelected the isSelected to set
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public void setSelector(Object selector) {
		this.selector = selector;
	}

	public Object getSelector() {
		return selector;
	}

	public void setCover(View cover) {
		this.cover = cover;
	}

	public View getCover() {
		return cover;
	}
}
