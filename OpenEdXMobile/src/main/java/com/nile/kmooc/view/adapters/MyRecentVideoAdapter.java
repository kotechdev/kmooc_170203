package com.nile.kmooc.view.adapters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nile.kmooc.model.db.DownloadEntry;
import com.nile.kmooc.core.IEdxEnvironment;
import com.nile.kmooc.interfaces.SectionItemInterface;
import com.nile.kmooc.model.api.EnrolledCoursesResponse;
import com.nile.kmooc.module.db.DataCallback;
import com.nile.kmooc.module.db.IDatabase;
import com.nile.kmooc.util.AppConstants;
import com.nile.kmooc.util.CheckboxDrawableUtil;
import com.nile.kmooc.util.MemoryUtil;

public abstract class MyRecentVideoAdapter extends VideoBaseAdapter<SectionItemInterface> {

    private IDatabase dbStore;

    public MyRecentVideoAdapter(Context context, IEdxEnvironment environment) {
        super(context, com.nile.kmooc.R.layout.row_video_list, environment);
        this.dbStore = environment.getDatabase();
    }

    @Override
    public void render(BaseViewHolder tag, final SectionItemInterface sectionItem) {
        final ViewHolder holder = (ViewHolder) tag;

        if (sectionItem != null) {
            if(sectionItem.isCourse()){
                EnrolledCoursesResponse enrollment = (EnrolledCoursesResponse) sectionItem;
                holder.section_title.setText(enrollment.getCourse().getName());
                holder.section_title.setVisibility(View.VISIBLE);
                holder.videolayout.setVisibility(View.GONE);
            }else if(sectionItem.isDownload()){
                holder.section_title.setVisibility(View.GONE);
                holder.videolayout.setVisibility(View.VISIBLE);

                // mark as NOT selected
                holder.videolayout.setSelected(false);
                holder.videolayout.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (AppConstants.myVideosDeleteMode) return;
                        selectedPosition = holder.position;
                        onItemClick(sectionItem, holder.position);
                    }
                });

                DownloadEntry videoData = (DownloadEntry) sectionItem;
                String selectedVideoId = getVideoId();
                holder.videoTitle.setText(videoData.getTitle());
                holder.videoSize.setText(MemoryUtil.format(getContext(), videoData.size));
                holder.videoPlayingTime.setText(videoData.getDurationReadable());

                dbStore.getWatchedStateForVideoId(videoData.videoId,
                        new DataCallback<DownloadEntry.WatchedState>(true) {
                    @Override
                    public void onResult(DownloadEntry.WatchedState result) {
                        DownloadEntry.WatchedState ws = result;if(ws == null || ws == DownloadEntry.WatchedState.UNWATCHED) {
                            holder.video_watched_status.setProgress(100);
                        } else if(ws == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                            holder.video_watched_status.setProgress(50);
                        } else {
                            holder.video_watched_status.setProgress(0);
                        }
                    }
                    @Override
                    public void onFail(Exception ex) {
                        logger.error(ex);
                    }
                });

                if(videoData.isDownloaded()) {
                    if (selectedVideoId != null) {
                        if (selectedVideoId.equalsIgnoreCase(videoData.videoId)) {
                            // mark this cell as selected and playing
                            holder.videolayout.setBackgroundResource(com.nile.kmooc.R.color.cyan_text_navigation_20);
                        } else {
                            // mark this cell as non-selected
                            holder.videolayout.setBackgroundResource(com.nile.kmooc.R.drawable.list_item_overlay_selector);
                        }
                    } else {
                        holder.videolayout.setBackgroundResource(com.nile.kmooc.R.drawable.list_item_overlay_selector);
                    }

                    if (AppConstants.myVideosDeleteMode) {
                        holder.delete_checkbox.setVisibility(View.VISIBLE);
                        holder.delete_checkbox.setChecked(isSelected(holder.position));
                    } else {
                        holder.delete_checkbox.setVisibility(View.GONE);
                    }
                }else{
                    holder.videolayout.setBackgroundResource(com.nile.kmooc.R.drawable.list_item_overlay_selector);
                    holder.delete_checkbox.setVisibility(View.GONE);
                }

            }
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        final ViewHolder holder = new ViewHolder();
        holder.videoTitle = (TextView) convertView
                .findViewById(com.nile.kmooc.R.id.video_title);
        holder.videoPlayingTime = (TextView) convertView
                .findViewById(com.nile.kmooc.R.id.video_playing_time);
        holder.videoSize = (TextView) convertView
                .findViewById(com.nile.kmooc.R.id.video_size);
        holder.video_watched_status = (ProgressBar) convertView
                .findViewById(com.nile.kmooc.R.id.video_watched_status);
        holder.section_title = (TextView) convertView
                .findViewById(com.nile.kmooc.R.id.txt_chapter_title);
        holder.videolayout = (RelativeLayout) convertView
                .findViewById(com.nile.kmooc.R.id.video_row_layout);
        holder.delete_checkbox  = (CheckBox) convertView
                .findViewById(com.nile.kmooc.R.id.video_select_checkbox);
        holder.delete_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    select(holder.position);
                    onSelectItem();
                } else {
                    unselect(holder.position);
                    onSelectItem();
                }
            }
        });
        holder.delete_checkbox.setButtonDrawable(
                CheckboxDrawableUtil.createStateListDrawable(
                        holder.delete_checkbox.getContext(),
                        com.nile.kmooc.R.dimen.fa_large,
                        com.nile.kmooc.R.color.edx_brand_primary_base,
                        com.nile.kmooc.R.color.edx_brand_gray_base
                )
        );
        return holder;
    }

    private static class ViewHolder extends BaseViewHolder {
        TextView videoTitle;
        TextView videoPlayingTime;
        TextView videoSize;
        ProgressBar video_watched_status;
        CheckBox delete_checkbox;
        TextView section_title;
        RelativeLayout videolayout;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//        selectedPosition=position;
//        SectionItemInterface model = getItem(position);
//        if(model!=null) onItemClicked(model, position);
    }

//    public abstract void onItemClicked(SectionItemInterface model, int position);
    public abstract void onSelectItem();
    protected abstract void onItemClick(SectionItemInterface model, int position);
}
