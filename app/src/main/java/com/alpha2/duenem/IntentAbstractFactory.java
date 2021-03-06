package com.alpha2.duenem;

import android.app.Activity;
import android.content.Intent;

import com.alpha2.duenem.model.Discipline;
import com.alpha2.duenem.signin.SignInActivity;

public abstract class IntentAbstractFactory {

    public static Intent createDisciplineActivityIntent(Activity source, Discipline discipline) {
        Intent intent = new Intent(source, DisciplineActivity.class);
        intent.putExtra(BaseActivity.SELECTED_ITEM_ID_EXTRA, R.id.materialestudo);
        intent.putExtra(DisciplineActivity.DISCIPLINE_EXTRA, discipline);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    public static Intent createSignInActivityIntent(Activity source) {
        Intent intent = new Intent(source, SignInActivity.class);
        intent.putExtra(BaseActivity.SELECTED_ITEM_ID_EXTRA, R.id.perfil);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    public static Intent createRankingActivityIntent(Activity source) {
        Intent intent = new Intent(source, RankingActivity.class);
        intent.putExtra(BaseActivity.SELECTED_ITEM_ID_EXTRA, R.id.ranking);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

}
