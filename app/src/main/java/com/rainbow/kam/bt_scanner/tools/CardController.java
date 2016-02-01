package com.rainbow.kam.bt_scanner.tools;

/**
 * Created by kam6512 on 2016-01-28.
 */

import android.os.Handler;
import android.support.v7.widget.CardView;

class CardController {




    private final Runnable showAction = new Runnable() {
        @Override
        public void run() {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    show(unlockAction);
                }
            }, 500);
        }
    };

    private final Runnable unlockAction =  new Runnable() {
        @Override
        public void run() {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    unlock();
                }
            }, 500);
        }
    };


    boolean firstStage;


    CardController(CardView card){
        super();
//        mPlayBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                dismiss(showAction);
//            }
//        });

//        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                update();
//            }
//        });
    }


    public void init(){
        show(unlockAction);
    }


    void show(Runnable action){
        lock();
        firstStage = false;
    }

    void update(){
        lock();
        firstStage = !firstStage;
    }

    void dismiss(Runnable action){
        lock();
    }


    private void lock(){
//        mPlayBtn.setEnabled(false);
//        mUpdateBtn.setEnabled(false);
    }

    private void unlock(){
//        mPlayBtn.setEnabled(true);
//        mUpdateBtn.setEnabled(true);
    }
}
