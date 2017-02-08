package com.nile.kmooc.view;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;
import com.nile.kmooc.base.BaseFragment;
import com.nile.kmooc.core.IEdxEnvironment;
import com.nile.kmooc.logger.Logger;
import com.nile.kmooc.model.api.CourseEntry;
import com.nile.kmooc.util.images.TopAnchorFillWidthTransformation;

import com.nile.kmooc.model.api.EnrolledCoursesResponse;
import com.nile.kmooc.module.analytics.ISegment;
import com.nile.kmooc.util.ResourceUtil;
import com.nile.kmooc.util.images.ShareUtils;

public class CourseDashboardFragment extends BaseFragment {
    static public String TAG = CourseHandoutFragment.class.getCanonicalName();
    static public String CourseData = TAG + ".course_data";
    protected final Logger logger = new Logger(getClass().getName());
    @Inject
    IEdxEnvironment environment;
    private EnrolledCoursesResponse courseData;
    private boolean isCoursewareAccessible = true;
    private TextView courseTextName;
    private TextView courseTextDetails;
    private ImageView headerImageView;
    private LinearLayout parent;
    private TextView errorText;
    private ImageButton shareButton;

    @Inject
    private ISegment segIO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        courseData = (EnrolledCoursesResponse) args.getSerializable(CourseData);
        if (courseData != null) {
            isCoursewareAccessible = courseData.getCourse().getCoursewareAccess().hasAccess();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if (isCoursewareAccessible) {
            view = inflater.inflate(com.nile.kmooc.R.layout.fragment_course_dashboard, container, false);
            courseTextName = (TextView) view.findViewById(com.nile.kmooc.R.id.course_detail_name);
            courseTextDetails = (TextView) view.findViewById(com.nile.kmooc.R.id.course_detail_extras);
            headerImageView = (ImageView) view.findViewById(com.nile.kmooc.R.id.header_image_view);
            parent = (LinearLayout) view.findViewById(com.nile.kmooc.R.id.dashboard_detail);
            shareButton = (ImageButton) view.findViewById(com.nile.kmooc.R.id.course_detail_share); //invisible by default

            // Full course name should appear on the course's dashboard screen.
            courseTextName.setEllipsize(null);
            courseTextName.setSingleLine(false);
        } else {
            view = inflater.inflate(com.nile.kmooc.R.layout.fragment_course_dashboard_disabled, container, false);
            errorText = (TextView) view.findViewById(com.nile.kmooc.R.id.error_msg);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isCoursewareAccessible) {
            final LayoutInflater inflater = LayoutInflater.from(getActivity());

            if (courseData.isCertificateEarned() && environment.getConfig().areCertificateLinksEnabled()) {
                final View child = inflater.inflate(com.nile.kmooc.R.layout.row_course_dashboard_cert, parent, false);
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        environment.getRouter().showCertificate(getActivity(), courseData);
                    }
                });
                parent.addView(child);
            }

            //Implementation Note: - we can create a list view and populate the list.
            //but as number of rows are fixed and each row is different. the only common
            //thing is UI layout. so we reuse the same UI layout programmatically here.
            ViewHolder holder = createViewHolder(inflater, parent);

            holder.typeView.setIcon(FontAwesomeIcons.fa_list_alt);
            holder.titleView.setText(com.nile.kmooc.R.string.courseware_title);
            holder.subtitleView.setText(com.nile.kmooc.R.string.courseware_subtitle);
            holder.rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    environment.getRouter().showCourseContainerOutline(getActivity(), courseData);
                }
            });


            if (courseData != null
                    && !TextUtils.isEmpty(courseData.getCourse().getDiscussionUrl())
                    && environment.getConfig().isDiscussionsEnabled()) {
                holder = createViewHolder(inflater, parent);

                holder.typeView.setIcon(FontAwesomeIcons.fa_comments_o);
                holder.titleView.setText(com.nile.kmooc.R.string.discussion_title);
                holder.subtitleView.setText(com.nile.kmooc.R.string.discussion_subtitle);
                holder.rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        environment.getRouter().showCourseDiscussionTopics(getActivity(), courseData);
                    }
                });
            }

            holder = createViewHolder(inflater, parent);

            holder.typeView.setIcon(FontAwesomeIcons.fa_file_text_o);
            holder.titleView.setText(com.nile.kmooc.R.string.handouts_title);
            holder.subtitleView.setText(com.nile.kmooc.R.string.handouts_subtitle);
            holder.rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (courseData != null)
                        environment.getRouter().showHandouts(getActivity(), courseData);
                }
            });

            holder = createViewHolder(inflater, parent);

            holder.typeView.setIcon(FontAwesomeIcons.fa_bullhorn);
            holder.titleView.setText(com.nile.kmooc.R.string.announcement_title);
            holder.subtitleView.setText(com.nile.kmooc.R.string.announcement_subtitle);
            holder.rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (courseData != null)
                        environment.getRouter().showCourseAnnouncement(getActivity(), courseData);
                }
            });
        } else {
            errorText.setText(com.nile.kmooc.R.string.course_not_started);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (courseData == null || !isCoursewareAccessible) return;

        final String headerImageUrl = courseData.getCourse().getCourse_image(environment.getConfig().getApiHostURL());
        Glide.with(CourseDashboardFragment.this)
                .load(headerImageUrl)
                .placeholder(com.nile.kmooc.R.drawable.placeholder_course_card_image)
                .transform(new TopAnchorFillWidthTransformation(getActivity()))
                .into(headerImageView);

        courseTextName.setText(courseData.getCourse().getName());
        CourseEntry course = courseData.getCourse();
        courseTextDetails.setText(course.getDescriptionWithStartDate(getActivity()));

        if (environment.getConfig().isCourseSharingEnabled()) {
            shareButton.setVisibility(headerImageView.VISIBLE);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openShareMenu();
                }
            });
        }
    }

    /**
     * Creates a dropdown menu with appropriate apps when the share button is clicked.
     */
    private void openShareMenu() {
        final String shareTextWithPlatformName = ResourceUtil.getFormattedString(
                getResources(),
                com.nile.kmooc.R.string.share_course_message,
                "platform_name",
                getString(com.nile.kmooc.R.string.platform_name)).toString() + "\n" + courseData.getCourse().getCourse_about();
        ShareUtils.showShareMenu(
                ShareUtils.newShareIntent(shareTextWithPlatformName),
                getActivity().findViewById(com.nile.kmooc.R.id.course_detail_share),
                new ShareUtils.ShareMenuItemListener() {
                    @Override
                    public void onMenuItemClick(@NonNull ComponentName componentName, @NonNull ShareUtils.ShareType shareType) {
                        final String shareText;
                        final String twitterTag = environment.getConfig().getTwitterConfig().getHashTag();
                        if (shareType == ShareUtils.ShareType.TWITTER && !TextUtils.isEmpty(twitterTag)) {
                            shareText = ResourceUtil.getFormattedString(
                                    getResources(),
                                    com.nile.kmooc.R.string.share_course_message,
                                    "platform_name",
                                    twitterTag).toString() + "\n" + courseData.getCourse().getCourse_about();

                        } else {
                            shareText = shareTextWithPlatformName;
                        }
                        segIO.courseDetailShared(courseData.getCourse().getId(), shareText, shareType);
                        final Intent intent = ShareUtils.newShareIntent(shareText);
                        intent.setComponent(componentName);
                        startActivity(intent);
                    }
                },
                com.nile.kmooc.R.string.share_course_popup_header);
    }

    private ViewHolder createViewHolder(LayoutInflater inflater, LinearLayout parent) {
        ViewHolder holder = new ViewHolder();
        holder.rowView = inflater.inflate(com.nile.kmooc.R.layout.row_course_dashboard_list, parent, false);
        holder.typeView = (IconImageView) holder.rowView.findViewById(com.nile.kmooc.R.id.row_type);
        holder.titleView = (TextView) holder.rowView.findViewById(com.nile.kmooc.R.id.row_title);
        holder.subtitleView = (TextView) holder.rowView.findViewById(com.nile.kmooc.R.id.row_subtitle);
        parent.addView(holder.rowView);
        return holder;
    }

    private class ViewHolder {
        View rowView;
        IconImageView typeView;
        TextView titleView;
        TextView subtitleView;
    }
}
