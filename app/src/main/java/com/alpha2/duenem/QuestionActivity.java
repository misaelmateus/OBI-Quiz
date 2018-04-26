package com.alpha2.duenem;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    private static final String TAG = QuestionActivity.class.getSimpleName();
    public static final String TOPIC_EXTRA = "topic_extra";

    private Topic mTopic;
    private Lesson mLesson;
    private LessonUser mLessonUser;



    private Query mUserLessonRef;
    private int correctAlternative;
    private int currentMaterial;
    private List<Material> materials;
    private int contCorrect = 0;
    private boolean isQuestion = false;
    private View mContentView;
    private SUBMIT_BUTTON_STATES buttonState;

    private enum SUBMIT_BUTTON_STATES {
        CONTINUE, VERIFY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTopic = (Topic) getIntent().getSerializableExtra(TOPIC_EXTRA);

        getNextLessonFromTopic();

        mContentView = setContentLayout(R.layout.content_question);
        mContentView.setVisibility(View.INVISIBLE);
    }

    private void getNextLessonFromTopic(){
        final Query mLessonRef;
        mLessonRef = DBHelper.getLessonsFromTopic(mTopic.getUid());
        mLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> idLessons = new ArrayList<>();
                for (DataSnapshot lessonSnap : dataSnapshot.getChildren()){
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

    private void getNextLessonFromUser(final List<String> idLessons){
        mUserLessonRef = DBHelper.getLessonUsersByUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
        final List<String> idLessonsNotDone = new ArrayList<String>();

        mUserLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(String idLesson : idLessons){
                    if(!dataSnapshot.hasChild(idLesson) ||
                            (dataSnapshot.child(idLesson).hasChild("done") &&
                                    !((boolean) dataSnapshot.child(idLesson).child("done").getValue())))
                        idLessonsNotDone.add(idLesson);
                }
                if(idLessonsNotDone.size() == 0){
                    topicCompleted();
                }
                else {
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
    private void getNextLessonFromUid(final String lessonUid){
        final Query mLessonRef;
        mLessonRef = DBHelper.getLessonsFromTopic(mTopic.getUid());
        mLessonRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLesson = dataSnapshot.child(lessonUid).getValue(Lesson.class);
                mLesson.setUid(lessonUid);
                loadDataFromQuestion();
                mContentView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Snackbar.make(mContentView, R.string.user_unauthorized_message, Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        });
    }
    private void topicCompleted(){
        // Do after
    }
    protected void loadDataFromQuestion(){
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
                if(buttonState == SUBMIT_BUTTON_STATES.VERIFY) {
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
                        ShowCorrect();
                        ChangeButtonState();
                    } else {
                        ShowIncorrect(correctAlternative);
                        ChangeButtonState();
                    }
                }
                else {
                    ChangeButtonState();
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
        }
    }


    public void nextQuestion(){
        currentMaterial++;

        ((ProgressBar)findViewById(R.id.progressBarQuestion)).setProgress((currentMaterial *100)/ materials.size());

        if (currentMaterial >= materials.size()) {
            endLesson();
        }
        else {
            setContent(materials.get(currentMaterial));
        }
    }

    private void setContent(Material material) {
        TextView textTitle = (TextView) findViewById(R.id.textTitleQuestion);
        TextView textContent = (TextView) findViewById(R.id.textContentQuestion);

        textTitle.setText(getString(R.string.material_title, currentMaterial +1));
        textContent.setText(material.getText());

        if (material instanceof Question) {
            setContentQuestion((Question) material, currentMaterial +1);
        } else {
            ChangeButtonState();
            findViewById(R.id.radioGroupQuestion).setVisibility(View.GONE);
            contCorrect++; //material count like question corrected
        }
    }

    private void setContentQuestion(Question question, int l) {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupQuestion);
        radioGroup.clearCheck();
        radioGroup.setVisibility(View.VISIBLE);

        TextView textTitle = (TextView) findViewById(R.id.textTitleQuestion);
        textTitle.setText(getString(R.string.question_title, l));

        int[] ListId = new int[]{R.id.radioBt1, R.id.radioBt2, R.id.radioBt3, R.id.radioBt4, R.id.radioBt5};
        Integer[] order = new Integer[]{0, 1, 2, 3, 4};
        List<Integer> list = Arrays.asList(order);
        Collections.shuffle(list);

        for(int i = 0; i < 5; i++){
            RadioButton radioButton = (RadioButton)findViewById(ListId[i]);
            radioButton.setText(question.getTextAlternative(list.get(i)));
            radioButton.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorQuestionDefault, null));
            if(list.get(i) == 0)
                correctAlternative = i;
        }
    }

    private void ShowCorrect(){
        TextView textView = (TextView) findViewById(R.id.textState);
        textView.setText(R.string.correct_answer_message);
    }

    private void ShowIncorrect(int correct_alternative){
        TextView textView = (TextView) findViewById(R.id.textState);
        textView.setText(R.string.incorrect_answer_message);

        int id = -1;
        switch (correct_alternative){
            case 0: id = R.id.radioBt1;
                break;
            case 1: id = R.id.radioBt2;
                break;
            case 2: id = R.id.radioBt3;
                break;
            case 3: id = R.id.radioBt4;
                break;
            case 4: id = R.id.radioBt5;
                break;
        }
        if(id != -1) ((RadioButton)findViewById(id) ).setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorQuestionCorrect, null));
    }

    private void ChangeButtonState(){
        Button bt = (Button) findViewById(R.id.buttonQuestion);
        if(buttonState == SUBMIT_BUTTON_STATES.VERIFY){
            bt.setText(R.string.bt_submit_continue_message);
            buttonState = SUBMIT_BUTTON_STATES.CONTINUE;
        }
        else{
            TextView textView = (TextView) findViewById(R.id.textState);
            textView.setText("");
            bt.setText(R.string.bt_submit_verify_message);
            buttonState = SUBMIT_BUTTON_STATES.VERIFY;
        }
    }

    private void endLesson(){
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupQuestion);
        radioGroup.setVisibility(View.GONE);
        int grade = (contCorrect * 100) / materials.size();

        TextView text1 = (TextView) findViewById(R.id.textTitleQuestion);
        TextView text2 = (TextView) findViewById(R.id.textContentQuestion);
        ((ProgressBar)findViewById(R.id.progressBarQuestion)).setProgress(100);
        Button bt = (Button) findViewById(R.id.buttonQuestion);
        bt.setText(getString(R.string.bt_submit_continue_message));
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if(!isQuestion){
            text1.setText(R.string.lesson_conclude);
            text2.setText("");
            return;
        }

        if(grade >= 70){
            text1.setText(R.string.end_lesson_success_message);
        }
        else{
            text1.setText(R.string.end_lesson_fail_message);
        }

        text2.setText(getString(R.string.lesson_result_message, contCorrect, materials.size()));

        setUserLessonResult(grade);
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
        Date date =  new Date();
        int interval = lessonUser.getInterval();
        date = new Date(date.getTime() + TimeUnit.DAYS.toMillis(interval));
        return date;
    }

}
