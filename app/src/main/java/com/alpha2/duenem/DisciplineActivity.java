package com.alpha2.duenem;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.alpha2.duenem.db.DBHelper;
import com.alpha2.duenem.model.Discipline;
import com.alpha2.duenem.model.Topic;
import com.alpha2.duenem.view_pager_cards.CardPagerAdapter;
import com.alpha2.duenem.view_pager_cards.ShadowTransformer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DisciplineActivity extends BaseActivity {

    private ViewPager mViewPager;
    private CardPagerAdapter mCardAdapter;
    private View mContentView;

    private static final String TAG = DisciplineActivity.class.getSimpleName();
    private ArrayAdapter<Topic> mAdapter;

    private ValueEventListener mTopicRefListener;
    private Query mTopicRef;

    public static final String DISCIPLINE_EXTRA = "discipline_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View contentView = setContentLayout(R.layout.content_discipline);

        mContentView = setContentLayout(R.layout.content_lesson);

        mViewPager = (ViewPager) mContentView.findViewById(R.id.viewPagerLesson);

        mCardAdapter = new CardPagerAdapter(this);

        initiateList();
        updateTopics();
    }

    @Override
    protected void onResume(){
        super.onResume();
        updateTopics();
    }

    private void updateTopics() {
        if (DuEnemApplication.getInstance().getUser() != null) {
            Discipline discipline = (Discipline) getIntent().getSerializableExtra(DISCIPLINE_EXTRA);
            mTopicRef = null;

            if (discipline != null) {
                setTitle(discipline.getName());

                mAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, new ArrayList<Topic>());

                mTopicRef = DBHelper.getTopicsFromDiscipline(discipline);

                mTopicRefListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mAdapter.clear();
                        for (DataSnapshot topicSnap : dataSnapshot.getChildren()) {
                            Topic topic = topicSnap.getValue(Topic.class);

                            if (topic != null) {
                                topic.setUid(topicSnap.getKey());
                                mCardAdapter.addTopic(topic);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, databaseError.getMessage());
                        mAdapter.clear();
                        Snackbar.make(mContentView, R.string.user_unauthorized_message, Snackbar.LENGTH_INDEFINITE)
                                .show();
                    }

                };
            }
        } else {
            Snackbar.make(mContentView, R.string.user_unauthorized_message, Snackbar.LENGTH_INDEFINITE)
                    .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mTopicRef != null)
            mTopicRef.addValueEventListener(mTopicRefListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTopicRef != null)
            mTopicRef.removeEventListener(mTopicRefListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initiateList() {
        ShadowTransformer mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);

        mViewPager.setAdapter(mCardAdapter);
        mViewPager.setPageTransformer(false, mCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(3);
    }
}
