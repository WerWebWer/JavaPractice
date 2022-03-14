package com.alexiv.finish.server;

import com.alexiv.finish.utils.Time;
import com.alexiv.utils.Logger;

import javax.swing.*;
import java.awt.*;

import static com.alexiv.finish.utils.Constants.TIMER_TEXT_FORMAT;

public class ServerUI extends JFrame {

    private JLabel mTimerLabel;

    private JSlider mHourSlider;
    private JSlider mMinuteSlider;
    private JSlider mSecondSlider;

    private JButton mStartButton;
    private JButton mPauseButton;
    private JButton mStopButton;

    private JPanel rootPanel;

    private Server mServer;

    interface ServerUICallback {
        void updateTimer(Time time);
        void log(String log);
        void end();
    }

    private ServerUICallback mCallback = new ServerUICallback() {
        @Override
        public void updateTimer(Time time) {
            mTimerLabel.setText(time.getTime());
        }

        @Override
        public void log(String log) {
            //no op
        }

        @Override
        public void end() {
            mHourSlider.setEnabled(true);
            mMinuteSlider.setEnabled(true);
            mSecondSlider.setEnabled(true);
        }
    };

    public ServerUI() {
        super("Server");
        setContentPane(rootPanel);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 200);

        mTimerLabel.setFont(new Font("Serif", Font.BOLD, 23));
        init();
    }

    public static void main(String[] args) {
        new ServerUI();
    }

    private void init() {
        mServer = new Server(mCallback);

        mStartButton.addActionListener(e -> {
            mHourSlider.setEnabled(false);
            mMinuteSlider.setEnabled(false);
            mSecondSlider.setEnabled(false);
            setTime();
            mServer.start();
        });

        mPauseButton.addActionListener(e -> {
            mServer.pauseResume();
        });

        mStopButton.addActionListener(e -> {
            mHourSlider.setEnabled(true);
            mMinuteSlider.setEnabled(true);
            mSecondSlider.setEnabled(true);
            mServer.stop();
        });

        mHourSlider.addChangeListener(e -> setTime());
        mMinuteSlider.addChangeListener(e -> setTime());
        mSecondSlider.addChangeListener(e -> setTime());
    }

    private void setTime() {
        mServer.setTimeTimer(mHourSlider.getValue(), mMinuteSlider.getValue(), mSecondSlider.getValue());
        mTimerLabel.setText(slidersToText());
    }

    private String slidersToText() {
        return String.format(TIMER_TEXT_FORMAT,
                mHourSlider.getValue(), mMinuteSlider.getValue(), mSecondSlider.getValue());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        mTimerLabel = new JLabel();
        mTimerLabel.setIconTextGap(4);
        mTimerLabel.setText("Timer");
        rootPanel.add(mTimerLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mStartButton = new JButton();
        mStartButton.setText("Start");
        rootPanel.add(mStartButton, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mStopButton = new JButton();
        mStopButton.setText("Stop");
        rootPanel.add(mStopButton, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mHourSlider = new JSlider();
        mHourSlider.setMajorTickSpacing(3);
        mHourSlider.setMaximum(24);
        mHourSlider.setPaintLabels(true);
        mHourSlider.setPaintTicks(true);
        mHourSlider.setValue(0);
        rootPanel.add(mHourSlider, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mMinuteSlider = new JSlider();
        mMinuteSlider.setMajorTickSpacing(10);
        mMinuteSlider.setMaximum(60);
        mMinuteSlider.setPaintLabels(true);
        mMinuteSlider.setPaintTicks(true);
        mMinuteSlider.setSnapToTicks(true);
        mMinuteSlider.setValue(0);
        rootPanel.add(mMinuteSlider, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mSecondSlider = new JSlider();
        mSecondSlider.setEnabled(true);
        mSecondSlider.setMajorTickSpacing(10);
        mSecondSlider.setMaximum(60);
        mSecondSlider.setPaintLabels(true);
        mSecondSlider.setPaintTicks(true);
        mSecondSlider.setValue(10);
        rootPanel.add(mSecondSlider, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        rootPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        mPauseButton = new JButton();
        mPauseButton.setText("Pause/Resume");
        rootPanel.add(mPauseButton, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Hour");
        rootPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Minute");
        rootPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Seconds");
        rootPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}