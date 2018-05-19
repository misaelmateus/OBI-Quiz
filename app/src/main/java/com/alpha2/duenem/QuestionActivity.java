package com.alpha2.duenem;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alpha2.duenem.db.DBHelper;
import com.alpha2.duenem.model.Lesson;
import com.alpha2.duenem.model.LessonUser;
import com.alpha2.duenem.model.Material;
import com.alpha2.duenem.model.Question;
import com.alpha2.duenem.model.Topic;
import com.alpha2.duenem.model.User;
import com.alpha2.duenem.util.LazyLoadingDrawable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class QuestionActivity extends BaseActivity {

    public static final String TOPIC_EXTRA = "topic_extra";
    private static final String TAG = QuestionActivity.class.getSimpleName();
    private Topic mTopic;
    private Lesson mLesson;
    private LessonUser mLessonUser;

    private int correctAlternative;
    private int currentMaterial;
    private List<Material> materials;
    private int contCorrect = 0;
    private boolean isQuestion = false;
    private View mContentView;
    private SUBMIT_BUTTON_STATES buttonState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentView = setContentLayout(R.layout.content_question);
        mContentView.findViewById(R.id.container).setVisibility(View.INVISIBLE);

        mTopic = (Topic) getIntent().getSerializableExtra(TOPIC_EXTRA);
        getNextLessonFromTopic();
    }

    private void getNextLessonFromTopic() {
        final Query lessonRef = DBHelper.getLessonsFromTopic(mTopic.getUid());

        lessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> idLessons = new ArrayList<>();
                for (DataSnapshot lessonSnap : dataSnapshot.getChildren()) {
                    idLessons.add(lessonSnap.getKey());
                }
                getNextLessonFromUser(idLessons);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Snackbar.make(mContentView, R.string.user_unauthorized_message, Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        });
    }

    private void getNextLessonFromUser(final List<String> idLessons) {
        final String userUid = DuEnemApplication.getInstance().getUser().getUid();
        Query userLessonRef = DBHelper.getLessonUsersByUser(userUid);
        final List<String> idLessonsNotDone = new ArrayList<>();

        userLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (String idLesson : idLessons) {
                    if (!dataSnapshot.hasChild(idLesson) || !(boolean) dataSnapshot.child(idLesson).child("done").getValue()) {
                        idLessonsNotDone.add(idLesson);
                    }
                }
                if (idLessonsNotDone.size() == 0) {
                    topicCompleted();
                } else {
                    int pos_element = new Random().nextInt(idLessonsNotDone.size());
                    getNextLessonFromUid(idLessonsNotDone.get(pos_element));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Snackbar.make(mContentView, R.string.user_unauthorized_message, Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        });
    }

    private void getNextLessonFromUid(final String lessonUid) {
        final Query lessonRef = DBHelper.getLesson(mTopic.getUid(), lessonUid);
        lessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLesson = dataSnapshot.getValue(Lesson.class);
                mLesson.setUid(lessonUid);
                loadDataFromQuestion();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Snackbar.make(mContentView, R.string.user_unauthorized_message, Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        });
    }

    private void topicCompleted() {
        // Do after
    }

    protected void loadDataFromQuestion() {
        this.setTitle(mLesson.getTitle());

        final Query materialsQuery = DBHelper.getMaterialsFromLesson(mLesson.getUid());
        materialsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLesson.getMaterial().clear();
                materialsQuery.removeEventListener(this);

                for (DataSnapshot materialSnap : dataSnapshot.getChildren()) {
                    if (materialSnap.child("alternatives").exists()) {
                        isQuestion = true;
                        Question q = materialSnap.getValue(Question.class);
                        mLesson.addMaterial(q);
                    } else {
                        Material m = materialSnap.getValue(Material.class);
                        mLesson.addMaterial(m);
                    }
                }

                initiate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.getMessage());
            }
        });

        buttonState = SUBMIT_BUTTON_STATES.VERIFY;

        Button bt = (Button) findViewById(R.id.buttonQuestion);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonState == SUBMIT_BUTTON_STATES.VERIFY) {
                    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupQuestion);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    int iSelected = -1;
                    if (selectedId == findViewById(R.id.radioBt1).getId())
                        iSelected = 0;
                    else if (selectedId == findViewById(R.id.radioBt2).getId())
                        iSelected = 1;
                    else if (selectedId == findViewById(R.id.radioBt3).getId())
                        iSelected = 2;
                    else if (selectedId == findViewById(R.id.radioBt4).getId())
                        iSelected = 3;
                    else if (selectedId == findViewById(R.id.radioBt5).getId())
                        iSelected = 4;

                    if (iSelected == correctAlternative) {
                        contCorrect++;
                        showCorrect();
                        changeButtonState();
                    } else {
                        showIncorrect(correctAlternative);
                        changeButtonState();
                    }
                } else {
                    changeButtonState();
                    nextQuestion();
                }
            }
        });
    }

    private void initiate() {
        User user = DuEnemApplication.getInstance().getUser();

        if (user != null) {
            String userUid = user.getUid();
            final Query lessonUsersQuery = DBHelper.getLessonUsersByUser(userUid)
                    .child(mLesson.getUid());

            lessonUsersQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mLessonUser = dataSnapshot.getValue(LessonUser.class);
                    lessonUsersQuery.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, databaseError.getMessage());
                    mLessonUser = null;
                }
            });

            materials = mLesson.getMaterial();
            currentMaterial = -1;
            nextQuestion();

            mContentView.findViewById(R.id.container).setVisibility(View.VISIBLE);
        }
    }

    public void nextQuestion() {
        currentMaterial++;

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarQuestion);
        progressBar.setProgress((currentMaterial * 100) / materials.size());

        if (currentMaterial >= materials.size()) {
            endLesson();
        } else {
            setContent(materials.get(currentMaterial));
        }
    }

    private void setContent(Material material) {
        TextView textLesson = (TextView) findViewById(R.id.textLesson);
        TextView textTitle = (TextView) findViewById(R.id.textTitleQuestion);
        TextView textContent = (TextView) findViewById(R.id.textContentQuestion);

        textLesson.setText(fromHtml(textLesson, mLesson.getText()));

        textTitle.setText(getString(R.string.material_title, currentMaterial + 1));
        textContent.setText(fromHtml(textContent, material.getText()));

        if (material instanceof Question) {
            setContentQuestion((Question) material, currentMaterial + 1);
        } else {
            changeButtonState();
            findViewById(R.id.radioGroupQuestion).setVisibility(View.GONE);
            contCorrect++; //material count like question corrected
        }
    }

    private void setContentQuestion(Question question, int number) {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupQuestion);
        radioGroup.clearCheck();
        radioGroup.setVisibility(View.VISIBLE);

        TextView textTitle = (TextView) findViewById(R.id.textTitleQuestion);
        textTitle.setText(getString(R.string.question_title, number));

        int[] ListId = new int[]{R.id.radioBt1, R.id.radioBt2, R.id.radioBt3, R.id.radioBt4, R.id.radioBt5};
        Integer[] order = new Integer[]{0, 1, 2, 3, 4};
        List<Integer> list = Arrays.asList(order);
        Collections.shuffle(list);

        for (int i = 0; i < 5; i++) {
            RadioButton radioButton = (RadioButton) findViewById(ListId[i]);
            radioButton.setText(fromHtml(radioButton, question.getTextAlternative(list.get(i))));
            radioButton.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorQuestionDefault, null));
            if (list.get(i) == 0)
                correctAlternative = i;
        }
    }

    private void showCorrect() {
        TextView textView = (TextView) findViewById(R.id.textState);
        textView.setText(R.string.correct_answer_message);
    }

    private void showIncorrect(int correctAlternative) {
        TextView textView = (TextView) findViewById(R.id.textState);
        textView.setText(R.string.incorrect_answer_message);

        int id = -1;
        switch (correctAlternative) {
            case 0:
                id = R.id.radioBt1;
                break;
            case 1:
                id = R.id.radioBt2;
                break;
            case 2:
                id = R.id.radioBt3;
                break;
            case 3:
                id = R.id.radioBt4;
                break;
            case 4:
                id = R.id.radioBt5;
                break;
        }

        if (id != -1) {
            RadioButton radioButton = (RadioButton) findViewById(id);
            radioButton.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorQuestionCorrect, null));
        }
    }

    private void changeButtonState() {
        Button bt = (Button) findViewById(R.id.buttonQuestion);
        if (buttonState == SUBMIT_BUTTON_STATES.VERIFY) {
            bt.setText(R.string.bt_submit_continue_message);
            buttonState = SUBMIT_BUTTON_STATES.CONTINUE;
        } else {
            TextView textView = (TextView) findViewById(R.id.textState);
            textView.setText("");
            bt.setText(R.string.bt_submit_verify_message);
            buttonState = SUBMIT_BUTTON_STATES.VERIFY;
        }
    }

    private void endLesson() {
        TextView lessonText = (TextView) findViewById(R.id.textLesson);
        lessonText.setVisibility(View.GONE);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupQuestion);
        radioGroup.setVisibility(View.GONE);

        int grade = (contCorrect * 100) / materials.size();

        TextView text1 = (TextView) findViewById(R.id.textTitleQuestion);
        TextView text2 = (TextView) findViewById(R.id.textContentQuestion);
        ProgressBar pg = (ProgressBar) findViewById(R.id.progressBarQuestion);
        pg.setProgress(100);
        Button bt = (Button) findViewById(R.id.buttonQuestion);
        bt.setText(getString(R.string.bt_submit_continue_message));
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (isQuestion) {
            if (grade >= 70) {
                text1.setText(R.string.end_lesson_success_message);
            } else {
                text1.setText(R.string.end_lesson_fail_message);
            }

            text2.setText(getString(R.string.lesson_result_message, contCorrect, materials.size()));

            setUserLessonResult(grade);
        } else {
            text1.setText(R.string.lesson_conclude);
            text2.setText("");
        }
    }

    private void setUserLessonResult(int grade) {
        if (mLessonUser == null) {
            mLessonUser = new LessonUser();
        }

        mLessonUser.userDoneQuestion(grade, new Date(), -1);

        mLessonUser.setLastDate(new Date());
        mLessonUser.setNextDate(calculateNextDate(mLessonUser));

        User user = DuEnemApplication.getInstance().getUser();

        DatabaseReference root = DBHelper.getRoot();

        Map<String, Object> updates = new HashMap<>();
        updates.put(String.format("lessonUser/%s/%s", user.getUid(), mLesson.getUid()), mLessonUser);

        if (!mLesson.isDone())
            updates.put(String.format("user/%s/points", user.getUid()), user.getPoints() + (mLessonUser.getDone() ? 100 : 0));

        root.updateChildren(updates);
    }

    private Date calculateNextDate(LessonUser lessonUser) {
        Date date = new Date();
        int interval = lessonUser.getInterval();
        date = new Date(date.getTime() + TimeUnit.DAYS.toMillis(interval));
        return date;
    }

    private enum SUBMIT_BUTTON_STATES {
        CONTINUE, VERIFY
    }

    private Spanned fromHtml(TextView textView, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY, new ImgTagParser(textView, this), null);
        } else {
            return Html.fromHtml(text, new ImgTagParser(textView, this), null);
        }
    }

    private class ImgTagParser implements Html.ImageGetter {
        private Context context;
        private View container;

        /***
         * Construct the ImgTagParser which will execute AsyncTask and refresh the container
         * @param view View that HTML text will be inserted
         * @param context Context of application
         */
        ImgTagParser(View view, Context context) {
            this.context = context;
            this.container = view;
        }

        public Drawable getDrawable(String source) {
            // create our special drawable that draw a "false" drawable
            // before we assign to it the real drawable (image in img tag)
            LazyLoadingDrawable lazyLoadingDrawable = new LazyLoadingDrawable();

            // get the actual source
            ImageGetterAsyncTask asyncTask =
                    new ImageGetterAsyncTask(lazyLoadingDrawable);

            asyncTask.execute(source);

            // return lazyLoadingDrawable to android, to be drawn
            return lazyLoadingDrawable;
        }

        public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
            LazyLoadingDrawable lazyLoadingDrawable;

            private ImageGetterAsyncTask(LazyLoadingDrawable d) {
                this.lazyLoadingDrawable = d;
            }

            @Override
            protected Drawable doInBackground(String... params) {
                String source = params[0];
                return fetchDrawable(source);
            }

            @Override
            protected void onPostExecute(Drawable result) {
                // set the correct bound according to the result from HTTP call
                lazyLoadingDrawable.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());

                // change the reference of the current drawable to the result
                // from the HTTP call, assign real image to drawable
                lazyLoadingDrawable.setDrawable(result);

                // redraw the image by invalidating the container
                if (container instanceof TextView) {
                    // in TextView only this manner works well
                    // I do not know why
                    TextView tv = (TextView) container;
                    tv.setText(tv.getText());
                } else {
                    container.invalidate();
                }
            }

            /***
             * Get the Drawable from URL
             * @param urlString URL of Image
             * @return Drawable of image downloaded
             */
            private Drawable fetchDrawable(String urlString) {
                Drawable drawable = null;

                try {
                    // use Picasso to download the image
                    drawable = new BitmapDrawable(getResources(),
                            Picasso.with(context).load(urlString).get());
                    // set the right bounds for drawable
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                // return drawable created by Picasso
                return drawable;
            }
        }
    }
}
