package com.nile.kmooc.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.nile.kmooc.base.BaseSingleFragmentActivity;
import com.nile.kmooc.discussion.DiscussionTopic;

import com.nile.kmooc.model.api.EnrolledCoursesResponse;
import com.nile.kmooc.module.analytics.ISegment;

import java.util.HashMap;
import java.util.Map;

import roboguice.inject.InjectExtra;

public class CourseDiscussionPostsActivity extends BaseSingleFragmentActivity {

    @Inject
    private CourseDiscussionPostsThreadFragment courseDiscussionPostsThreadFragment;

    @Inject
    private CourseDiscussionPostsSearchFragment courseDiscussionPostsSearchFragment;

    @InjectExtra(value = Router.EXTRA_SEARCH_QUERY, optional = true)
    private String searchQuery;

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC, optional = true)
    private DiscussionTopic discussionTopic;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blockDrawerFromOpening();

        String screenName;
        String actionItem;
        Map<String, String> values = new HashMap<>();
        if (searchQuery != null) {
            screenName = ISegment.Screens.FORUM_SEARCH_THREADS;
            values.put(ISegment.Keys.SEARCH_STRING, searchQuery);
            actionItem = searchQuery;
        } else {
            screenName = ISegment.Screens.FORUM_VIEW_TOPIC_THREADS;
            String topicId = discussionTopic.getIdentifier();
            if (DiscussionTopic.ALL_TOPICS_ID.equals(topicId)) {
                topicId = actionItem = ISegment.Values.POSTS_ALL;
            } else if (DiscussionTopic.FOLLOWING_TOPICS_ID.equals(topicId)) {
                topicId = actionItem = ISegment.Values.POSTS_FOLLOWING;
            } else {
                actionItem = discussionTopic.getName();
            }
            values.put(ISegment.Keys.TOPIC_ID, topicId);
        }
        environment.getSegment().trackScreenView(screenName, courseData.getCourse().getId(),
                actionItem, values);
    }

    @Override
    public Fragment getFirstFragment() {
        Fragment fragment;
        if (searchQuery != null) {
            fragment = courseDiscussionPostsSearchFragment;
        } else {
            fragment = courseDiscussionPostsThreadFragment;
        }

        // TODO: Move argument setting logic to base class
        // Currently RoboGuice doesn't allowing injecting arguments of a Fragment
        if (fragment.getArguments() == null) {
            Bundle args = new Bundle();
            args.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
            args.putBoolean(CourseDiscussionPostsThreadFragment.ARG_DISCUSSION_HAS_TOPIC_NAME,
                    discussionTopic != null);
            fragment.setArguments(args);
        }
        fragment.setRetainInstance(true);

        return fragment;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (searchQuery != null) {
            setTitle(getString(com.nile.kmooc.R.string.discussion_posts_search_title));
            return;
        }

        if (discussionTopic != null && discussionTopic.getName() != null) {
            if (discussionTopic.isFollowingType()) {
                SpannableString title = new SpannableString("   " + discussionTopic.getName());
                IconDrawable starIcon = new IconDrawable(this, FontAwesomeIcons.fa_star)
                        .colorRes(this, com.nile.kmooc.R.color.white)
                        .sizeRes(this, com.nile.kmooc.R.dimen.edx_base)
                        .tint(null); // IconDrawable is tinted by default, but we don't want it to be tinted here
                starIcon.setBounds(0, 0, starIcon.getIntrinsicWidth(), starIcon.getIntrinsicHeight());
                ImageSpan iSpan = new ImageSpan(starIcon, ImageSpan.ALIGN_BASELINE);
                title.setSpan(iSpan, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                setTitle(title);
            } else {
                setTitle(discussionTopic.getName());
            }
        }
    }
}
