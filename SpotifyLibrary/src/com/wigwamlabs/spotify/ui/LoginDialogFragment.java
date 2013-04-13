package com.wigwamlabs.spotify.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.wigwamlabs.spotify.R;
import com.wigwamlabs.spotify.SpotifyError;

@SuppressWarnings("WeakerAccess")
public class LoginDialogFragment extends DialogFragment {
    private EditText mUsername;
    private EditText mPassword;
    private TextView mErrorMessage;
    private View mButton;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.dialog_login, container, false);

        final View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ViewUtils.showSoftInput(v);
                }
            }
        };

        mUsername = (EditText) root.findViewById(R.id.username);
        mUsername.setOnFocusChangeListener(focusChangeListener);
        mPassword = (EditText) root.findViewById(R.id.password);
        mPassword.setOnFocusChangeListener(focusChangeListener);
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.actionLogin || actionId == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });
        mErrorMessage = (TextView) root.findViewById(R.id.errorMessage);
        mButton = root.findViewById(R.id.login);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        return root;
    }

    private void login() {
        final String username = mUsername.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();

        EditText firstError = null;
        if (username.length() == 0) {
            mUsername.setError(getString(R.string.loginUsernameRequired));
            if (firstError == null) {
                firstError = mUsername;
            }
        } else {
            mUsername.setError(null);
        }

        if (password.length() == 0) {
            mPassword.setError(getString(R.string.loginPasswordRequired));
            if (firstError == null) {
                firstError = mPassword;
            }
        } else {
            mPassword.setError(null);
        }

        if (firstError == null) {
            ViewUtils.hideSoftInput(mUsername);

            mErrorMessage.setVisibility(View.GONE);
            mUsername.setEnabled(false);
            mPassword.setEnabled(false);
            mButton.setEnabled(false);

            final SpotifyActivity activity = (SpotifyActivity) getActivity();
            activity.getSpotifySession().login(username, password, true);
        } else {
            firstError.requestFocus();
        }
    }

    public void onLoggedIn(int error) {
        mUsername.setEnabled(true);
        mPassword.setEnabled(true);
        mButton.setEnabled(true);

        final int errorResourceId;
        switch (error) {
        case SpotifyError.OK:
            errorResourceId = 0;
            break;
        case SpotifyError.UNABLE_TO_CONTACT_SERVER:
            errorResourceId = R.string.loginErrorUnableToContactServer;
            break;
        case SpotifyError.BAD_USERNAME_OR_PASSWORD:
            errorResourceId = R.string.loginErrorBadUsernameOrPassword;
            break;
        case SpotifyError.USER_BANNED:
            errorResourceId = R.string.loginErrorUserBanned;
            break;
        case SpotifyError.USER_NEEDS_PREMIUM:
            errorResourceId = R.string.loginErrorUserNeedsPremium;
            break;
        case SpotifyError.CLIENT_TOO_OLD:
            errorResourceId = R.string.loginErrorClientTooOld;
            break;
        case SpotifyError.OTHER_PERMANENT:
            errorResourceId = R.string.loginErrorPermanent;
            break;
        default:
        case SpotifyError.OTHER_TRANSIENT:
            errorResourceId = R.string.loginErrorTransient;
            break;
        }

        if (errorResourceId == 0) {
            mErrorMessage.setVisibility(View.GONE);
        } else {
            mErrorMessage.setText(errorResourceId);
            mErrorMessage.setVisibility(View.VISIBLE);
        }

        if (error == SpotifyError.BAD_USERNAME_OR_PASSWORD) {
            mUsername.requestFocus();
        }
    }
}
