package com.novoda.bonfire.login.database;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.novoda.bonfire.login.data.model.Authentication;
import com.novoda.bonfire.user.data.model.User;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class FirebaseAuthDatabase implements AuthDatabase {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthDatabase(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public Observable<Authentication> readAuthentication() {
        return Observable.create(new ObservableOnSubscribe<Authentication>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Authentication> emitter) throws Exception {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    emitter.onNext(authenticationFrom(currentUser));
                }
                emitter.onComplete();
            }
        });
    }

    @Override
    public Observable<Authentication> loginWithGoogle(final String idToken) {
        return Observable.create(new ObservableOnSubscribe<Authentication>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Authentication> emitter) throws Exception {
                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = task.getResult().getUser();
                                    emitter.onNext(authenticationFrom(firebaseUser));
                                } else {
                                    emitter.onNext(new Authentication(task.getException()));
                                }
                                emitter.onComplete();
                            }
                        });
            }
        });
    }

    private Authentication authenticationFrom(FirebaseUser currentUser) {
        Uri photoUrl = currentUser.getPhotoUrl();
        return new Authentication(new User(currentUser.getUid(), currentUser.getDisplayName(), photoUrl == null ? "" : photoUrl.toString()));
    }

}
