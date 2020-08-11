package ml.dstudios.travmeet.ui.notifications;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

import ml.dstudios.travmeet.R;

public class NotificationsViewModel extends ViewModel {

    private MutableLiveData<String> mText;



    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

}