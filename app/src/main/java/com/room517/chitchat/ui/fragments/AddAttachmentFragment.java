package com.room517.chitchat.ui.fragments;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.room517.chitchat.Def;
import com.room517.chitchat.R;
import com.room517.chitchat.ui.adapters.BaseViewHolder;

/**
 * Created by ywwynm on 2016/7/6.
 * 一个用来显示要发送哪种媒体消息的Fragment
 * 媒体类型包括照片、图片、音频、地理位置及文件
 */
public class AddAttachmentFragment extends BaseFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_add_attachment;
    }

    @Override
    protected void initUI() {
        RecyclerView rv = f(R.id.rv_add_attachment);
        rv.setAdapter(new AttachmentAdapter());
        rv.setLayoutManager(new GridLayoutManager(getActivity(), 4));
    }

    class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.Holder> {

        private int[] mIcons = new int[] {
                R.drawable.ic_take_photo,
                R.drawable.ic_image,
                R.drawable.ic_audio,
                R.drawable.ic_location,
                R.drawable.ic_file
        };
        private String[] mTexts
                = getActivity().getResources().getStringArray(R.array.attachment_type);

        private LayoutInflater mInflater = LayoutInflater.from(getActivity());

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(mInflater.inflate(R.layout.rv_add_attachment, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.tv.setText(mTexts[position]);
            holder.tv.setCompoundDrawablesWithIntrinsicBounds(0, mIcons[position], 0, 0);
        }

        @Override
        public int getItemCount() {
            return mIcons.length;
        }

        class Holder extends BaseViewHolder implements View.OnClickListener {

            TextView  tv;

            public Holder(View itemView) {
                super(itemView);
                tv = f(R.id.tv_add_attachment);

                tv.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos == 0) {
                    RxBus.get().post(Def.Event.TAKE_PHOTO, new Object());
                } else if (pos == 1) {
                    RxBus.get().post(Def.Event.PICK_IMAGE, new Object());
                } else if (pos == 2) {
                    RxBus.get().post(Def.Event.RECORD_AUDIO, new Object());
                } else if (pos == 3) {
                    RxBus.get().post(Def.Event.LOCATE_ME, new Object());
                }
            }
        }

    }

}
